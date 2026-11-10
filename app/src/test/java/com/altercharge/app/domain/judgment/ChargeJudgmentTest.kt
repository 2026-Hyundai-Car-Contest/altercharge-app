package com.altercharge.app.domain.judgment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargeJudgmentTest {

    private fun input(rangeKm: Int, socPercent: Int = 80, days: List<Int>) =
        JudgmentInput(rangeKm = rangeKm, socPercent = socPercent, recentDailyKm = days)

    // ---- REQ_JD_002 예외: 기록 7일 미만 ----

    @Test
    fun `기록이 7일 미만이면 일 평균과 예비 거리 모두 40`() {
        val result = judge(input(rangeKm = 200, days = listOf(10, 20, 30, 40, 50, 60)))
        assertEquals(40, result.averageDailyKm)
        assertEquals(40, result.reserveKm)
        // (200 - 40) / 40 = 4
        assertEquals(4, result.daysLeft)
    }

    @Test
    fun `기록이 없어도 기본값으로 계산한다`() {
        val result = judge(input(rangeKm = 120, days = emptyList()))
        assertEquals(2, result.daysLeft) // (120 - 40) / 40
    }

    // ---- REQ_JD_002: 일 평균 · 예비 거리 ----

    @Test
    fun `일 평균은 운행하지 않은 날을 포함해 14로 나눈다`() {
        // 7일 기록, 합 280 → 280 / 14 = 20
        val result = judge(input(rangeKm = 300, days = listOf(40, 40, 40, 40, 40, 40, 40)))
        assertEquals(20, result.averageDailyKm)
    }

    @Test
    fun `14일보다 많으면 마지막 14일만 쓴다`() {
        val old = List(10) { 1000 }
        val recent = List(14) { 28 }
        val result = judge(input(rangeKm = 300, days = old + recent))
        assertEquals(28, result.averageDailyKm)
        assertEquals(30, result.reserveKm) // 중앙값 28 → 최소 30
    }

    @Test
    fun `예비 거리는 운행한 날의 중앙값이다`() {
        // 운행한 날: 30, 40, 50, 60, 70 → 중앙값 50
        val days = listOf(0, 30, 0, 40, 50, 0, 60, 70)
        assertEquals(50, judge(input(rangeKm = 300, days = days)).reserveKm)
    }

    @Test
    fun `운행한 날이 짝수면 가운데 두 값의 평균`() {
        // 운행한 날: 40, 50, 60, 80 → (50 + 60) / 2 = 55
        val days = listOf(0, 40, 0, 50, 0, 60, 80)
        assertEquals(55, judge(input(rangeKm = 300, days = days)).reserveKm)
    }

    @Test
    fun `예비 거리는 최소 30`() {
        val days = List(14) { 10 }
        assertEquals(30, judge(input(rangeKm = 300, days = days)).reserveKm)
    }

    @Test
    fun `2주 동안 운행하지 않으면 예비 거리 30, 버틸 일수 상한`() {
        val result = judge(input(rangeKm = 300, days = List(14) { 0 }))
        assertEquals(0, result.averageDailyKm)
        assertEquals(30, result.reserveKm)
        assertEquals(MAX_DAYS_LEFT, result.daysLeft)
    }

    // ---- REQ_JD_001: 버틸 일수 ----

    @Test
    fun `버틸 일수는 소수점을 버린다`() {
        // 14일 x 30km → 평균 30, 예비 30. (100 - 30) / 30 = 2.33 → 2
        val result = judge(input(rangeKm = 100, days = List(14) { 30 }))
        assertEquals(2, result.daysLeft)
    }

    @Test
    fun `주행가능거리가 예비 거리보다 적으면 0`() {
        val result = judge(input(rangeKm = 20, days = List(14) { 30 }))
        assertEquals(0, result.daysLeft)
    }

    @Test
    fun `음수 입력은 0으로 본다`() {
        val result = judge(input(rangeKm = -10, socPercent = 50, days = listOf(-5, 30, 30, 30, 30, 30, 30)))
        assertEquals(0, result.daysLeft)
    }

    // ---- REQ_ST_005: 충전 권장 ----

    @Test
    fun `버틸 일수 1 이하면 충전 권장`() {
        // 평균 40, 예비 40 → (110 - 40) / 40 = 1.75 → 1
        val result = judge(input(rangeKm = 110, socPercent = 80, days = List(14) { 40 }))
        assertEquals(1, result.daysLeft)
        assertTrue(result.chargeRecommended)
    }

    @Test
    fun `배터리 30퍼센트 이하면 버틸 일수가 많아도 충전 권장`() {
        val result = judge(input(rangeKm = 400, socPercent = 30, days = List(14) { 20 }))
        assertTrue(result.daysLeft > 1)
        assertTrue(result.chargeRecommended)
    }

    @Test
    fun `버틸 일수 2 이상이고 배터리 31퍼센트 이상이면 출발 준비 완료`() {
        val result = judge(input(rangeKm = 400, socPercent = 31, days = List(14) { 20 }))
        assertFalse(result.chargeRecommended)
    }
}
