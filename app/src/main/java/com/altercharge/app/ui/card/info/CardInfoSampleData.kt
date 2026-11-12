package com.altercharge.app.ui.card.info

import com.altercharge.app.ui.card.common.ChargeVerdict

/**
 * L1 카드 샘플 값. 미리보기 · 디버그 카탈로그 · 화면 개발 중 임시 데이터로 쓴다.
 * 실데이터가 아니다 (장소는 "예시 아파트").
 */
internal object CardInfoSampleData {
    /** 오늘 충전 */
    val Today = CardInfoUiState(ChargeVerdict.TODAY, daysUntilCharge = 0, placeName = "예시 아파트")

    /** 내일 충전 */
    val Tomorrow = CardInfoUiState(ChargeVerdict.WITHIN_TWO_DAYS, daysUntilCharge = 1, placeName = "예시 아파트 지하주차장")

    /** 2일내 충전 · 긴 장소 이름(한 줄 말줄임) */
    val WithinTwoDaysLongPlace = CardInfoUiState(
        ChargeVerdict.WITHIN_TWO_DAYS,
        daysUntilCharge = 2,
        placeName = "아주 긴 예시 장소 이름이 들어가면 한 줄로 말줄임",
    )

    /** 두 자리 일수 "12일내 충전": 제목 폭이 가장 넓은 경우 */
    val WithinTwelveDays = CardInfoUiState(ChargeVerdict.THREE_DAYS_OR_MORE, daysUntilCharge = 12, placeName = "예시 아파트")

    /** 기록 수집 중: 첫날 · 3일차 · 13일차 */
    val CollectingFirstDay = CardInfoUiState.collecting(recordedDays = 0)
    val CollectingDay3 = CardInfoUiState.collecting(recordedDays = 3)
    val CollectingDay13 = CardInfoUiState.collecting(recordedDays = 13)

    val all: List<CardInfoUiState> = listOf(Today, Tomorrow, WithinTwoDaysLongPlace, WithinTwelveDays)
    val collecting: List<CardInfoUiState> = listOf(CollectingFirstDay, CollectingDay3, CollectingDay13)
}
