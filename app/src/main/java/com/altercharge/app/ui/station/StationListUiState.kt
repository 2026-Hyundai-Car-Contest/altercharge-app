/** R2 주변 충전소 목록 화면 값 (REQ_SL_001 ~ REQ_SL_005) */
package com.altercharge.app.ui.station

import androidx.compose.runtime.Immutable
import com.altercharge.app.data.station.StationFilter
import com.altercharge.app.data.station.StationSort
import com.altercharge.app.ui.card.info.LastChecked
import com.altercharge.app.ui.card.stationitem.CardItemUiState

@Immutable
data class StationListUiState(
    val items: List<CardItemUiState> = emptyList(),
    /** 검색어. 음성 검색 결과도 여기에 들어간다 (REQ_SL_002 · REQ_SL_003) */
    val query: String = "",
    val sort: StationSort = StationSort.DISTANCE,
    val filter: StationFilter = StationFilter(),
    /** 방문 기록이 한 곳이라도 있어야 방문순을 고를 수 있다 (REQ_SL_004 예외) */
    val visitSortEnabled: Boolean = false,
    val isLoading: Boolean = false,
    /** 서버에 못 받아 저장한 목록을 쓰고 있다 (REQ_CM_006) */
    val offline: Boolean = false,
    /** 저장한 목록을 받은 시각. 오프라인일 때만 "N분 전 기준"으로 보여 준다 */
    val lastChecked: LastChecked? = null,
    val message: StationListMessage? = null,
    val voice: VoiceSearchState = VoiceSearchState.Idle,
) {
    /** 받은 충전소는 있는데 조건에 맞는 곳이 없다 */
    val isFilterEmpty: Boolean get() = message == StationListMessage.FILTER_EMPTY
}

enum class StationListMessage {
    /** 오프라인에서는 검색할 수 없다 (REQ_SL_002 · REQ_SL_003 예외) */
    SEARCH_UNAVAILABLE,
    FILTER_EMPTY,
    VOICE_PERMISSION,
    VOICE_UNAVAILABLE,
    VOICE_ERROR,
}

sealed interface VoiceSearchState {
    data object Idle : VoiceSearchState
    data object Listening : VoiceSearchState
}
