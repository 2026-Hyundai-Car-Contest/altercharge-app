package com.altercharge.app.data.upload

import com.altercharge.app.data.home.VehicleReading
import com.altercharge.app.data.local.KeyValueStore
import com.altercharge.app.data.local.VisitCountStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.HttpClient
import com.altercharge.app.data.remote.HttpResponse
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.StationSummary
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Instant

/** 충전 시작 → 충전소 방문 기록 (REQ_ST_008) */
class ChargingEventRecorderTest {

    private class MemoryStore : KeyValueStore {
        val map = mutableMapOf<String, String>()
        override fun getString(key: String): String? = map[key]
        override fun putStrings(values: Map<String, String?>) {
            values.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }
    }

    private class FakeHttp : HttpClient {
        val calls = mutableListOf<Pair<String, String?>>()
        var online = true
        var status = 201
        override fun request(method: String, url: String, body: String?): HttpResponse {
            if (!online) throw IOException("offline")
            calls += url to body
            return HttpResponse(status, """{"eventId":"e","accepted":true,"matchedStationId":null,"visitCountAdded":0,"dailyDuplicate":false}""")
        }
    }

    private val http = FakeHttp()
    private val memory = MemoryStore()
    private val visits = VisitCountStore(memory)
    private val api = AlterChargeApi("https://example.test/api/v1/", http)
    private val recorder = ChargingEventRecorder(api, memory, visits)
    private val reading = VehicleReading(socPercent = 32, rangeKm = 98, odometerKm = 12500.0)
    private val now: Instant = Instant.parse("2026-11-14T03:00:00Z") // 한국 11.14(토) 12:00

    private fun station(id: String, lat: Double, lng: Double) = StationSummary(
        stationId = id,
        name = "충전소 $id",
        address = "서울 어딘가",
        lat = lat,
        lng = lng,
        distanceMeters = 30,
        availableCount = 1,
        totalValidCount = 1,
        chargerTypes = listOf(ConnectorType.DC_COMBO),
        maxOutputKw = 50.0,
        visitCount = null,
        statusObservedAt = null,
    )

    private val near = listOf(station("ST-1", 37.5502, 127.1400)) // 약 22m
    private val here = 37.5500 to 127.1400

    @Test
    fun 충전_시작을_서버로_보내고_방문을_센다() = runBlocking {
        val recorded = recorder.onChargingStarted(reading, now, here.first, here.second, near)
        assertEquals("ST-1", recorded.stationId)
        assertEquals(1, recorded.visitCount)

        assertEquals(1, recorder.flush(now))
        val (url, body) = http.calls.single()
        assertEquals("https://example.test/api/v1/charging-events", url)
        assertTrue(body!!.contains("\"eventType\":\"START\""))
        assertTrue(body.contains("\"vehicleId\":\"mock-vehicle-001\""))
        assertTrue(body.contains("\"socPercent\":32"))
        assertEquals(0, recorder.pendingCount())
    }

    @Test
    fun 오프라인이면_보내지_못해도_횟수는_바로_센다() = runBlocking {
        http.online = false
        recorder.onChargingStarted(reading, now, here.first, here.second, near)
        assertEquals(0, recorder.flush(now))
        assertEquals(1, recorder.pendingCount())
        assertEquals(1, visits.count("ST-1"))

        http.online = true
        assertEquals(1, recorder.flush(now.plusSeconds(60)))
        assertEquals(0, recorder.pendingCount())
    }

    @Test
    fun 반경_100m_밖이면_방문을_세지_않는다() {
        val far = listOf(station("ST-9", 37.5600, 127.1400)) // 약 1.1km
        val recorded = recorder.onChargingStarted(reading, now, here.first, here.second, far)
        assertNull(recorded.stationId)
        assertNull(recorded.visitCount)
        assertEquals(0, visits.count("ST-9"))
    }

    @Test
    fun 같은_날_두_번_충전해도_한_번만_센다() {
        recorder.onChargingStarted(reading, now, here.first, here.second, near)
        val second = recorder.onChargingStarted(reading, now.plusSeconds(3600), here.first, here.second, near)
        assertNull(second.visitCount)
        assertEquals(1, visits.count("ST-1"))
    }

    @Test
    fun 다음_날_충전하면_또_센다() {
        recorder.onChargingStarted(reading, now, here.first, here.second, near)
        val nextDay = recorder.onChargingStarted(reading, now.plusSeconds(24 * 3600), here.first, here.second, near)
        assertEquals(2, nextDay.visitCount)
    }

    @Test
    fun 다시_보내도_같은_키라_이벤트가_겹치지_않는다() {
        recorder.onChargingStarted(reading, now, here.first, here.second, near)
        // 같은 시각에 같은 충전소면 같은 eventId → 대기열에 하나만 남는다
        recorder.onChargingStarted(reading, now, here.first, here.second, near)
        assertEquals(1, recorder.pendingCount())
    }

    @Test
    fun 다시_보내도_안_되는_4xx는_버리고_서버_오류는_남긴다() = runBlocking {
        http.status = 400
        recorder.onChargingStarted(reading, now, here.first, here.second, near)
        assertEquals(0, recorder.flush(now))
        assertEquals(0, recorder.pendingCount())

        http.status = 503
        recorder.onChargingStarted(reading, now.plusSeconds(60), here.first, here.second, near)
        assertEquals(0, recorder.flush(now.plusSeconds(60)))
        assertEquals(1, recorder.pendingCount())
    }

    @Test
    fun 충전소를_못_찾아도_이벤트는_남긴다() = runBlocking {
        recorder.onChargingStarted(reading, now, here.first, here.second, emptyList())
        assertEquals(1, recorder.pendingCount())
        assertEquals(1, recorder.flush(now))
    }
}
