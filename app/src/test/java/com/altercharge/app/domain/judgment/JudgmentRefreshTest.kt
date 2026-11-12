package com.altercharge.app.domain.judgment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/** REQ_JD_004: 그날 첫 실행 · 충전 완료 · 오늘 주행이 예비 거리를 넘을 때만 다시 계산 */
class JudgmentRefreshTest {

    private val today = LocalDate.of(2026, 11, 12)
    private val computedAt = Instant.parse("2026-11-12T00:00:00Z") // 한국 09:00

    private fun stamp(on: LocalDate = today, reserveKm: Int? = 35, todayKm: Int = 10) =
        JudgmentStamp(computedOn = on, computedAt = computedAt, reserveKm = reserveKm, todayKmAtCompute = todayKm)

    @Test
    fun 한_번도_계산하지_않았으면_계산() {
        assertEquals(RecomputeReason.NEVER_COMPUTED, recomputeReason(null, today, 0, null))
    }

    @Test
    fun 그날_첫_실행이면_계산() {
        assertEquals(RecomputeReason.FIRST_RUN_TODAY, recomputeReason(stamp(on = today.minusDays(1)), today, 0, null))
    }

    @Test
    fun 같은_날_아무_일_없으면_유지() {
        assertNull(recomputeReason(stamp(), today, todayKm = 20, lastChargeCompletedAt = null))
    }

    @Test
    fun 마지막_계산_뒤에_충전이_끝나면_계산() {
        assertEquals(
            RecomputeReason.CHARGE_COMPLETED,
            recomputeReason(stamp(), today, 20, lastChargeCompletedAt = computedAt.plusSeconds(60)),
        )
    }

    @Test
    fun 계산_전에_끝난_충전은_다시_계산하지_않는다() {
        assertNull(recomputeReason(stamp(), today, 20, lastChargeCompletedAt = computedAt.minusSeconds(60)))
    }

    @Test
    fun 오늘_주행이_예비_거리를_넘는_순간_계산() {
        assertNull(recomputeReason(stamp(reserveKm = 35, todayKm = 10), today, todayKm = 35, lastChargeCompletedAt = null))
        assertEquals(
            RecomputeReason.EXCEEDED_RESERVE,
            recomputeReason(stamp(reserveKm = 35, todayKm = 10), today, todayKm = 36, lastChargeCompletedAt = null),
        )
    }

    @Test
    fun 이미_넘은_뒤_계산했으면_더_달려도_유지() {
        assertNull(recomputeReason(stamp(reserveKm = 35, todayKm = 40), today, todayKm = 80, lastChargeCompletedAt = null))
    }

    @Test
    fun 기록_수집_중이면_예비_거리_조건은_없다() {
        assertNull(recomputeReason(stamp(reserveKm = null, todayKm = 0), today, todayKm = 500, lastChargeCompletedAt = null))
    }

    @Test
    fun 판정_결과로_계산_기록을_만든다() {
        val ready = JudgmentResult.Ready(Judgment(averageDailyKm = 40, reserveKm = 35, daysLeft = 3, chargeRecommended = false))
        assertEquals(JudgmentStamp(today, computedAt, 35, 12), ready.stamp(today, computedAt, todayKm = 12))

        val collecting = JudgmentResult.Collecting(recordedDays = 3, chargeRecommended = false)
        assertNull(collecting.stamp(today, computedAt, todayKm = 0).reserveKm)
    }
}
