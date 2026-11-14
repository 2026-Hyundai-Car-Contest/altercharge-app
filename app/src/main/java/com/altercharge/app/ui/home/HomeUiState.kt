package com.altercharge.app.ui.home

import com.altercharge.app.data.home.HomeCards
import com.altercharge.app.data.home.RecommendedStationInfo
import com.altercharge.app.ui.card.vehiclestatus.CardVehicleStatusUiState

data class HomeUiState(
    val isLoading: Boolean = false,
    val cards: HomeCards? = null,
    /** C 차량 상태 카드. 차량 값을 읽는 즉시 채우고, 서버 값이 오면 다시 만든다 (REQ_VS_001 ~ REQ_VS_006) */
    val vehicleStatus: CardVehicleStatusUiState? = null,
    /** R1 추천 충전소 (REQ_ST_007) */
    val recommended: RecommendedStationInfo? = null,
    val error: String? = null,
)
