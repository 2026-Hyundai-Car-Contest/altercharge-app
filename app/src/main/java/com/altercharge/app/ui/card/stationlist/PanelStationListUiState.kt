package com.altercharge.app.ui.card.stationlist

import androidx.compose.runtime.Immutable
import com.altercharge.app.ui.card.common.ChargerSpeed

/** 정렬 세그먼트: 거리순 / 방문순 */
enum class StationSort { DISTANCE, VISITS }

/** 즐겨찾기 거점이면 집 · 회사 아이콘을 보여준다 */
enum class SavedPlace { HOME, COMPANY }

/** 목록 한 줄 */
@Immutable
data class StationItemUiState(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val address: String,
    val speed: ChargerSpeed,
    val availableCount: Int,
    val totalCount: Int,
    val savedPlace: SavedPlace?,
    /** 점검 중 · 이용 불가면 false (disabled 상태) */
    val enabled: Boolean = true,
)

/**
 * R2 충전소 목록 패널에 넘기는 값. Figma panel_station_list (366:38178), 칩 열림 (476:90036).
 *
 * 초안이다. 담당인 석기님이 구현하면서 필드를 고쳐도 된다.
 * TODO 석기님: "연결 타입" 필터 값 정의 (Figma 칩: 연결 타입 전체)
 */
@Immutable
data class PanelStationListUiState(
    val query: String,
    val sort: StationSort,
    /** null = 속도 전체 */
    val speedFilter: ChargerSpeed?,
    val items: List<StationItemUiState>,
    /** null = 선택 없음 (R1은 추천 충전소를 보여준다) */
    val selectedId: String?,
)
