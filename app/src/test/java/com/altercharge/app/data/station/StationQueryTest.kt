package com.altercharge.app.data.station

import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.StationSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 목록 정렬 · 필터 (REQ_SL_004 · REQ_SL_005) */
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

    @Test
    fun 연결타입_필터는_하나라도_맞으면_남긴다() {
        val list = listOf(
            station("combo", 100, types = listOf(ConnectorType.DC_COMBO)),
            station("ac", 200, types = listOf(ConnectorType.AC_SLOW)),
            station("mixed", 300, types = listOf(ConnectorType.DC_CHADEMO, ConnectorType.AC_3PHASE)),
        )
        val filtered = filterStations(list, StationFilter(connectors = setOf(ConnectorFilter.DC_CHADEMO)))
        assertEquals(listOf("mixed"), filtered.map { it.stationId })
    }

    @Test
    fun 연결타입을_여러_개_고르면_합집합() {
        val list = listOf(
            station("combo", 100, types = listOf(ConnectorType.DC_COMBO)),
            station("ac", 200, types = listOf(ConnectorType.AC_SLOW)),
            station("nacs", 300, types = listOf(ConnectorType.NACS)),
        )
        val filter = StationFilter(connectors = setOf(ConnectorFilter.AC_SLOW, ConnectorFilter.NACS))
        assertEquals(listOf("ac", "nacs"), filterStations(list, filter).map { it.stationId })
    }

    @Test
    fun NACS_필터는_테슬라_규격만_남긴다() {
        val list = listOf(
            station("combo", 100, types = listOf(ConnectorType.DC_COMBO)),
            station("nacs", 200, types = listOf(ConnectorType.DC_COMBO, ConnectorType.NACS)),
        )
        val filter = StationFilter(connectors = setOf(ConnectorFilter.NACS))
        assertEquals(listOf("nacs"), filterStations(list, filter).map { it.stationId })
    }

    @Test
    fun 출력_범위_필터는_출력을_모르는_충전소를_뺀다() {
        val list = listOf(
            station("slow", 100, maxOutputKw = 7.0),
            station("fast", 200, maxOutputKw = 100.0),
            station("unknown", 300, maxOutputKw = null),
        )
        val filter = StationFilter(power = PowerRange(50, 200))
        assertEquals(listOf("fast"), filterStations(list, filter).map { it.stationId })
    }

    @Test
    fun 출력_위쪽_끝_350은_이상_이라_상한이_없다() {
        val list = listOf(station("over", 100, maxOutputKw = 400.0), station("mid", 200, maxOutputKw = 150.0))
        val filter = StationFilter(power = PowerRange(200, 350))
        assertEquals(listOf("over"), filterStations(list, filter).map { it.stationId })
    }

    @Test
    fun 기본_범위는_출력이_없는_충전소도_남긴다() {
        val list = listOf(station("unknown", 100, maxOutputKw = null))
        assertTrue(StationFilter().isAll)
        assertEquals(listOf("unknown"), filterStations(list, StationFilter()).map { it.stationId })
    }

    @Test
    fun 출력_범위는_작은_값을_앞에_넣어도_같다() {
        val range = PowerRange(minKw = 350, maxKw = 3)
        assertTrue(range.isAll)
        assertEquals(3, range.normalizedMinKw)
        assertEquals(350, range.normalizedMaxKw)
    }
}
