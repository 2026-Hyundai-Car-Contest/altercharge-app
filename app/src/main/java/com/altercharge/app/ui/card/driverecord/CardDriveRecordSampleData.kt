package com.altercharge.app.ui.card.driverecord

import java.time.LocalDate

/**
 * L2 카드 샘플 값. 미리보기 · 디버그 카탈로그 · 화면 개발 중 임시 데이터로 쓴다. 실데이터가 아니다.
 */
internal object CardDriveRecordSampleData {
    /** 이번 주 (오늘 = 금, 토 · 일은 아직) */
    val ThisWeek = CardDriveRecordUiState(
        weekStart = LocalDate.of(2026, 11, 2),
        dailyKm = listOf(42, 36, 55, 31, 18, null, null),
        todayIndex = 4,
        averageKm = 37,
        totalKm = 182,
        daysSinceLastCharge = 3,
        kmSinceLastCharge = 120,
        diffFromLastWeekKm = 12,
        hasPreviousWeek = true,
        hasNextWeek = false,
        expanded = false,
    )

    /** 지난 주 (오늘 없음, 0km 요일 포함) */
    val LastWeek = CardDriveRecordUiState(
        weekStart = LocalDate.of(2026, 10, 26),
        dailyKm = listOf(28, 0, 47, 33, 52, 64, 12),
        todayIndex = null,
        averageKm = 34,
        totalKm = 236,
        daysSinceLastCharge = 3,
        kmSinceLastCharge = 120,
        diffFromLastWeekKm = -5,
        hasPreviousWeek = true,
        hasNextWeek = true,
        expanded = false,
    )

    /** 기록이 하나도 없는 첫날 (이번 주, 오늘 = 금) */
    val Empty = CardDriveRecordUiState(
        weekStart = LocalDate.of(2026, 11, 2),
        dailyKm = List(7) { null },
        todayIndex = 4,
        averageKm = 0,
        totalKm = 0,
        daysSinceLastCharge = null,
        kmSinceLastCharge = null,
        diffFromLastWeekKm = null,
        hasPreviousWeek = false,
        hasNextWeek = false,
        expanded = false,
        empty = true,
    )
}
