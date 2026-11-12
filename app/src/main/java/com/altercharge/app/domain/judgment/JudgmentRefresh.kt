/**
 * 판정을 언제 다시 계산할지 (REQ_JD_004). 하루 안에 판정이 수시로 바뀌지 않게
 * 그날 첫 실행 · 충전 완료 · 오늘 주행이 예비 거리를 넘는 순간에만 다시 계산한다.
 */
package com.altercharge.app.domain.judgment

import com.altercharge.app.domain.time.KoreaZone
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 마지막 계산 기록. reserveKm은 기록 수집 중이면 null */
data class JudgmentStamp(
    val computedOn: LocalDate,
    val computedAt: Instant,
    val reserveKm: Int?,
    val todayKmAtCompute: Int,
)

enum class RecomputeReason { NEVER_COMPUTED, FIRST_RUN_TODAY, CHARGE_COMPLETED, EXCEEDED_RESERVE }

/** 다시 계산할 이유. null이면 마지막 판정 유지 */
fun recomputeReason(
    last: JudgmentStamp?,
    today: LocalDate,
    todayKm: Int,
    lastChargeCompletedAt: Instant?,
): RecomputeReason? = when {
    last == null -> RecomputeReason.NEVER_COMPUTED
    last.computedOn != today -> RecomputeReason.FIRST_RUN_TODAY
    lastChargeCompletedAt != null && lastChargeCompletedAt > last.computedAt -> RecomputeReason.CHARGE_COMPLETED
    // 이미 넘은 뒤 계산했으면 더 달려도 다시 계산하지 않는다
    last.reserveKm != null && todayKm > last.reserveKm && last.todayKmAtCompute <= last.reserveKm ->
        RecomputeReason.EXCEEDED_RESERVE
    else -> null
}

/** 날짜를 계산 시각에서 구해 둘이 어긋나지 않게 한다 */
fun JudgmentResult.stamp(computedAt: Instant, todayKm: Int, zone: ZoneId = KoreaZone): JudgmentStamp = JudgmentStamp(
    computedOn = computedAt.atZone(zone).toLocalDate(),
    computedAt = computedAt,
    reserveKm = (this as? JudgmentResult.Ready)?.judgment?.reserveKm,
    todayKmAtCompute = todayKm,
)
