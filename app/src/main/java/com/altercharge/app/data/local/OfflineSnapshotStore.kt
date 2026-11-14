/**
 * 연결이 끊겨도 마지막 값을 보여 주려고 기기에 저장한다 (REQ_CM_006).
 * 판정 계산 기록도 저장해야 앱을 다시 켜도 같은 날 판정이 유지된다 (REQ_JD_004).
 */
package com.altercharge.app.data.local

import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.RecommendationReason
import com.altercharge.app.data.remote.model.SpeedCategory
import org.json.JSONArray
import org.json.JSONObject
import com.altercharge.app.data.home.RecommendedStationInfo
import android.content.Context
import com.altercharge.app.domain.judgment.Judgment
import com.altercharge.app.domain.judgment.JudgmentResult
import com.altercharge.app.domain.judgment.JudgmentStamp
import java.time.Instant
import java.time.LocalDate

data class OfflineSnapshot(
    val judgment: JudgmentResult?,
    val judgmentStamp: JudgmentStamp?,
    val placeName: String?,
    val nearestStationKm: Double?,
    val stationReceivedAt: Instant?,
    /** 마지막으로 받은 추천 충전소. 오프라인에서도 R1에 유지한다 (REQ_CM_006 "추천 충전소는 유지") */
    val recommended: RecommendedStationInfo? = null,
)

class SharedPreferencesStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putStrings(values: Map<String, String?>) {
        prefs.edit().apply {
            values.forEach { (key, value) -> if (value == null) remove(key) else putString(key, value) }
        }.apply()
    }

    private companion object {
        const val PREFS_NAME = "altercharge_offline"
    }
}

class OfflineSnapshotStore(private val store: KeyValueStore) {

    fun saveJudgment(result: JudgmentResult, stamp: JudgmentStamp) {
        store.putStrings(mapOf(KEY_JUDGMENT to encodeJudgment(result), KEY_JUDGMENT_STAMP to encodeStamp(stamp)))
    }

    /**
     * 충전소 값과 추천 충전소를 한 번에 저장한다. 나눠 쓰면 중간에 앱이 죽었을 때 새 시각과 옛 추천이 섞인다.
     * receivedAt은 응답을 받은 기기 시각이다 (서버 statusObservedAt 아님). recommended가 null이면 추천을 지운다.
     */
    fun saveStation(placeName: String, nearestStationKm: Double?, receivedAt: Instant, recommended: RecommendedStationInfo? = null) {
        store.putStrings(
            mapOf(
                KEY_PLACE_NAME to placeName,
                KEY_NEAREST_KM to nearestStationKm?.toString(),
                KEY_STATION_RECEIVED_AT to receivedAt.toString(),
                KEY_RECOMMENDED to recommended?.let(::encodeRecommended),
            ),
        )
    }

    fun load(): OfflineSnapshot = OfflineSnapshot(
        judgment = store.getString(KEY_JUDGMENT)?.let(::decodeJudgment),
        judgmentStamp = store.getString(KEY_JUDGMENT_STAMP)?.let(::decodeStamp),
        placeName = store.getString(KEY_PLACE_NAME),
        nearestStationKm = store.getString(KEY_NEAREST_KM)?.toDoubleOrNull(),
        stationReceivedAt = store.getString(KEY_STATION_RECEIVED_AT)?.let { runCatching { Instant.parse(it) }.getOrNull() },
        recommended = store.getString(KEY_RECOMMENDED)?.let(::decodeRecommended),
    )

    private companion object {
        const val KEY_JUDGMENT = "judgment"
        const val KEY_JUDGMENT_STAMP = "judgment_stamp"
        const val KEY_PLACE_NAME = "place_name"
        const val KEY_NEAREST_KM = "nearest_station_km"
        const val KEY_STATION_RECEIVED_AT = "station_received_at"
        const val KEY_RECOMMENDED = "recommended_station"
    }
}

// 필드가 몇 개뿐이라 "값|값|…" 한 줄로 저장한다. 깨진 값은 저장 전처럼 null로 읽는다.

private const val COLLECTING = "collecting"
private const val READY = "ready"
private const val SEPARATOR = "|"

internal fun encodeJudgment(result: JudgmentResult): String = when (result) {
    is JudgmentResult.Collecting -> listOf(COLLECTING, result.recordedDays, result.chargeRecommended)
    is JudgmentResult.Ready -> with(result.judgment) {
        listOf(READY, averageDailyKm, reserveKm, daysLeft, chargeRecommended)
    }
}.joinToString(SEPARATOR)

internal fun decodeJudgment(text: String): JudgmentResult? = runCatching {
    val parts = text.split(SEPARATOR)
    when (parts.first()) {
        COLLECTING -> JudgmentResult.Collecting(
            recordedDays = parts[1].toInt(),
            chargeRecommended = parts[2].toBooleanStrict(),
        )
        READY -> JudgmentResult.Ready(
            Judgment(
                averageDailyKm = parts[1].toInt(),
                reserveKm = parts[2].toInt(),
                daysLeft = parts[3].toInt(),
                chargeRecommended = parts[4].toBooleanStrict(),
            ),
        )
        else -> null
    }
}.getOrNull()

internal fun encodeStamp(stamp: JudgmentStamp): String = with(stamp) {
    listOf(computedOn, computedAt, reserveKm ?: "", todayKmAtCompute).joinToString(SEPARATOR)
}

internal fun decodeStamp(text: String): JudgmentStamp? = runCatching {
    val (on, at, reserve, todayKm) = text.split(SEPARATOR)
    JudgmentStamp(
        computedOn = LocalDate.parse(on),
        computedAt = Instant.parse(at),
        reserveKm = reserve.takeIf { it.isNotEmpty() }?.toInt(),
        todayKmAtCompute = todayKm.toInt(),
    )
}.getOrNull()

// 추천 충전소는 필드가 많아 JSON으로 저장한다. 충전기별 상세(chargers)는 오프라인 표시에 쓰지 않아 뺀다.
private fun encodeRecommended(info: RecommendedStationInfo): String = JSONObject().apply {
    put("stationId", info.stationId)
    put("name", info.name)
    put("distanceMeters", info.distanceMeters)
    put("availableCount", info.availableCount)
    info.visitCount?.let { put("visitCount", it) }
    info.speedCategory?.let { put("speedCategory", it.name) }
    info.maxOutputKw?.let { put("maxOutputKw", it) }
    put("fallbackUsed", info.fallbackUsed)
    put("reasonCode", info.reasonCode.name)
    info.address?.let { put("address", it) }
    info.totalValidCount?.let { put("totalValidCount", it) }
    put("connectorTypes", JSONArray(info.connectorTypes.map { it.name }))
    info.lat?.let { put("lat", it) }
    info.lng?.let { put("lng", it) }
    info.statusObservedAt?.let { put("statusObservedAt", it.toString()) }
}.toString()

private fun decodeRecommended(text: String): RecommendedStationInfo? = runCatching {
    val o = JSONObject(text)
    fun optInt(key: String) = if (o.has(key)) o.getInt(key) else null
    fun optDouble(key: String) = if (o.has(key)) o.getDouble(key) else null
    fun optString(key: String) = if (o.has(key)) o.getString(key) else null
    val connectors = o.optJSONArray("connectorTypes")
    RecommendedStationInfo(
        stationId = o.getString("stationId"),
        name = o.getString("name"),
        distanceMeters = o.getInt("distanceMeters"),
        availableCount = o.getInt("availableCount"),
        visitCount = optInt("visitCount"),
        speedCategory = optString("speedCategory")?.let { runCatching { SpeedCategory.valueOf(it) }.getOrNull() },
        maxOutputKw = optDouble("maxOutputKw"),
        fallbackUsed = o.getBoolean("fallbackUsed"),
        reasonCode = runCatching { RecommendationReason.valueOf(o.getString("reasonCode")) }.getOrDefault(RecommendationReason.UNKNOWN),
        address = optString("address"),
        totalValidCount = optInt("totalValidCount"),
        connectorTypes = (0 until (connectors?.length() ?: 0)).mapNotNull { i ->
            runCatching { ConnectorType.valueOf(connectors!!.getString(i)) }.getOrNull()
        },
        lat = optDouble("lat"),
        lng = optDouble("lng"),
        statusObservedAt = optString("statusObservedAt")?.let { runCatching { Instant.parse(it) }.getOrNull() },
    )
}.getOrNull()
