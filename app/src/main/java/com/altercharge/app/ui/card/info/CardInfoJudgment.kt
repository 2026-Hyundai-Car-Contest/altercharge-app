package com.altercharge.app.ui.card.info

import com.altercharge.app.domain.judgment.Judgment
import com.altercharge.app.ui.card.common.ChargeVerdict

/**
 * 충전 판정 결과로 L1 카드 값을 만든다. 버틸 일수(daysLeft)로 판정 단계를 고른다.
 * - 0일 → 오늘 충전
 * - 1일 → 내일 충전
 * - 2일 → 3일내 충전
 * - 3일 이상 → 3일 이상 단계. 카드는 지금 3일내로 보여 준다(CardInfoUiState.toStatus).
 *
 * TODO 기획 확인: 3일 이상 남았을 때 L1 표시가 정해지면 toStatus 쪽에서 바꾼다.
 *
 * @param placeName 기준 거점 이름
 */
fun Judgment.toCardInfoUiState(placeName: String): CardInfoUiState {
    val days = daysLeft.coerceAtLeast(0)
    val verdict = when {
        days == 0 -> ChargeVerdict.TODAY
        days <= LAST_DAY_WITHIN_TWO_DAYS -> ChargeVerdict.WITHIN_TWO_DAYS
        else -> ChargeVerdict.THREE_DAYS_OR_MORE
    }
    return CardInfoUiState(verdict = verdict, daysUntilCharge = days, placeName = placeName)
}

/** 1~2일 단계의 마지막 날 */
private const val LAST_DAY_WITHIN_TWO_DAYS = 2
