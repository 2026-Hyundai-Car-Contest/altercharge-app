/** R2 주변 충전소 목록 화면 값 (REQ_SL_001 · REQ_SL_002) */
package com.altercharge.app.ui.station

import androidx.compose.runtime.Immutable
import com.altercharge.app.ui.card.info.LastChecked
import com.altercharge.app.ui.card.stationitem.CardItemUiState

@Immutable
data class StationListUiState(
    val items: List<CardItemUiState> = emptyList(),
    /** 검색어 (REQ_SL_002) */
    val query: String = "",
    val isLoading: Boolean = false,
    /** 서버에 못 받아 저장한 목록을 쓰고 있다 (REQ_CM_006) */
    val offline: Boolean = false,
    /** 저장한 목록을 받은 시각. 오프라인일 때만 "N분 전 기준"으로 보여 준다 */
    val lastChecked: LastChecked? = null,
    val message: StationListMessage? = null,
)

enum class StationListMessage {
    /** 오프라인에서는 검색할 수 없다 (REQ_SL_002 예외) */
    SEARCH_UNAVAILABLE,
}
