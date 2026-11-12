package com.altercharge.app.ui.card.common

/** 카드끼리는 서로를 가져다 쓰지 않고 공통 값만 여기 둔다. 바꿀 때 다른 카드 담당에게 알린다 */

/** Figma 판정: 오늘 / 1~2일 / 3일 이상 */
enum class ChargeVerdict { TODAY, WITHIN_TWO_DAYS, THREE_DAYS_OR_MORE }
