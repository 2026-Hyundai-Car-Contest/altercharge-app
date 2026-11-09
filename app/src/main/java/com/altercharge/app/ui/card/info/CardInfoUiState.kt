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
)

/**
 * 카드가 그리는 판정 단계. Figma status: today · tomorrow · 3days.
 * 공통 ChargeVerdict(오늘 / 1~2일 / 3일 이상)와 단계가 달라 카드 안에서 바꾼다.
 */
internal enum class CardInfoStatus { TODAY, TOMORROW, WITHIN_THREE_DAYS }

internal fun CardInfoUiState.toStatus(): CardInfoStatus = when (verdict) {
    ChargeVerdict.TODAY -> CardInfoStatus.TODAY
    // 1일 후 → 내일 충전, 2일 후 → 3일내 충전
    ChargeVerdict.WITHIN_TWO_DAYS ->
        if (daysUntilCharge <= 1) CardInfoStatus.TOMORROW else CardInfoStatus.WITHIN_THREE_DAYS
    // TODO 기획 확인: 3일 이상 남았을 때 L1 표시가 시안에 없다. 정해질 때까지 가장 여유 있는 단계로 보여 준다.
    ChargeVerdict.THREE_DAYS_OR_MORE -> CardInfoStatus.WITHIN_THREE_DAYS
}
