/**
 * 서버 주행 기록(차량 스냅샷)을 충전 판정 입력과 L2 주간 주행 기록 카드 값으로 바꾼다.
 * 날짜는 한국시간(KoreaZone, 서버 보관 기준과 같다)으로 자르고, 테스트에서 고정하려고 zone · today를 인자로 받는다.
 */
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

/** 하루 주행거리. 기록이 없는 날은 0 */
data class DailyDistance(val date: LocalDate, val km: Int)

/**
 * 스냅샷을 날짜별 주행거리로 바꾼다. 첫 기록 날부터 [today]까지 하루도 빠짐없이 돌려준다.
 *
 * - 하루 주행거리 = 그날 마지막 누적거리 − 그 전 기록이 있는 날의 마지막 누적거리
 * - 첫 기록 날은 그날 마지막 − 그날 첫 누적거리 (앱 설치 전 주행은 모른다)
 * - 기록이 없는 날은 0. 그 사이 늘어난 거리는 다음 기록이 있는 날에 들어간다 (언제 달렸는지 알 수 없어서)
 * - 누적거리가 줄어든 날(계기 초기화 등)은 0
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

/**
 * 오늘 지금까지 달린 거리 (REQ_JD_004 "오늘 주행거리가 예비 거리를 넘었을 때"에 쓴다).
 * 서버 기록은 다음 스냅샷을 올려야 늘어나므로, 차량 SDK의 지금 누적거리([currentOdometerKm])로 계산한다.
 *
 * - 어제까지 마지막 기록이 있으면: 지금 누적거리 − 그 값
 * - 오늘 처음 기록을 시작했으면: 지금 누적거리 − 오늘 첫 기록
 * - 기록이 아예 없으면 0 (기준점이 없다)
 */
fun VehicleSnapshotHistory.todayKm(currentOdometerKm: Double, today: LocalDate, zone: ZoneId = KoreaZone): Int {
    val sorted = records.sortedBy { it.recordedAt }
    val baseline = sorted.lastOrNull { it.recordedAt.atZone(zone).toLocalDate() < today }
        ?: sorted.firstOrNull { it.recordedAt.atZone(zone).toLocalDate() == today }
        ?: return 0
    return (currentOdometerKm - baseline.odometerKm).roundToInt().coerceAtLeast(0)
}

/**
 * 충전 판정 입력. 최근 14일(오래된 날 → 오늘)만 담는다. 기록이 14일 미만이면 judge()가 수집 중으로 판정한다.
 * @param rangeKm 차량 주행가능거리 (차량 SDK)
 * @param socPercent 배터리 잔량 (차량 SDK)
 */
fun VehicleSnapshotHistory.toJudgmentInput(
    rangeKm: Int,
    socPercent: Int,
    today: LocalDate,
    zone: ZoneId = KoreaZone,
): JudgmentInput = JudgmentInput(
    rangeKm = rangeKm,
    socPercent = socPercent,
    recentDailyKm = dailyDistances(today, zone).takeLast(WINDOW_DAYS).map { it.km },
)

/** 날짜가 속한 주의 월요일 */
fun weekStartOf(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/**
 * L2 주간 주행 기록 카드 값 ([weekStart] 주).
 *
 * - 아직 안 온 날과 첫 기록 전 날은 null (막대 없음)
 * - 하루 평균 = 값이 있는 날의 합 ÷ 날 수 (반올림)
 * - 지난주 대비 = 이 주 합 − 지난주 같은 요일들의 합. 수요일이면 지난주 월~수와 비교한다
 *   (지난주 7일 전체와 비교하면 주 초에는 늘 "덜 달렸어요"가 된다). 지난주 그 요일들에 기록이 없으면 null
 * - 이전 주 버튼: 첫 기록이 이 주보다 앞이면 / 다음 주 버튼: 오늘이 속한 주보다 앞이면
 * - 마지막 충전은 [latestCharge]에서. 완료 이벤트가 없으면 null ("--")
 * - 이 주에 값이 하나도 없으면 빈 상태 (Figma state = empty)
 */
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
    // 이 주에 값이 있는 요일만 지난주에서 골라 같은 기간끼리 비교한다
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
