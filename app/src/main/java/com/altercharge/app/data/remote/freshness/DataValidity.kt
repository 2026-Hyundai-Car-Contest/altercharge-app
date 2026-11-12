/**
 * 받아 둔 충전소 정보가 아직 믿을 만한지 판단한다 (REQ_CM_009 유효 기간, REQ_CM_006 오프라인 표시).
 *
 * - 가용 대수 · 운영중지 · 점검 상태: 15분
 * - 이름 · 위치 · 충전기 타입 · 출력 · 방문 횟수: 7일
 *
 * 기준 시각은 "마지막으로 충전소 정보를 받은 시각"(서버 fetchedAt 또는 응답을 받은 때)이고,
 * L1 · R1 · R2의 "N분 전 기준" 표시도 같은 값을 쓴다 (디자이너 확정 2026-11-12).
 * 유효 기간이 지난 값은 R1 · R2가 "정보 없음"으로 바꾼다 (Figma v2 · availability = unknown).
 */
package com.altercharge.app.data.remote.freshness

import java.time.Duration
import java.time.Instant

/** 가용 대수 · 운영 상태 유효 기간 */
val AVAILABILITY_VALIDITY: Duration = Duration.ofMinutes(15)

/** 이름 · 위치 · 충전기 타입 · 출력 · 방문 횟수 유효 기간 */
val STATION_INFO_VALIDITY: Duration = Duration.ofDays(7)

enum class Validity {
    /** 유효 기간 안. 그대로 보여 준다 */
    VALID,

    /** 유효 기간이 지났다. "정보 없음"으로 보여 준다 */
    EXPIRED,

    /** 받아 둔 정보가 아예 없다 (첫 실행 · 저장 전). 충전소 영역은 빈 상태 */
    NONE,
}

/**
 * [receivedAt]에 받은 값이 [now] 기준으로 [period] 안에 있는지.
 * 기기 시계가 어긋나 받은 시각이 미래면 유효로 본다 (lastCheckedOf와 같은 처리).
 */
fun validityOf(receivedAt: Instant?, now: Instant, period: Duration): Validity = when {
    receivedAt == null -> Validity.NONE
    Duration.between(receivedAt, now) <= period -> Validity.VALID
    else -> Validity.EXPIRED
}

/** 충전소 한 곳(또는 목록)의 두 가지 유효성. 15분이 지나도 이름 · 거리는 7일까지 그대로다 */
data class StationValidity(
    /** 가용 대수 · 운영 상태 (15분) */
    val availability: Validity,
    /** 이름 · 위치 · 타입 · 출력 · 방문 횟수 (7일) */
    val info: Validity,
) {
    /** R1 · R2가 가용 대수 자리에 "정보 없음"을 보여 줄지 */
    val showAvailabilityUnknown: Boolean get() = availability == Validity.EXPIRED

    /** 받아 둔 정보가 하나도 쓸 수 없어 충전소 영역을 빈 상태로 보여 줄지 */
    val isEmpty: Boolean get() = info != Validity.VALID
}

fun stationValidity(receivedAt: Instant?, now: Instant): StationValidity = StationValidity(
    availability = validityOf(receivedAt, now, AVAILABILITY_VALIDITY),
    info = validityOf(receivedAt, now, STATION_INFO_VALIDITY),
)

/**
 * 다시 요청할 때. 충전소 정보가 없거나 만료됐을 때 30초마다 자동으로 다시 묻고, 연결이 돌아오면 바로 묻는다 (CM_006 예외 · Figma R1 빈 상태).
 */
object RefreshRule {
    val AUTO_RETRY_INTERVAL: Duration = Duration.ofSeconds(30)

    /**
     * 지금 다시 요청해야 하는지.
     * @param lastRequestAt 마지막으로 요청한 시각. null이면 아직 한 번도 안 했다
     * @param reconnected 방금 연결이 돌아왔는지 (끊김 → 연결)
     */
    fun isDue(lastRequestAt: Instant?, now: Instant, reconnected: Boolean = false): Boolean = when {
        reconnected -> true
        lastRequestAt == null -> true
        else -> Duration.between(lastRequestAt, now) >= AUTO_RETRY_INTERVAL
    }
}
