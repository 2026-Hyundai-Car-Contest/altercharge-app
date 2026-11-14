/**
 * 받아 둔 목록 · 검색 결과 · 정렬 · 필터 → R2 화면 값 (REQ_SL_004 · REQ_SL_005 · REQ_CM_006).
 * ViewModel과 테스트가 같이 쓴다: 안드로이드 없이 확인할 수 있게 순수 함수로 둔다.
 */
package com.altercharge.app.ui.station

import com.altercharge.app.data.remote.freshness.stationValidity
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.data.station.StationFilter
import com.altercharge.app.data.station.StationSort
import com.altercharge.app.data.station.filterStations
import com.altercharge.app.data.station.hasVisitHistory
import com.altercharge.app.data.station.sortStations
import com.altercharge.app.data.station.visitCountOf
import com.altercharge.app.ui.card.stationitem.toCardItemUiState
import java.time.Instant

/**
 * @param searchResults 서버 검색 결과. null이면 [all] 전체를 쓴다
 * @param receivedAt 목록을 받은 시각. 15분이 지나면 가용 대수를 "정보 없음"으로 보여 준다 (REQ_CM_009)
 */
fun buildStationList(
    all: List<StationSummary>,
    searchResults: List<StationSummary>?,
    sort: StationSort,
    filter: StationFilter,
    localCounts: Map<String, Int>,
    receivedAt: Instant?,
    now: Instant,
): StationListUiState {
    val source = searchResults ?: all
    val visitSortEnabled = hasVisitHistory(source, localCounts)
    // 방문 기록이 없으면 방문순을 쓸 수 없다 (REQ_SL_004 예외)
    val appliedSort = if (sort == StationSort.VISIT && visitSortEnabled) StationSort.VISIT else StationSort.DISTANCE
    val availabilityUnknown = stationValidity(receivedAt, now).showAvailabilityUnknown
    val filtered = filterStations(source, filter)
    val items = sortStations(filtered, appliedSort, localCounts).map { station ->
        station.toCardItemUiState(
            visitCount = visitCountOf(station, localCounts),
            showVisitCount = appliedSort == StationSort.VISIT,
            availabilityUnknown = availabilityUnknown,
        )
    }
    return StationListUiState(
        items = items,
        sort = appliedSort,
        filter = filter,
        visitSortEnabled = visitSortEnabled,
        // 받은 충전소는 있는데 조건에 맞는 곳이 없을 때만 안내한다
        message = if (source.isNotEmpty() && filtered.isEmpty()) StationListMessage.FILTER_EMPTY else null,
    )
}
