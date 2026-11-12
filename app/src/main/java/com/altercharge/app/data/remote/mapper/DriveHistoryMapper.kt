/** 서버 주행 기록 → 판정 입력 · L2 카드 값. zone · today는 테스트에서 고정하려고 인자로 받는다 */
package com.altercharge.app.data.remote.mapper

import com.altercharge.app.data.remote.model.LatestChargeHistory
import com.altercharge.app.data.remote.model.VehicleSnapshotHistory
import com.altercharge.app.domain.judgment.JudgmentInput
import com.altercharge.app.domain.judgment.WINDOW_DAYS
import com.altercharge.app.domain.time.KoreaZone
import com.altercharge.app.ui.card.driverecord.CardDriveRecordUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

data class DailyDistance(val date: LocalDate, val km: Int)

/**
 * 첫 기록 날부터 오늘까지 날짜별 주행거리.
 * 기록이 없는 날은 0이고, 그 사이 늘어난 거리는 다음 기록 날에 들어간다 (언제 달렸는지 알 수 없어서).
 * 누적거리가 줄어든 날(계기 초기화 등)은 0.
 */
fun VehicleSnapshotHistory.dailyDistances(today: LocalDate, zone: ZoneId = KoreaZone): List<DailyDistance> {
    if (records.isEmpty()) return emptyList()
    val byDate = records
        .sortedBy { it.recordedAt }
        .groupBy { it.recordedAt.atZone(zone).toLocalDate() }
    val firstDate = byDate.keys.min()
    val lastDate = maxOf(today, byDate.keys.max())

    var previousEnd = byDate.getValue(firstDate).first().odometerKm
    return generateSequence(firstDate) { it.plusDays(1) }
        .takeWhile { it <= lastDate }
        .map { date ->
            val dayRecords = byDate[date]
            if (dayRecords == null) {
                DailyDistance(date, 0)
            } else {
                val end = dayRecords.last().odometerKm
                val km = (end - previousEnd).roundToInt().coerceAtLeast(0)
                previousEnd = end
                DailyDistance(date, km)
            }
        }
        .toList()
}

/** 서버 기록은 다음 전송 전까지 늘지 않아서, 차량 SDK의 지금 누적거리로 계산한다 (REQ_JD_004) */
fun VehicleSnapshotHistory.todayKm(currentOdometerKm: Double, today: LocalDate, zone: ZoneId = KoreaZone): Int {
    val sorted = records.sortedBy { it.recordedAt }
    val baseline = sorted.lastOrNull { it.recordedAt.atZone(zone).toLocalDate() < today }
        ?: sorted.firstOrNull { it.recordedAt.atZone(zone).toLocalDate() == today }
        ?: return 0
    return (currentOdometerKm - baseline.odometerKm).roundToInt().coerceAtLeast(0)
}

/**
 * 충전 판정 입력. 어제까지 14일만 넣는다 (기획 확정 2026-11-12):
 * 오늘은 아직 안 끝나 아침엔 0km에 가까워 평균을 낮춘다. 오늘 주행은 REQ_JD_004 재계산으로 반영된다.
 */
fun VehicleSnapshotHistory.toJudgmentInput(
    rangeKm: Int,
    socPercent: Int,
    today: LocalDate,
    zone: ZoneId = KoreaZone,
): JudgmentInput = JudgmentInput(
    rangeKm = rangeKm,
    socPercent = socPercent,
    recentDailyKm = dailyDistances(today, zone).filter { it.date < today }.takeLast(WINDOW_DAYS).map { it.km },
)

fun weekStartOf(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/** 아직 안 온 날과 첫 기록 전 날은 null (막대 없음) */
fun VehicleSnapshotHistory.toCardDriveRecordUiState(
    weekStart: LocalDate,
    today: LocalDate,
    latestCharge: LatestChargeHistory? = null,
    expanded: Boolean = false,
    zone: ZoneId = KoreaZone,
): CardDriveRecordUiState {
    val kmByDate = dailyDistances(today, zone).associate { it.date to it.km }
    val firstDate = kmByDate.keys.minOrNull()

    fun kmOn(date: LocalDate): Int? = when {
        date > today -> null
        firstDate == null || date < firstDate -> null
        else -> kmByDate[date] ?: 0
    }

    val weekDates = (0L until 7L).map { weekStart.plusDays(it) }
    val dailyKm = weekDates.map(::kmOn)
    val values = dailyKm.filterNotNull()
    val totalKm = values.sum()
    // 지난주 7일 전체와 비교하면 주 초에는 늘 "덜 달렸어요"가 되므로 같은 요일까지만 비교한다
    val previousWeekValues = weekDates.indices
        .filter { dailyKm[it] != null }
        .mapNotNull { kmOn(weekDates[it].minusWeeks(1)) }

    val lastChargeDate = latestCharge?.lastChargeCompletedAt?.atZone(zone)?.toLocalDate()

    return CardDriveRecordUiState(
        weekStart = weekStart,
        dailyKm = dailyKm,
        todayIndex = weekDates.indexOf(today).takeIf { it >= 0 },
        averageKm = if (values.isEmpty()) 0 else (totalKm.toDouble() / values.size).roundToInt(),
        totalKm = totalKm,
        daysSinceLastCharge = lastChargeDate?.let { ChronoUnit.DAYS.between(it, today).toInt().coerceAtLeast(0) },
        kmSinceLastCharge = latestCharge?.distanceSinceChargeKm?.roundToInt(),
        diffFromLastWeekKm = if (previousWeekValues.isEmpty()) null else totalKm - previousWeekValues.sum(),
        hasPreviousWeek = firstDate != null && firstDate < weekStart,
        hasNextWeek = weekStart < weekStartOf(today),
        expanded = expanded,
        empty = values.isEmpty(),
    )
}
