/**
 * 받아 둔 충전소 정보를 믿을 수 있는지 (REQ_CM_009). 가용 대수 · 운영 상태는 15분, 나머지는 7일.
 * 기준 시각은 "N분 전 기준" 표시와 같은 "받은 시각"이다.
 */
package com.altercharge.app.data.remote.freshness

import java.time.Duration
import java.time.Instant

val AVAILABILITY_VALIDITY: Duration = Duration.ofMinutes(15)
val STATION_INFO_VALIDITY: Duration = Duration.ofDays(7)

enum class Validity { VALID, EXPIRED, NONE }

/** 받은 시각이 미래면(기기 시계 어긋남) 유효로 본다 */
fun validityOf(receivedAt: Instant?, now: Instant, period: Duration): Validity = when {
    receivedAt == null -> Validity.NONE
    Duration.between(receivedAt, now) <= period -> Validity.VALID
    else -> Validity.EXPIRED
}

data class StationValidity(val availability: Validity, val info: Validity) {
    /** R1 · R2 가용 대수 자리에 "정보 없음" */
    val showAvailabilityUnknown: Boolean get() = availability == Validity.EXPIRED

    /** 충전소 영역 빈 상태 */
    val isEmpty: Boolean get() = info != Validity.VALID
}

fun stationValidity(receivedAt: Instant?, now: Instant): StationValidity = StationValidity(
    availability = validityOf(receivedAt, now, AVAILABILITY_VALIDITY),
    info = validityOf(receivedAt, now, STATION_INFO_VALIDITY),
)

/** 서버에 못 붙어 저장값을 보여 주는 동안 다시 요청하는 간격 (REQ_CM_006). 홈 ViewModel이 쓴다 */
object RefreshRule {
    val AUTO_RETRY_INTERVAL: Duration = Duration.ofSeconds(30)
}
