package com.altercharge.app.data.station

import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.StationSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 목록 정렬 (REQ_SL_004) */
class StationQueryTest {

    private fun station(
        id: String,
        distanceMeters: Int,
        visitCount: Int? = null,
        types: List<ConnectorType> = listOf(ConnectorType.DC_COMBO),
        maxOutputKw: Double? = 100.0,
    ) = StationSummary(
        stationId = id,
        name = "충전소 $id",
        address = "서울 어딘가",
        lat = 37.55,
        lng = 127.14,
        distanceMeters = distanceMeters,
        availableCount = 1,
        totalValidCount = 2,
        chargerTypes = types,
        maxOutputKw = maxOutputKw,
        visitCount = visitCount,
        statusObservedAt = null,
    )

    @Test
    fun 거리순은_거리_다음_충전소_ID() {
        val list = listOf(station("B", 500), station("A", 500), station("C", 100))
        assertEquals(listOf("C", "A", "B"), sortStations(list, StationSort.DISTANCE).map { it.stationId })
    }

    @Test
    fun 방문순은_방문_많은_순_다음_가까운_순() {
        val list = listOf(
            station("A", 900, visitCount = 3),
            station("B", 100, visitCount = 3),
            station("C", 200, visitCount = 5),
        )
        assertEquals(listOf("C", "B", "A"), sortStations(list, StationSort.VISIT).map { it.stationId })
    }

    @Test
    fun 방문_없는_충전소는_방문한_곳_뒤에_거리순_으로() {
        val list = listOf(
            station("N1", 50),
            station("V1", 800, visitCount = 1),
            station("N2", 10),
        )
        assertEquals(listOf("V1", "N2", "N1"), sortStations(list, StationSort.VISIT).map { it.stationId })
    }

    @Test
    fun 기기에_센_방문도_정렬에_쓴다() {
        val list = listOf(station("A", 100), station("B", 300))
        assertEquals(
            listOf("B", "A"),
            sortStations(list, StationSort.VISIT, localCounts = mapOf("B" to 2)).map { it.stationId },
        )
    }

    @Test
    fun 방문_기록이_한_곳도_없으면_방문순을_쓸_수_없다() {
        val list = listOf(station("A", 100), station("B", 300))
        assertFalse(hasVisitHistory(list, emptyMap()))
        assertTrue(hasVisitHistory(list, mapOf("A" to 1)))
        assertTrue(hasVisitHistory(list, mapOf("B" to 1)))
    }

    @Test
    fun 서버_방문수와_기기_방문수_중_큰_값을_쓴다() {
        assertEquals(4, visitCountOf(station("A", 100, visitCount = 4), mapOf("A" to 2)))
        assertEquals(3, visitCountOf(station("A", 100), mapOf("A" to 3)))
        assertEquals(0, visitCountOf(station("A", 100), emptyMap()))
    }
}
