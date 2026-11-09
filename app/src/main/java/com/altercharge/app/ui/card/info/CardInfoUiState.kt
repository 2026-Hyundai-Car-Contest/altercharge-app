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
    /** 기준 거점 이름 (예: 명일 신동아 아파트) */
    val placeName: String,
)
