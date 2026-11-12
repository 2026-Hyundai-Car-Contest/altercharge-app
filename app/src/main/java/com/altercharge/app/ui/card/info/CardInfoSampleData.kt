package com.altercharge.app.ui.card.info

import com.altercharge.app.ui.card.common.ChargeVerdict

/** L1 카드 샘플. 실데이터가 아니다 */
internal object CardInfoSampleData {
    val Today = CardInfoUiState(ChargeVerdict.TODAY, daysUntilCharge = 0, placeName = "예시 아파트")

    val Tomorrow = CardInfoUiState(ChargeVerdict.WITHIN_TWO_DAYS, daysUntilCharge = 1, placeName = "예시 아파트 지하주차장")

    val WithinTwoDaysLongPlace = CardInfoUiState(
        ChargeVerdict.WITHIN_TWO_DAYS,
        daysUntilCharge = 2,
        placeName = "아주 긴 예시 장소 이름이 들어가면 한 줄로 말줄임",
    )

    /** 제목 폭이 가장 넓은 경우 + 충전소 정보 없음 */
    val WithinTwelveDaysNoPlace = CardInfoUiState(ChargeVerdict.THREE_DAYS_OR_MORE, daysUntilCharge = 12, placeName = "")

    val TodayOffline = Today.copy(placeLastChecked = LastChecked.MinutesAgo(10))

    val WithinFiveDaysOfflineOtherDay = CardInfoUiState(
        ChargeVerdict.THREE_DAYS_OR_MORE,
        daysUntilCharge = 5,
        placeName = "예시 아파트",
        placeLastChecked = LastChecked.OnDate(month = 11, day = 11),
    )

    val CollectingFirstDay = CardInfoUiState.collecting(recordedDays = 0)
    val CollectingDay3 = CardInfoUiState.collecting(recordedDays = 3)
    val CollectingDay13 = CardInfoUiState.collecting(recordedDays = 13)

    val all: List<CardInfoUiState> = listOf(
        Today, Tomorrow, WithinTwoDaysLongPlace, WithinTwelveDaysNoPlace, TodayOffline, WithinFiveDaysOfflineOtherDay,
    )
    val collecting: List<CardInfoUiState> = listOf(CollectingFirstDay, CollectingDay3, CollectingDay13)
}
