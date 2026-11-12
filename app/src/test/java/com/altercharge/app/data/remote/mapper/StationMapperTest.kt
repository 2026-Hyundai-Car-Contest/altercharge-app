package com.altercharge.app.data.remote.mapper

import com.altercharge.app.data.remote.model.LatestChargeHistory
import com.altercharge.app.data.remote.model.Recommendation
import com.altercharge.app.data.remote.model.RecommendationReason
import com.altercharge.app.data.remote.model.RecommendedStation
import com.altercharge.app.data.remote.model.SpeedCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StationMapperTest {

    private val zone: ZoneId = ZoneId.of("Asia/Seoul")

    private val station = RecommendedStation(
        stationId = "ST1",
        name = "예시 공영주차장",
        distanceMeters = 1234,
        availableCount = 2,
        visitCount = null,
        speedCategory = SpeedCategory.FAST,
        maxOutputKw = 100.0,
    )

    @Test
    fun 추천_충전소가_있으면_이름과_거리() {
        val r = Recommendation(station, RecommendationReason.VISITS_THEN_DISTANCE, fallbackUsed = false, statusObservedAt = null)
        assertEquals("예시 공영주차장", r.placeName())
        assertEquals(1.2, r.nearestStationKm()!!, 0.0001)
    }

    @Test
    fun 거리는_소수_첫째_자리로_반올림() {
        val r = Recommendation(station.copy(distanceMeters = 1250), RecommendationReason.NEAREST_FALLBACK, true, null)
        assertEquals(1.3, r.nearestStationKm()!!, 0.0001)
    }

    @Test
    fun 반경_안에_충전소가_없으면_빈_이름과_null_거리() {
        val r = Recommendation(null, RecommendationReason.NO_STATIONS_IN_RADIUS, fallbackUsed = false, statusObservedAt = null)
        assertEquals("", r.placeName())
        assertNull(r.nearestStationKm())
    }

    private fun charge(at: Instant?) = LatestChargeHistory("v", at, null, null, null, trusted = false)

    @Test
    fun 오늘_충전_완료는_시각만() {
        // UTC 22:30 (11.05) = 한국 11.06 07:30
        val text = charge(Instant.parse("2026-11-05T22:30:00Z")).lastChargedAtText(LocalDate.of(2026, 11, 6), zone)
        assertEquals("07:30", text)
    }

    @Test
    fun 다른_날_충전_완료는_날짜() {
        val text = charge(Instant.parse("2026-11-03T12:40:00Z")).lastChargedAtText(LocalDate.of(2026, 11, 6), zone)
        assertEquals("11.3", text)
    }

    @Test
    fun 완료_이벤트가_없으면_null() {
        assertNull(charge(null).lastChargedAtText(LocalDate.of(2026, 11, 6), zone))
    }
}
