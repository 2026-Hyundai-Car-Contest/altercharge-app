package com.altercharge.app.data.home

import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.StationSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectedStationInfoTest {

    private val summary = StationSummary(
        stationId = "ST9",
        name = "연남동 공영주차장",
        address = "서울 마포구 동교로 263",
        lat = 37.56,
        lng = 126.92,
        distanceMeters = 1200,
        availableCount = 2,
        totalValidCount = 4,
        chargerTypes = listOf(ConnectorType.DC_COMBO),
        maxOutputKw = 100.0,
        visitCount = 3,
        statusObservedAt = null,
    )

    @Test
    fun 목록에서_고른_충전소는_상세_없이_R1_값과_길_안내_목적지가_된다() {
        val info = summary.toSelectedStationInfo()
        assertEquals("ST9", info.stationId)
        assertEquals(2, info.availableCount)
        assertEquals(4, info.totalValidCount)
        assertEquals("서울 마포구 동교로 263", info.address)
        assertEquals(RouteCheck("ST9", 37.56, 126.92), info.toRouteDestination()?.let { RouteCheck(it.stationId, it.lat, it.lng) })
    }

    private data class RouteCheck(val id: String, val lat: Double, val lng: Double)
}
