package com.altercharge.app.ui.card.info

import androidx.compose.runtime.Immutable
import com.altercharge.app.ui.card.common.ChargeVerdict

/**
 * L1 오늘 충전 판정 카드에 넘기는 값. Figma card_info (366:38742).
 * 숫자와 상태만 담고, 문구는 카드가 리소스에서 만든다.
 */
@Immutable
data class CardInfoUiState(
    val verdict: ChargeVerdict,
    /** 충전까지 남은 날 (D-n). 0 = 오늘 */
    val daysUntilCharge: Int,
    /** 기준 거점 이름 (예: 명일 신동아 아파트). 한 줄 말줄임 */
    val placeName: String,
    /**
     * 기록 수집 중(14일 미만)일 때 쌓인 기록 일수 0~13. null이면 판정 카드다.
     * 값이 있으면 [verdict] · [daysUntilCharge] · [placeName]은 쓰지 않는다. [collecting]으로 만든다.
     */
    val recordedDays: Int? = null,
) {
    companion object {
        /** 기록 수집 중 카드 값 (Figma status = collecting). 판정 값은 쓰지 않는 자리 채움이다. */
        fun collecting(recordedDays: Int) = CardInfoUiState(
            verdict = ChargeVerdict.THREE_DAYS_OR_MORE,
            daysUntilCharge = 0,
            placeName = "",
            recordedDays = recordedDays.coerceIn(0, MAX_COLLECTING_DAYS),
        )
    }
}

/** 수집 기간 14일 중 판정 전까지 최대 기록 일수 (REQ_JD_002) */
internal const val MAX_COLLECTING_DAYS = 13

/** 수집 기간(일). 진행 바 "N / 14일" */
internal const val COLLECTING_TOTAL_DAYS = 14

/**
 * 카드가 그리는 판정 단계. Figma status: today · tomorrow · 3days.
 * 공통 ChargeVerdict(오늘 / 1~2일 / 3일 이상)와 단계가 달라 카드 안에서 바꾼다.
 */
internal enum class CardInfoStatus { TODAY, TOMORROW, WITHIN_THREE_DAYS, COLLECTING }

internal fun CardInfoUiState.toStatus(): CardInfoStatus = if (recordedDays != null) CardInfoStatus.COLLECTING else when (verdict) {
    ChargeVerdict.TODAY -> CardInfoStatus.TODAY
    // 1일 후 → 내일 충전, 2일 후 → 3일내 충전
    ChargeVerdict.WITHIN_TWO_DAYS ->
        if (daysUntilCharge <= 1) CardInfoStatus.TOMORROW else CardInfoStatus.WITHIN_THREE_DAYS
    // TODO 기획 확인: 3일 이상 남았을 때 L1 표시가 시안에 없다. 정해질 때까지 가장 여유 있는 단계로 보여 준다.
    ChargeVerdict.THREE_DAYS_OR_MORE -> CardInfoStatus.WITHIN_THREE_DAYS
}

/** 진행 막대 채움 폭(dp). 트랙 220 × N / 14 반올림, 최소 8 (Figma: 1일차 16, 3일차 47, 13일차 204, 첫날 최소 8). */
internal fun collectingFillWidthDp(recordedDays: Int, trackDp: Int = 220): Int =
    Math.round(trackDp.toFloat() * recordedDays.coerceIn(0, COLLECTING_TOTAL_DAYS) / COLLECTING_TOTAL_DAYS).coerceAtLeast(8)
