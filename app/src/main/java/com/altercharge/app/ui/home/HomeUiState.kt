package com.altercharge.app.ui.home

import com.altercharge.app.data.navi.NavigationOutcome
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
    /** 내가 이 충전소에 충전하러 간 횟수 (REQ_ST_008) */
    val myVisitCount: Int = 0,
    /** 길 안내 요청 중 (REQ_BR_004). 이동 중 버튼을 비활성으로 둔다 */
    val navigating: Boolean = false,
    /** 길 안내 결과 안내. 한 번 보여 주면 [HomeViewModel.onNavigationResultShown]으로 지운다 */
    val navigationResult: NavigationOutcome? = null,
    val error: String? = null,
)
