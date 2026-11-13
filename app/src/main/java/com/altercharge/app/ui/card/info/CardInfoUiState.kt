package com.altercharge.app.ui.card.info

import androidx.compose.runtime.Immutable
import com.altercharge.app.ui.card.common.ChargeVerdict

/** L1 오늘 충전 판정. Figma card_info (366:38742). 문구는 카드가 리소스에서 만든다 */
@Immutable
data class CardInfoUiState(
    val verdict: ChargeVerdict,
    /** 0 = 오늘 */
    val daysUntilCharge: Int,
    val placeName: String,
    /** 기록 수집 중 일수 0~13. 값이 있으면 판정 값은 쓰지 않는다. [collecting]으로 만든다 */
    val recordedDays: Int? = null,
    /** 오프라인일 때 "N분 전 기준"(REQ_CM_006). null이면 온라인. [lastCheckedOf]로 만든다 */
    val placeLastChecked: LastChecked? = null,
) {
    companion object {
        /** Figma status = collecting. 판정 값은 쓰지 않는 자리 채움이다 */
        fun collecting(recordedDays: Int) = CardInfoUiState(
            verdict = ChargeVerdict.THREE_DAYS_OR_MORE,
            daysUntilCharge = 0,
            placeName = "",
            recordedDays = recordedDays.coerceIn(0, MAX_COLLECTING_DAYS),
        )
    }
}

/** REQ_JD_002 */
internal const val MAX_COLLECTING_DAYS = 13

internal const val COLLECTING_TOTAL_DAYS = 14

/**
 * 0일 오늘 · 1일 내일 · 2일 이상 N일내 (기획 확정 2026-11-12, JD_001).
 * 공통 ChargeVerdict와 단계가 달라 카드 안에서 바꾼다.
 * N일내 번개는 Figma status=3days variant 값을 쓴다(그래서 ModeSpec 이름이 threeDaysShift).
 */
internal enum class CardInfoStatus { TODAY, TOMORROW, WITHIN_N_DAYS, COLLECTING }

internal fun CardInfoUiState.toStatus(): CardInfoStatus = if (recordedDays != null) CardInfoStatus.COLLECTING else when (verdict) {
    ChargeVerdict.TODAY -> CardInfoStatus.TODAY
    ChargeVerdict.WITHIN_TWO_DAYS ->
        if (daysUntilCharge <= 1) CardInfoStatus.TOMORROW else CardInfoStatus.WITHIN_N_DAYS
    ChargeVerdict.THREE_DAYS_OR_MORE -> CardInfoStatus.WITHIN_N_DAYS
}

/** Figma: 1일차 16, 3일차 47, 13일차 204, 첫날 최소 8 */
internal fun collectingFillWidthDp(recordedDays: Int, trackDp: Int = 220): Int =
    Math.round(trackDp.toFloat() * recordedDays.coerceIn(0, COLLECTING_TOTAL_DAYS) / COLLECTING_TOTAL_DAYS).coerceAtLeast(8)
