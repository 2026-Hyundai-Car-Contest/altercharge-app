package com.altercharge.app.domain.judgment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargeJudgmentTest {

    private fun input(rangeKm: Int, socPercent: Int = 80, days: List<Int>) =
        JudgmentInput(rangeKm = rangeKm, socPercent = socPercent, recentDailyKm = days)

    /** 기록이 14일 이상인 입력의 판정. 수집 중이면 테스트가 실패한다. */
    private fun ready(rangeKm: Int, socPercent: Int = 80, days: List<Int>): Judgment =
        (judge(input(rangeKm, socPercent, days)) as JudgmentResult.Ready).judgment

    private fun collecting(rangeKm: Int = 200, socPercent: Int = 80, days: List<Int>): JudgmentResult.Collecting =
        judge(input(rangeKm, socPercent, days)) as JudgmentResult.Collecting

    // ---- REQ_JD_002: 일 평균 · 예비 거리 ----

    @Test
    fun `일 평균은 운행하지 않은 날을 포함해 14로 나눈다`() {
        // 14일 x 40 = 560 → 560 / 14 = 40
        val result = ready(rangeKm = 300, days = List(14) { 40 })
        assertEquals(40, result.averageDailyKm)
    }

    @Test
    fun `14일보다 많으면 마지막 14일만 쓴다`() {
        val old = List(10) { 1000 }
        val recent = List(14) { 28 }
        val result = ready(rangeKm = 300, days = old + recent)
        assertEquals(28, result.averageDailyKm)
        assertEquals(30, result.reserveKm) // 중앙값 28 → 최소 30
    }

    @Test
    fun `예비 거리는 운행한 날의 중앙값이다`() {
        // 운행한 날: 30, 40, 50, 60, 70 → 중앙값 50
        val days = listOf(0, 30, 0, 40, 50, 0, 60, 70) + List(6) { 0 }
        assertEquals(50, ready(rangeKm = 300, days = days).reserveKm)
    }

    @Test
    fun `운행한 날이 짝수면 가운데 두 값의 평균`() {
        // 운행한 날: 40, 50, 60, 80 → (50 + 60) / 2 = 55
        val days = listOf(0, 40, 0, 50, 0, 60, 80) + List(7) { 0 }
        assertEquals(55, ready(rangeKm = 300, days = days).reserveKm)
    }

    @Test
    fun `예비 거리는 최소 30`() {
        val days = List(14) { 10 }
        assertEquals(30, ready(rangeKm = 300, days = days).reserveKm)
    }

    @Test
    fun `2주 동안 운행하지 않으면 예비 거리 30, 버틸 일수 상한`() {
        val result = ready(rangeKm = 300, days = List(14) { 0 })
        assertEquals(0, result.averageDailyKm)
        assertEquals(30, result.reserveKm)
        assertEquals(MAX_DAYS_LEFT, result.daysLeft)
    }

    // ---- REQ_JD_001: 버틸 일수 ----

    @Test
    fun `버틸 일수는 소수점을 버린다`() {
        // 14일 x 30km → 평균 30, 예비 30. (100 - 30) / 30 = 2.33 → 2
        val result = ready(rangeKm = 100, days = List(14) { 30 })
        assertEquals(2, result.daysLeft)
    }

    @Test
    fun `주행가능거리가 예비 거리보다 적으면 0`() {
        val result = ready(rangeKm = 20, days = List(14) { 30 })
        assertEquals(0, result.daysLeft)
    }

    @Test
    fun `음수 입력은 0으로 본다`() {
        val result = ready(rangeKm = -10, socPercent = 50, days = listOf(-5) + List(13) { 30 })
        assertEquals(0, result.daysLeft)
    }

    // ---- REQ_ST_005: 충전 권장 ----

    @Test
    fun `버틸 일수 1 이하면 충전 권장`() {
        // 평균 40, 예비 40 → (110 - 40) / 40 = 1.75 → 1
        val result = ready(rangeKm = 110, socPercent = 80, days = List(14) { 40 })
        assertEquals(1, result.daysLeft)
        assertTrue(result.chargeRecommended)
    }

    @Test
    fun `배터리 30퍼센트 이하면 버틸 일수가 많아도 충전 권장`() {
        val result = ready(rangeKm = 400, socPercent = 30, days = List(14) { 20 })
        assertTrue(result.daysLeft > 1)
        assertTrue(result.chargeRecommended)
    }

    @Test
    fun `버틸 일수 2 이상이고 배터리 31퍼센트 이상이면 출발 준비 완료`() {
        val result = ready(rangeKm = 400, socPercent = 31, days = List(14) { 20 })
        assertFalse(result.chargeRecommended)
    }

    // ---- 경계값 ----

    @Test
    fun `딱 나누어떨어지는 경계에서 하루 모자라지 않는다`() {
        // 합 9 → 평균 9/14. 예비 30, 주행가능 39 → 거리 9. 9 ÷ (9/14) = 정확히 14일
        val days = listOf(9) + List(13) { 0 }
        assertEquals(14, ready(rangeKm = 39, days = days).daysLeft)
    }

    @Test
    fun `버틸 일수 1은 충전 권장, 2는 아니다`() {
        // 평균 40, 예비 40
        val days = List(14) { 40 }
        val one = ready(rangeKm = 119, socPercent = 80, days = days) // 79 / 40 = 1
        val two = ready(rangeKm = 120, socPercent = 80, days = days) // 80 / 40 = 2
        assertEquals(1, one.daysLeft)
        assertTrue(one.chargeRecommended)
        assertEquals(2, two.daysLeft)
        assertFalse(two.chargeRecommended)
    }

    @Test
    fun `기록 14일이 모두 0이면 버틸 일수 상한`() {
        val result = ready(rangeKm = 200, days = List(14) { 0 })
        assertEquals(MAX_DAYS_LEFT, result.daysLeft)
        assertEquals(30, result.reserveKm)
    }

    // ---- REQ_JD_002 예외: 기록 14일 미만은 수집 중 ----

    @Test
    fun `기록이 14일 미만이면 수집 중이고 기록 일수를 돌려준다`() {
        assertEquals(0, collecting(days = emptyList()).recordedDays)
        assertEquals(3, collecting(days = List(3) { 40 }).recordedDays)
        assertEquals(13, collecting(days = List(13) { 40 }).recordedDays)
    }

    @Test
    fun `기록 13일은 수집 중, 14일부터 판정한다`() {
        assertTrue(judge(input(rangeKm = 300, days = List(13) { 70 })) is JudgmentResult.Collecting)
        // 14일 x 70 = 980 → 980 / 14 = 70
        assertEquals(70, ready(rangeKm = 300, days = List(14) { 70 }).averageDailyKm)
    }

    @Test
    fun `수집 중에는 배터리 30퍼센트 이하일 때만 충전 권장`() {
        // 주행가능거리가 아주 짧아도 버틸 일수로는 판단하지 않는다 (REQ_ST_005 예외)
        assertFalse(collecting(rangeKm = 10, socPercent = 31, days = List(5) { 40 }).chargeRecommended)
        assertTrue(collecting(rangeKm = 500, socPercent = 30, days = List(5) { 40 }).chargeRecommended)
        assertTrue(collecting(rangeKm = 500, socPercent = 0, days = emptyList()).chargeRecommended)
    }

    @Test
    fun `수집 중 음수 기록도 일수로 센다`() {
        assertEquals(2, collecting(days = listOf(-5, 30)).recordedDays)
    }

    @Test
    fun `기록이 14일보다 많아도 수집 중이 아니다`() {
        assertTrue(judge(input(rangeKm = 300, days = List(20) { 40 })) is JudgmentResult.Ready)
    }
}
