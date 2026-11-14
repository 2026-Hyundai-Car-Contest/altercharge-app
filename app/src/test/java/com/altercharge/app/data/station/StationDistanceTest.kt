package com.altercharge.app.data.station

import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.SpeedCategory
import com.altercharge.app.data.remote.model.StationSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 거리 계산 · 100m 방문 판정 (REQ_SL_001 · REQ_ST_008) · 표시 값 */
class StationDistanceTest {

    private fun station(id: String, lat: Double, lng: Double) = StationSummary(
        stationId = id,
        name = "충전소 $id",
        address = "서울 어딘가",
        lat = lat,
        lng = lng,
        distanceMeters = 0,
        availableCount = 1,
        totalValidCount = 1,
        chargerTypes = listOf(ConnectorType.DC_COMBO),
        maxOutputKw = 50.0,
        visitCount = null,
        statusObservedAt = null,
    )

    @Test
    fun 같은_좌표는_0미터() {
        assertEquals(0.0, distanceMeters(37.55, 127.14, 37.55, 127.14), 0.5)
    }

    @Test
    fun 위도_0_001도는_약_111미터() {
        // 위도 1도 ≈ 111.32km
        assertEquals(111.0, distanceMeters(37.550, 127.140, 37.551, 127.140), 1.0)
    }

    @Test
    fun 반경_100m_안에서_가장_가까운_충전소를_찾는다() {
        val list = listOf(
            station("far", 37.5530, 127.1400), // 약 330m
            station("near", 37.5505, 127.1400), // 약 56m
            station("nearer", 37.5502, 127.1400), // 약 22m
        )
        assertEquals("nearer", nearestStationWithin(list, 37.5500, 127.1400)?.stationId)
    }

    @Test
    fun 반경을_벗어나면_찾지_않는다() {
        val list = listOf(station("far", 37.5530, 127.1400))
        assertNull(nearestStationWithin(list, 37.5500, 127.1400))
    }

    @Test
    fun 거리가_같으면_ID_순으로_고른다() {
        val list = listOf(
            station("B", 37.5505, 127.1400),
            station("A", 37.5505, 127.1400),
        )
        assertEquals("A", nearestStationWithin(list, 37.5500, 127.1400)?.stationId)
    }

    @Test
    fun 거리는_소수_한_자리_km로_보여_주고_최소_0_1km() {
        assertEquals(0.1, displayKm(0), 0.0001)
        assertEquals(0.1, displayKm(40), 0.0001)
        assertEquals(0.6, displayKm(560), 0.0001)
        assertEquals(1.3, displayKm(1_250), 0.0001)
    }

    @Test
    fun 출력으로_충전_속도를_가른다() {
        assertNull(speedCategoryOf(null))
        assertEquals(SpeedCategory.SLOW, speedCategoryOf(7.0))
        assertEquals(SpeedCategory.SLOW, speedCategoryOf(49.9))
        assertEquals(SpeedCategory.FAST, speedCategoryOf(50.0))
        assertEquals(SpeedCategory.FAST, speedCategoryOf(199.9))
        assertEquals(SpeedCategory.ULTRA_FAST, speedCategoryOf(200.0))
    }
}
