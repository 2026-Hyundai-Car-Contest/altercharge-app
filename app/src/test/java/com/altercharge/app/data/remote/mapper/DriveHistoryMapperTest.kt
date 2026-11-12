package com.altercharge.app.data.remote.mapper

import com.altercharge.app.data.remote.model.LatestChargeHistory
import com.altercharge.app.data.remote.model.VehicleSnapshotHistory
import com.altercharge.app.data.remote.model.VehicleSnapshotRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class DriveHistoryMapperTest {

    private val zone: ZoneId = ZoneId.of("Asia/Seoul")

    /** 오늘 = 2026-11-06 (금). 샘플 L2 "이번 주 11.02 ~ 11.08"과 같은 주 */
    private val today: LocalDate = LocalDate.of(2026, 11, 6)

    private fun at(date: LocalDate, time: LocalTime): Instant = date.atTime(time).atZone(zone).toInstant()

    private fun record(date: LocalDate, time: LocalTime, odometer: Double) =
        VehicleSnapshotRecord("s", at(date, time), socPercent = 70, odometerKm = odometer, drivableRangeKm = null)

    private fun history(vararg records: VehicleSnapshotRecord) =
        VehicleSnapshotHistory("v", records.toList(), retentionDays = 56, completeDayCount = 0)

    /** 날짜마다 아침 · 저녁 두 건. 누적거리는 전날 저녁 값에서 km만큼 늘어난다 */
    private fun daysOf(start: LocalDate, kms: List<Int>, startOdometer: Double = 1000.0): VehicleSnapshotHistory {
        var odometer = startOdometer
        val records = kms.flatMapIndexed { i, km ->
            val date = start.plusDays(i.toLong())
            listOf(record(date, LocalTime.of(7, 0), odometer), record(date, LocalTime.of(22, 0), odometer + km)).also { odometer += km }
        }
        return history(*records.toTypedArray())
    }

    // ---- dailyDistances ----

    @Test
    fun 기록이_없으면_빈_목록() {
        assertEquals(emptyList<DailyDistance>(), history().dailyDistances(today, zone))
    }

    @Test
    fun 하루_주행은_그날_마지막_누적거리에서_전날_마지막을_뺀_값() {
        val h = daysOf(LocalDate.of(2026, 11, 4), listOf(10, 20, 30))
        assertEquals(listOf(10, 20, 30), h.dailyDistances(today, zone).map { it.km })
    }

    @Test
    fun 첫날은_그날_첫_기록부터_센다() {
        val h = history(record(today, LocalTime.of(9, 0), 500.0), record(today, LocalTime.of(18, 0), 542.0))
        assertEquals(listOf(DailyDistance(today, 42)), h.dailyDistances(today, zone))
    }

    @Test
    fun 기록_없는_날은_0이고_늘어난_거리는_다음_기록_날에_들어간다() {
        val h = history(
            record(LocalDate.of(2026, 11, 3), LocalTime.of(22, 0), 100.0),
            record(LocalDate.of(2026, 11, 5), LocalTime.of(22, 0), 160.0),
        )
        val kms = h.dailyDistances(today, zone)
        assertEquals(listOf(0, 0, 60, 0), kms.map { it.km })
        assertEquals(LocalDate.of(2026, 11, 3), kms.first().date)
        assertEquals(today, kms.last().date) // 오늘까지 채운다
    }

    @Test
    fun 누적거리가_줄어든_날은_0() {
        val h = history(
            record(LocalDate.of(2026, 11, 5), LocalTime.of(22, 0), 100.0),
            record(today, LocalTime.of(22, 0), 90.0),
        )
        assertEquals(listOf(0, 0), h.dailyDistances(today, zone).map { it.km })
    }

    @Test
    fun 한국시간_자정_직전_기록은_그날_것() {
        // UTC 14:59 = 한국 23:59
        val h = history(
            record(LocalDate.of(2026, 11, 5), LocalTime.of(7, 0), 0.0),
            VehicleSnapshotRecord("s", Instant.parse("2026-11-05T14:59:00Z"), 70, 30.0, null),
        )
        assertEquals(listOf(30, 0), h.dailyDistances(today, zone).map { it.km })
    }

    // ---- todayKm ----

    @Test
    fun 오늘_주행은_지금_누적거리에서_어제_마지막_기록을_뺀다() {
        val h = history(
            record(today.minusDays(1), LocalTime.of(22, 0), 1000.0),
            record(today, LocalTime.of(8, 0), 1010.0),
        )
        assertEquals(37, h.todayKm(currentOdometerKm = 1037.2, today = today, zone = zone))
    }

    @Test
    fun 오늘_처음_기록했으면_오늘_첫_기록부터() {
        val h = history(record(today, LocalTime.of(8, 0), 500.0))
        assertEquals(12, h.todayKm(512.0, today, zone))
    }

    @Test
    fun 기록이_없으면_오늘_주행은_0() {
        assertEquals(0, history().todayKm(1234.0, today, zone))
    }

    // ---- toJudgmentInput ----

    @Test
    fun 판정_입력은_최근_14일만() {
        val h = daysOf(today.minusDays(19), List(20) { it + 1 }) // 1 … 20
        val input = h.toJudgmentInput(rangeKm = 300, socPercent = 80, today = today, zone = zone)
        assertEquals((7..20).toList(), input.recentDailyKm)
        assertEquals(300, input.rangeKm)
        assertEquals(80, input.socPercent)
    }

    // ---- toCardDriveRecordUiState ----

    private val weekStart = LocalDate.of(2026, 11, 2)

    @Test
    fun 이번_주_카드_값() {
        // 10.30(금)부터 기록. 이번 주 월~금 42 36 55 31 18, 토·일은 아직
        val h = daysOf(LocalDate.of(2026, 10, 30), listOf(5, 7, 9, 42, 36, 55, 31, 18))
        val s = h.toCardDriveRecordUiState(weekStart, today, zone = zone)
        assertEquals(listOf(42, 36, 55, 31, 18, null, null), s.dailyKm)
        assertEquals(4, s.todayIndex)
        assertEquals(182, s.totalKm)
        assertEquals(36, s.averageKm) // 182 / 5 = 36.4
        assertEquals(182 - 5, s.diffFromLastWeekKm) // 같은 요일(월~금)끼리: 지난주는 금요일 5만 기록이 있다
        assertTrue(s.hasPreviousWeek)
        assertFalse(s.hasNextWeek)
        assertFalse(s.empty)
    }

    @Test
    fun 지난주_대비는_같은_요일까지만_비교한다() {
        // 지난주 월~일 10씩(70), 이번 주 월~수 20씩. 오늘 = 수요일
        val wednesday = LocalDate.of(2026, 11, 4)
        val h = daysOf(LocalDate.of(2026, 10, 26), List(7) { 10 } + List(3) { 20 })
        val s = h.toCardDriveRecordUiState(weekStart, wednesday, zone = zone)
        assertEquals(60 - 30, s.diffFromLastWeekKm) // 지난주 월~수 30과 비교 (70 아님)
    }

    @Test
    fun 지난_주_카드는_오늘_칸이_없고_다음_주로_갈_수_있다() {
        val h = daysOf(LocalDate.of(2026, 10, 26), listOf(28, 0, 47, 33, 52, 64, 12, 42, 36, 55, 31, 18))
        val s = h.toCardDriveRecordUiState(weekStart.minusWeeks(1), today, zone = zone)
        assertEquals(listOf(28, 0, 47, 33, 52, 64, 12), s.dailyKm)
        assertNull(s.todayIndex)
        assertEquals(236, s.totalKm)
        assertEquals(34, s.averageKm)
        assertNull(s.diffFromLastWeekKm) // 그 전 주 기록 없음
        assertFalse(s.hasPreviousWeek)
        assertTrue(s.hasNextWeek)
    }

    @Test
    fun 첫_기록_전_날은_null이고_기록이_있으면_그_사이_빈_날은_0() {
        // 수요일 첫 기록, 목요일 기록 없음, 금요일(오늘) 기록
        val h = history(
            record(LocalDate.of(2026, 11, 4), LocalTime.of(9, 0), 0.0),
            record(LocalDate.of(2026, 11, 4), LocalTime.of(20, 0), 12.0),
            record(today, LocalTime.of(20, 0), 30.0),
        )
        val s = h.toCardDriveRecordUiState(weekStart, today, zone = zone)
        assertEquals(listOf(null, null, 12, 0, 18, null, null), s.dailyKm)
        assertFalse(s.hasPreviousWeek)
    }

    @Test
    fun 이번_주_기록이_하나도_없으면_빈_상태() {
        val s = history().toCardDriveRecordUiState(weekStart, today, zone = zone)
        assertTrue(s.empty)
        assertEquals(List(7) { null }, s.dailyKm)
        assertEquals(4, s.todayIndex)
        assertEquals(0, s.averageKm)
        assertNull(s.daysSinceLastCharge)
        assertFalse(s.hasPreviousWeek)
        assertFalse(s.hasNextWeek)
    }

    @Test
    fun 마지막_충전은_완료_시각과_그_뒤_주행거리에서() {
        val charge = LatestChargeHistory(
            vehicleId = "v",
            lastChargeCompletedAt = at(LocalDate.of(2026, 11, 3), LocalTime.of(21, 40)),
            odometerAtLastChargeKm = 1000.0,
            currentOdometerKm = 1120.4,
            distanceSinceChargeKm = 120.4,
            trusted = false,
        )
        val s = history().toCardDriveRecordUiState(weekStart, today, latestCharge = charge, zone = zone)
        assertEquals(3, s.daysSinceLastCharge)
        assertEquals(120, s.kmSinceLastCharge)
    }

    @Test
    fun 충전_완료_이벤트가_없으면_마지막_충전_값은_null() {
        val charge = LatestChargeHistory("v", null, null, 1120.0, null, trusted = false)
        val s = history().toCardDriveRecordUiState(weekStart, today, latestCharge = charge, zone = zone)
        assertNull(s.daysSinceLastCharge)
        assertNull(s.kmSinceLastCharge)
    }

    @Test
    fun 주_시작은_월요일() {
        assertEquals(weekStart, weekStartOf(today))
        assertEquals(weekStart, weekStartOf(weekStart))
        assertEquals(weekStart, weekStartOf(LocalDate.of(2026, 11, 8)))
    }
}
