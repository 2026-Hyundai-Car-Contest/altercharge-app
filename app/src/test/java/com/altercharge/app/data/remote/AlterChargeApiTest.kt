package com.altercharge.app.data.remote

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/** 실제 연결 대신 가짜 HttpClient로 요청 주소 · 결과 나누기를 확인한다 */
class AlterChargeApiTest {

    private class FakeHttp(private val respond: (String, String, String?) -> HttpResponse) : HttpClient {
        val calls = mutableListOf<Triple<String, String, String?>>()
        override fun request(method: String, url: String, body: String?): HttpResponse {
            calls += Triple(method, url, body)
            return respond(method, url, body)
        }
    }

    private val base = "https://example.test/api/v1/"

    @Test
    fun 쿼리는_null을_빼고_인코딩한다() {
        val api = AlterChargeApi(base, FakeHttp { _, _, _ -> HttpResponse(200, "{}") })
        assertEquals(
            "https://example.test/api/v1/stations?lat=37.5&lng=127.0&keyword=%ED%99%8D%EB%8C%80%20%EC%A3%BC%EC%B0%A8%EC%9E%A5",
            api.urlOf("stations", listOf("lat" to 37.5, "lng" to 127.0, "radiusKm" to null, "keyword" to "홍대 주차장")),
        )
    }

    @Test
    fun 끝_슬래시가_없어도_주소를_만든다() {
        val api = AlterChargeApi("https://example.test/api/v1", FakeHttp { _, _, _ -> HttpResponse(200, "{}") })
        assertEquals("https://example.test/api/v1/charging-events/latest", api.urlOf("charging-events/latest", emptyList()))
    }

    @Test
    fun 성공_응답은_읽어서_돌려준다() = runBlocking {
        val http = FakeHttp { _, _, _ ->
            HttpResponse(200, """{"vehicleId":"mock-vehicle-001","records":[],"retentionDays":56,"completeDayCount":0}""")
        }
        val r = AlterChargeApi(base, http).snapshots(DEMO_VEHICLE_ID)
        assertTrue(r is ApiResult.Success)
        assertEquals("GET", http.calls.single().first)
        assertEquals("${base}vehicles/snapshots?vehicleId=mock-vehicle-001", http.calls.single().second)
    }

    @Test
    fun 서버_오류는_오류_코드와_함께() = runBlocking {
        val http = FakeHttp { _, _, _ ->
            HttpResponse(503, """{"error":"STATION_DATA_UNAVAILABLE","message":"아직 수집된 충전소 데이터가 없습니다."}""")
        }
        val r = AlterChargeApi(base, http).recommendation(37.5, 127.0, chargeRecommended = true)
        r as ApiResult.HttpError
        assertEquals(503, r.status)
        assertEquals("STATION_DATA_UNAVAILABLE", r.error!!.error)
    }

    @Test
    fun 본문_없는_서버_오류도_처리한다() = runBlocking {
        val r = AlterChargeApi(base, FakeHttp { _, _, _ -> HttpResponse(502, "") }).latestCharge(DEMO_VEHICLE_ID)
        r as ApiResult.HttpError
        assertNull(r.error)
    }

    @Test
    fun 연결_실패는_네트워크_오류() = runBlocking {
        val r = AlterChargeApi(base, FakeHttp { _, _, _ -> throw IOException("offline") }).stationDetail("ST-001")
        assertTrue(r is ApiResult.NetworkError)
    }

    @Test
    fun 형식이_다른_응답은_형식_오류() = runBlocking {
        val r = AlterChargeApi(base, FakeHttp { _, _, _ -> HttpResponse(200, """{"unexpected":true}""") }).navigationCheck("ST-001")
        assertTrue(r is ApiResult.ParseError)
    }

    @Test
    fun 주소가_비어_있으면_호출하지_않는다() = runBlocking {
        val http = FakeHttp { _, _, _ -> HttpResponse(200, "{}") }
        val r = AlterChargeApi("", http).stations(37.5, 127.0)
        assertEquals(ApiResult.NotConfigured, r)
        assertTrue(http.calls.isEmpty())
    }

    @Test
    fun 저장은_POST로_본문을_보낸다() = runBlocking {
        val http = FakeHttp { _, _, _ -> HttpResponse(201, """{"snapshotId":"s1","saved":true,"recordedAt":"2026-11-12T05:30:00Z"}""") }
        val r = AlterChargeApi(base, http).postSnapshot(
            com.altercharge.app.data.remote.model.VehicleSnapshotRequest(
                DEMO_VEHICLE_ID, java.time.Instant.parse("2026-11-12T05:30:00Z"), 62, 20350.4,
            ),
        )
        assertTrue(r is ApiResult.Success)
        val (method, url, body) = http.calls.single()
        assertEquals("POST", method)
        assertEquals("${base}vehicles/snapshots", url)
        assertTrue(body!!.contains("\"vehicleId\":\"mock-vehicle-001\""))
    }

    @Test
    fun 누적거리가_NaN이면_보내지_않고_형식_오류() = runBlocking {
        val http = FakeHttp { _, _, _ -> HttpResponse(201, "{}") }
        val r = AlterChargeApi(base, http).postSnapshot(
            com.altercharge.app.data.remote.model.VehicleSnapshotRequest(
                DEMO_VEHICLE_ID, java.time.Instant.parse("2026-11-12T05:30:00Z"), 62, Double.NaN,
            ),
        )
        assertTrue(r is ApiResult.ParseError)
        assertTrue(http.calls.isEmpty())
    }

    @Test
    fun 충전소_ID는_경로에_인코딩() = runBlocking {
        val http = FakeHttp { _, _, _ -> HttpResponse(404, """{"error":"STATION_NOT_FOUND","message":"없음"}""") }
        AlterChargeApi(base, http).stationDetail("A/B")
        assertEquals("${base}stations/A%2FB", http.calls.single().second)
    }
}
