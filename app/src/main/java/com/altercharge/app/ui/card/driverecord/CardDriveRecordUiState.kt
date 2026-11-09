package com.altercharge.app.ui.card.driverecord

import androidx.compose.runtime.Immutable
import java.time.LocalDate

/**
 * L2 주간 주행 기록 카드에 넘기는 값. Figma card_drive_record (366:38474).
 * 막대 차트 · 평균선 · 타일 4개(하루 평균, 주간 합계, 마지막 충전, 충전 후 주행)를 그린다.
 */
@Immutable
data class CardDriveRecordUiState(
    /** 보고 있는 주의 월요일 */
    val weekStart: LocalDate,
    /** 월~일 7칸 주행 km. 아직 안 온 날은 null */
    val dailyKm: List<Int?>,
    /** 오늘 칸 위치 (0 = 월). 지난 주를 보고 있으면 null */
    val todayIndex: Int?,
    val averageKm: Int,
    val totalKm: Int,
    /** 마지막 충전 후 지난 날 수 */
    val daysSinceLastCharge: Int?,
    /** 충전 후 주행 km */
    val kmSinceLastCharge: Int?,
    /** 지난 주보다 더 달린 km. 덜 달렸으면 음수 */
    val diffFromLastWeekKm: Int?,
    val hasPreviousWeek: Boolean,
    val hasNextWeek: Boolean,
    /** 펼침 상태 (상황 15) */
    val expanded: Boolean,
)
