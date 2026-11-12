/**
 * 오프라인에서 보여 줄 마지막 값을 기기에 저장한다 (REQ_CM_006: 연결이 끊기면 받아 둔 마지막 값을 보여 준다).
 *
 * 저장하는 것
 * - 충전 판정 결과 (L1 · C 카드). 판정은 오프라인에서도 마지막 값을 유지한다
 * - 판정 계산 기록 (REQ_JD_004: 앱을 다시 켜도 같은 날이면 판정을 다시 계산하지 않게)
 * - 추천 충전소 이름 · 거리 (L1 장소 줄 · C 가까운 충전소)
 * - 충전소 정보를 받은 시각 ("N분 전 기준", 15분 · 7일 유효 기간의 기준값)
 *
 * 저장소는 [KeyValueStore]로 감싸 SharedPreferences(앱)와 메모리(테스트)를 바꿔 끼운다. 새 라이브러리를 쓰지 않는다.
 */
package com.altercharge.app.data.local

import android.content.Context
import com.altercharge.app.domain.judgment.Judgment
import com.altercharge.app.domain.judgment.JudgmentResult
import com.altercharge.app.domain.judgment.JudgmentStamp
import java.time.Instant
import java.time.LocalDate

/** 오프라인용 마지막 값 묶음. 저장 전이면 각 값이 null이다 */
data class OfflineSnapshot(
    val judgment: JudgmentResult?,
    /** 마지막 판정 계산 기록. recomputeReason(last = 이 값, …)에 넘긴다 */
    val judgmentStamp: JudgmentStamp?,
    /** 추천 충전소 이름. 반경 안에 없었으면 빈 문자열 */
    val placeName: String?,
    /** 추천 충전소까지 거리 km */
    val nearestStationKm: Double?,
    /** 충전소 정보를 받은 시각 */
    val stationReceivedAt: Instant?,
)

/** 문자열 키-값 저장소. 앱은 SharedPreferences, 테스트는 메모리 맵을 쓴다 */
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

    /** 판정 결과와 그 계산 기록을 한 번에 저장한다. 판정을 다시 계산할 때마다 부른다 */
    fun saveJudgment(result: JudgmentResult, stamp: JudgmentStamp) {
        store.putStrings(mapOf(KEY_JUDGMENT to encodeJudgment(result), KEY_JUDGMENT_STAMP to encodeStamp(stamp)))
    }

    /**
     * 추천 충전소와 받은 시각을 같이 저장한다. 서버 응답을 받을 때마다 부른다.
     * @param receivedAt 응답을 받은 기기 시각 (Instant.now()). 서버 statusObservedAt이 아니다 — "N분 전 기준"과 15분 · 7일 유효 기간의 기준값
     */
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

// 판정 결과는 필드가 몇 개뿐이라 "종류|값|값…" 한 줄로 저장한다. 형식이 깨져 있으면 저장 전처럼 null로 읽는다.

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

/** 계산 기록: "날짜|시각|예비 거리(없으면 빈칸)|오늘 주행" */
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
