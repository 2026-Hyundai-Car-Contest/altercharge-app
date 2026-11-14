/**
 * 충전소 목록 거르기 · 정렬 (REQ_SL_004 · REQ_SL_005).
 * 서버 01은 반경 3km 목록을 거리 오름차순 한 가지로만 주므로, 정렬과 필터는 받아 둔 목록에서 앱이 한다 (REQ_CM_006).
 */
package com.altercharge.app.data.station

import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.StationSummary

/** REQ_SL_004 정렬 종류. 거리순이 기본이고, 방문 기록이 없으면 방문순을 쓸 수 없다 */
enum class StationSort { DISTANCE, VISIT }

/**
 * REQ_SL_005 연결 타입 5가지.
 * 한 충전소에 규격이 여러 개면(chargerTypes 합집합) 하나만 골라도 남긴다.
 * DC_COMBO_2(11)는 버스 전용 원본 코드라 DC콤보로 묶지 않는다.
 */
enum class ConnectorFilter(val types: Set<ConnectorType>) {
    AC_SLOW(setOf(ConnectorType.AC_SLOW)),
    AC_THREE_PHASE(setOf(ConnectorType.AC_3PHASE)),
    DC_CHADEMO(setOf(ConnectorType.DC_CHADEMO)),
    DC_COMBO(setOf(ConnectorType.DC_COMBO)),
    NACS(setOf(ConnectorType.NACS)),
}

/** 슬라이더 눈금(ChargePowerSlider와 같다). 마지막 350은 "350kW 이상"이라는 뜻이다 */
val POWER_STEPS: List<Int> = listOf(3, 7, 11, 50, 100, 200, 350)

/** 출력 범위 연속 선택 (REQ_SL_005). 마지막 눈금이면 위쪽 한계가 없다 */
data class PowerRange(
    val minKw: Int = POWER_STEPS.first(),
    val maxKw: Int = POWER_STEPS.last(),
) {
    val normalizedMinKw: Int get() = minOf(minKw, maxKw)

    val normalizedMaxKw: Int get() = maxOf(minKw, maxKw)

    /** 기본값이면 출력으로 거르지 않는다. 출력 정보가 없는 충전소도 남는다 */
    val isAll: Boolean get() = normalizedMinKw <= POWER_STEPS.first() && normalizedMaxKw >= POWER_STEPS.last()

    fun contains(outputKw: Double?): Boolean {
        if (isAll) return true
        if (outputKw == null) return false // 출력을 모르면 범위 안이라고 말할 수 없다
        if (outputKw < normalizedMinKw) return false
        // 위쪽 끝 눈금은 "이상"이라 상한이 없다
        return normalizedMaxKw >= POWER_STEPS.last() || outputKw <= normalizedMaxKw
    }
}

/** REQ_SL_005 필터. 연결 타입은 복수 선택, 출력은 범위 하나 */
data class StationFilter(
    val connectors: Set<ConnectorFilter> = emptySet(),
    val power: PowerRange = PowerRange(),
) {
    val isAll: Boolean get() = connectors.isEmpty() && power.isAll

    fun matches(station: StationSummary): Boolean =
        matchesConnector(station) && power.contains(station.maxOutputKw)

    private fun matchesConnector(station: StationSummary): Boolean {
        if (connectors.isEmpty()) return true
        return station.chargerTypes.any { type -> connectors.any { type in it.types } }
    }
}

/**
 * 화면에 쓸 방문 횟수. 서버 값과 기기에 센 값을 비교해 큰 쪽을 쓴다.
 * 서버는 MOCK 이벤트를 실제 방문으로 세지 않아(visitCountAdded=0) 시연 중에는 기기 기록이 기준이 된다 (REQ_ST_008).
 */
fun visitCountOf(station: StationSummary, localCounts: Map<String, Int>): Int =
    maxOf(localCounts[station.stationId] ?: 0, station.visitCount ?: 0)

/** 방문 기록이 한 곳이라도 있어야 방문순을 쓸 수 있다 (REQ_SL_004 예외) */
fun hasVisitHistory(stations: List<StationSummary>, localCounts: Map<String, Int>): Boolean =
    stations.any { visitCountOf(it, localCounts) > 0 }

/** 필터를 그대로 적용한다. 정렬 순서는 [sortStations]가 정한다 */
fun filterStations(stations: List<StationSummary>, filter: StationFilter): List<StationSummary> =
    if (filter.isAll) stations else stations.filter(filter::matches)

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
