/**
 * 충전소 목록 정렬 (REQ_SL_004).
 * 서버 01은 반경 3km 목록을 거리 오름차순 한 가지로만 주므로, 정렬은 받아 둔 목록에서 앱이 한다 (REQ_CM_006).
 */
package com.altercharge.app.data.station

import com.altercharge.app.data.remote.model.StationSummary

/** REQ_SL_004 정렬 종류. 거리순이 기본이고, 방문 기록이 없으면 방문순을 쓸 수 없다 */
enum class StationSort { DISTANCE, VISIT }

/**
 * 화면에 쓸 방문 횟수. 서버 값과 기기에 센 값을 비교해 큰 쪽을 쓴다.
 * 서버는 MOCK 이벤트를 실제 방문으로 세지 않아(visitCountAdded=0) 시연 중에는 기기 기록이 기준이 된다 (REQ_ST_008).
 */
fun visitCountOf(station: StationSummary, localCounts: Map<String, Int>): Int =
    maxOf(localCounts[station.stationId] ?: 0, station.visitCount ?: 0)

/** 방문 기록이 한 곳이라도 있어야 방문순을 쓸 수 있다 (REQ_SL_004 예외) */
fun hasVisitHistory(stations: List<StationSummary>, localCounts: Map<String, Int>): Boolean =
    stations.any { visitCountOf(it, localCounts) > 0 }

/**
 * REQ_SL_004. 거리순은 거리 → 충전소 ID, 방문순은 방문 많은 순 → 가까운 순 → ID 순이다.
 * 방문 기록이 없는 충전소는 방문 있는 충전소 아래에 거리순으로 놓는다.
 */
fun sortStations(
    stations: List<StationSummary>,
    sort: StationSort,
    localCounts: Map<String, Int> = emptyMap(),
): List<StationSummary> = when (sort) {
    StationSort.DISTANCE -> stations.sortedWith(DistanceOrder)
    StationSort.VISIT -> stations.sortedWith(VisitOrder(localCounts))
}

private val DistanceOrder: Comparator<StationSummary> =
    compareBy<StationSummary> { it.distanceMeters }.thenBy { it.stationId }

private fun VisitOrder(localCounts: Map<String, Int>): Comparator<StationSummary> {
    fun visits(station: StationSummary) = visitCountOf(station, localCounts)
    return compareByDescending<StationSummary> { visits(it) > 0 }
        .thenByDescending { visits(it) }
        .thenBy { it.distanceMeters }
        .thenBy { it.stationId }
}
