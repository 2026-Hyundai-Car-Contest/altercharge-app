/**
 * 판정을 언제 다시 계산할지 (REQ_JD_004 판정 갱신 시점).
 * 판정 표시가 하루 안에 수시로 바뀌지 않게, 아래 셋 중 하나일 때만 다시 계산하고 그 밖에는 마지막 판정을 유지한다.
 * - 그날 첫 실행 (마지막 계산이 오늘이 아니다)
 * - 충전 완료 (마지막 계산 뒤에 충전이 끝났다)
 * - 오늘 주행거리가 예비 거리를 넘었다 (넘는 순간 한 번만)
 *
 * judge()처럼 값만 받아 결과를 돌려주는 순수 함수다. 현재 시각 · 저장은 부르는 쪽이 넘긴다.
 */
package com.altercharge.app.domain.judgment

import com.altercharge.app.domain.time.KoreaZone
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 마지막으로 판정을 계산한 때의 기록. 계산할 때마다 새로 만들어 저장한다.
 *
 * @property computedOn 계산한 날짜 (한국시간)
 * @property computedAt 계산한 시각. 충전 완료가 이보다 뒤면 다시 계산한다
 * @property reserveKm 그때 판정의 예비 거리. 기록 수집 중이면 예비 거리가 없어 null
 * @property todayKmAtCompute 계산할 때까지 오늘 달린 거리
 */
data class JudgmentStamp(
    val computedOn: LocalDate,
    val computedAt: Instant,
    val reserveKm: Int?,
    val todayKmAtCompute: Int,
)

/** 다시 계산하는 이유. 로그 · 테스트에서 어떤 조건이 걸렸는지 보려고 둔다 */
enum class RecomputeReason { NEVER_COMPUTED, FIRST_RUN_TODAY, CHARGE_COMPLETED, EXCEEDED_RESERVE }

/**
 * 지금 판정을 다시 계산해야 하면 그 이유를, 아니면 null(마지막 판정 유지)을 돌려준다.
 *
 * @param last 마지막 계산 기록. 한 번도 계산하지 않았으면 null
 * @param today 오늘 날짜 (한국시간)
 * @param todayKm 오늘 지금까지 달린 거리
 * @param lastChargeCompletedAt 마지막 충전 완료 시각. 모르면 null
 */
fun recomputeReason(
    last: JudgmentStamp?,
    today: LocalDate,
    todayKm: Int,
    lastChargeCompletedAt: Instant?,
): RecomputeReason? = when {
    last == null -> RecomputeReason.NEVER_COMPUTED
    last.computedOn != today -> RecomputeReason.FIRST_RUN_TODAY
    lastChargeCompletedAt != null && lastChargeCompletedAt > last.computedAt -> RecomputeReason.CHARGE_COMPLETED
    // 넘는 순간 한 번만: 마지막 계산 때는 아직 안 넘었고 지금 넘었을 때
    last.reserveKm != null && todayKm > last.reserveKm && last.todayKmAtCompute <= last.reserveKm ->
        RecomputeReason.EXCEEDED_RESERVE
    else -> null
}

/** 판정 결과로 다음 비교에 쓸 계산 기록을 만든다. 날짜는 계산 시각에서 한국시간으로 구해 둘이 어긋나지 않게 한다 */
fun JudgmentResult.stamp(computedAt: Instant, todayKm: Int, zone: ZoneId = KoreaZone): JudgmentStamp = JudgmentStamp(
    computedOn = computedAt.atZone(zone).toLocalDate(),
    computedAt = computedAt,
    reserveKm = (this as? JudgmentResult.Ready)?.judgment?.reserveKm,
    todayKmAtCompute = todayKm,
)
