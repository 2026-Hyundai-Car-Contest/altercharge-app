package com.altercharge.app.ui.card.common

/**
 * 여러 카드가 같이 쓰는 값. 카드끼리는 서로를 가져다 쓰지 않고, 공통으로 필요한 것만 여기 둔다.
 * 담당: 서희님 (바꿀 때 석기님에게 알린다)
 */

/** 충전 판정 단계. L1과 C가 같이 쓴다. Figma 판정: 오늘 / 1~2일 / 3일 이상 */
enum class ChargeVerdict { TODAY, WITHIN_TWO_DAYS, THREE_DAYS_OR_MORE }

/** 충전기 속도. R1과 R2가 같이 쓴다. 아이콘: 완속 플러그, 급속 번개 1개, 초급속 번개 2개 */
enum class ChargerSpeed { SLOW, FAST, ULTRA_FAST }
