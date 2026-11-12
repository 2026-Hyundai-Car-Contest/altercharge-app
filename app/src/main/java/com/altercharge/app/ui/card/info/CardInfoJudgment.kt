package com.altercharge.app.ui.card.info

import com.altercharge.app.domain.judgment.Judgment
import com.altercharge.app.domain.judgment.JudgmentResult
import com.altercharge.app.ui.card.common.ChargeVerdict

/** 0일 오늘 · 1일 내일 · 2일 이상 N일내 (기획 확정 2026-11-12, JD_001) */
fun Judgment.toCardInfoUiState(placeName: String, placeLastChecked: LastChecked? = null): CardInfoUiState {
    val days = daysLeft.coerceAtLeast(0)
    val verdict = when {
        days == 0 -> ChargeVerdict.TODAY
        days <= LAST_DAY_WITHIN_TWO_DAYS -> ChargeVerdict.WITHIN_TWO_DAYS
        else -> ChargeVerdict.THREE_DAYS_OR_MORE
    }
    return CardInfoUiState(verdict = verdict, daysUntilCharge = days, placeName = placeName, placeLastChecked = placeLastChecked)
}

private const val LAST_DAY_WITHIN_TWO_DAYS = 2

fun JudgmentResult.toCardInfoUiState(placeName: String, placeLastChecked: LastChecked? = null): CardInfoUiState = when (this) {
    is JudgmentResult.Collecting -> CardInfoUiState.collecting(recordedDays)
    is JudgmentResult.Ready -> judgment.toCardInfoUiState(placeName, placeLastChecked)
}
