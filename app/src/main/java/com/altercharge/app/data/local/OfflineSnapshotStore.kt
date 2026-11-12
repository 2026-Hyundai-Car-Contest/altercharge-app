/**
 * 연결이 끊겨도 마지막 값을 보여 주려고 기기에 저장한다 (REQ_CM_006).
 * 판정 계산 기록도 저장해야 앱을 다시 켜도 같은 날 판정이 유지된다 (REQ_JD_004).
 */
package com.altercharge.app.data.local

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
)

/** 테스트에서 메모리로 바꿔 끼우려고 SharedPreferences를 감싼다 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putStrings(values: Map<String, String?>)
}

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

    /** receivedAt은 응답을 받은 기기 시각이다 (서버 statusObservedAt 아님) */
    fun saveStation(placeName: String, nearestStationKm: Double?, receivedAt: Instant) {
        store.putStrings(
            mapOf(
                KEY_PLACE_NAME to placeName,
                KEY_NEAREST_KM to nearestStationKm?.toString(),
                KEY_STATION_RECEIVED_AT to receivedAt.toString(),
            ),
        )
    }

    fun load(): OfflineSnapshot = OfflineSnapshot(
        judgment = store.getString(KEY_JUDGMENT)?.let(::decodeJudgment),
        judgmentStamp = store.getString(KEY_JUDGMENT_STAMP)?.let(::decodeStamp),
        placeName = store.getString(KEY_PLACE_NAME),
        nearestStationKm = store.getString(KEY_NEAREST_KM)?.toDoubleOrNull(),
        stationReceivedAt = store.getString(KEY_STATION_RECEIVED_AT)?.let { runCatching { Instant.parse(it) }.getOrNull() },
    )

    private companion object {
        const val KEY_JUDGMENT = "judgment"
        const val KEY_JUDGMENT_STAMP = "judgment_stamp"
        const val KEY_PLACE_NAME = "place_name"
        const val KEY_NEAREST_KM = "nearest_station_km"
        const val KEY_STATION_RECEIVED_AT = "station_received_at"
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
