package com.altercharge.app.data.remote

import com.altercharge.app.data.remote.model.ChargerStatus
import com.altercharge.app.data.remote.model.ChargingEventRequest
import com.altercharge.app.data.remote.model.ChargingEventType
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.NavigationReason
import com.altercharge.app.data.remote.model.RecommendationReason
import com.altercharge.app.data.remote.model.SpeedCategory
import com.altercharge.app.data.remote.model.VehicleSnapshotRequest
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** 응답 예시는 서버 OpenAPI(/v3/api-docs, 2026-11-12)의 examples 그대로다 */
class ServerJsonTest {

    @Test
    fun 충전소_목록() {
        val page = ServerJson.stationPage(
            """{"items":[{"stationId":"ST-001","name":"예시 충전소","address":"서울특별시 예시 주소","lat":37.551,"lng":127.141,
            "distanceMeters":142,"availableCount":1,"totalValidCount":2,"chargerTypes":["AC_SLOW","DC_COMBO"],"maxOutputKw":100.0,
            "visitCount":null,"statusObservedAt":"2026-11-11T01:30:00+09:00"}],"page":0,"size":20,"hasNext":false,
            "fetchedAt":"2026-11-11T01:31:00+09:00"}""",
        )
        val s = page.items.single()
        assertEquals("예시 충전소", s.name)
        assertEquals(142, s.distanceMeters)
        assertEquals(listOf(ConnectorType.AC_SLOW, ConnectorType.DC_COMBO), s.chargerTypes)
        assertEquals(100.0, s.maxOutputKw!!, 0.0)
        assertNull(s.visitCount)
        assertEquals(Instant.parse("2026-11-10T16:30:00Z"), s.statusObservedAt)
        assertEquals(Instant.parse("2026-11-10T16:31:00Z"), page.fetchedAt)
        assertFalse(page.hasNext)
    }

    @Test
    fun 반경_안에_없으면_빈_목록() {
        val page = ServerJson.stationPage("""{"items":[],"page":0,"size":20,"hasNext":false,"fetchedAt":"2026-11-11T01:31:00+09:00"}""")
        assertTrue(page.items.isEmpty())
    }

    @Test
    fun 모르는_연결_규격은_UNKNOWN() {
        val page = ServerJson.stationPage(
            """{"items":[{"stationId":"A","name":"n","address":"a","lat":0,"lng":0,"distanceMeters":1,"availableCount":0,
            "totalValidCount":0,"chargerTypes":["NEW_TYPE"],"maxOutputKw":null,"visitCount":3,"statusObservedAt":null}],
            "page":0,"size":20,"hasNext":true,"fetchedAt":"2026-11-11T01:31:00Z"}""",
        )
        assertEquals(listOf(ConnectorType.UNKNOWN), page.items.single().chargerTypes)
        assertEquals(3, page.items.single().visitCount)
        assertNull(page.items.single().statusObservedAt)
    }

    @Test
    fun 충전소_상세() {
        val d = ServerJson.stationDetail(
            """{"stationId":"ST-001","name":"예시 충전소","address":"서울특별시 예시 주소","lat":37.551,"lng":127.141,
            "availableCount":1,"totalValidCount":2,"chargers":[
            {"chargerId":"01","connectorType":"DC_COMBO","connectorTypes":["DC_COMBO"],"chgerType":"04","outputKw":100.0,
             "status":"AVAILABLE","stat":"2","statusUpdatedAt":"2026-11-11T01:30:00+09:00","statusObservedAt":"2026-11-11T01:30:00+09:00"},
            {"chargerId":"02","connectorType":"AC_SLOW","connectorTypes":["AC_SLOW"],"chgerType":"02","outputKw":null,
             "status":"CHARGING","stat":"3","statusUpdatedAt":"2026-11-11T01:29:00+09:00","statusObservedAt":"2026-11-11T01:30:00+09:00"}],
            "statusObservedAt":"2026-11-11T01:30:00+09:00"}""",
        )
        assertEquals(listOf("01", "02"), d.chargers.map { it.chargerId }) // 앞자리 0 보존
        assertEquals(ChargerStatus.CHARGING, d.chargers[1].status)
        assertNull(d.chargers[1].outputKw)
    }

    @Test
    fun 길_안내_확인() {
        val ok = ServerJson.navigationCheck(
            """{"stationId":"ST-001","navigationAllowed":true,"reasonCode":"OK","latestStatuses":["AVAILABLE","CHARGING"],"checkedAt":"2026-11-11T01:31:00+09:00"}""",
        )
        assertTrue(ok.navigationAllowed)
        assertEquals(NavigationReason.OK, ok.reasonCode)

        val stale = ServerJson.navigationCheck(
            """{"stationId":"ST-001","navigationAllowed":false,"reasonCode":"STATUS_STALE","latestStatuses":["AVAILABLE"],"checkedAt":"2026-11-11T02:00:00+09:00"}""",
        )
        assertEquals(NavigationReason.STATUS_STALE, stale.reasonCode)
    }

    @Test
    fun 모르는_길_안내_이유는_막는_쪽으로() {
        val c = ServerJson.navigationCheck(
            """{"stationId":"A","navigationAllowed":false,"reasonCode":"SOMETHING_NEW","latestStatuses":[],"checkedAt":"2026-11-11T02:00:00Z"}""",
        )
        assertEquals(NavigationReason.STATUS_UNKNOWN, c.reasonCode)
    }

    @Test
    fun 추천_충전소_세_가지() {
        val available = ServerJson.recommendation(
            """{"station":{"stationId":"ST-001","name":"예시 충전소","distanceMeters":150,"availableCount":1,"visitCount":null,
            "speedCategory":"FAST","maxOutputKw":100.0},"reasonCode":"VISITS_THEN_DISTANCE","fallbackUsed":false,
            "statusObservedAt":"2026-11-11T01:30:00+09:00"}""",
        )
        assertEquals(SpeedCategory.FAST, available.station!!.speedCategory)
        assertEquals(RecommendationReason.VISITS_THEN_DISTANCE, available.reasonCode)

        val fallback = ServerJson.recommendation(
            """{"station":{"stationId":"ST-001","name":"예시 충전소","distanceMeters":150,"availableCount":0,"visitCount":null,
            "speedCategory":null,"maxOutputKw":null},"reasonCode":"NEAREST_FALLBACK","fallbackUsed":true,
            "statusObservedAt":"2026-11-11T01:30:00+09:00"}""",
        )
        assertTrue(fallback.fallbackUsed)
        assertNull(fallback.station!!.speedCategory)

        val none = ServerJson.recommendation(
            """{"station":null,"reasonCode":"NO_STATIONS_IN_RADIUS","fallbackUsed":false,"statusObservedAt":null}""",
        )
        assertNull(none.station)
        assertEquals(RecommendationReason.NO_STATIONS_IN_RADIUS, none.reasonCode)
    }

    @Test
    fun 모르는_추천_근거는_UNKNOWN() {
        val r = ServerJson.recommendation(
            """{"station":{"stationId":"A","name":"n","distanceMeters":1,"availableCount":1,"visitCount":null,
            "speedCategory":null,"maxOutputKw":null},"reasonCode":"NEW_REASON","fallbackUsed":false,"statusObservedAt":null}""",
        )
        assertEquals(RecommendationReason.UNKNOWN, r.reasonCode)
        assertEquals("A", r.station!!.stationId)
    }

    @Test
    fun 차량_스냅샷_기록() {
        val h = ServerJson.snapshotHistory(
            """{"vehicleId":"mock-vehicle-001","records":[{"snapshotId":"e2f6","recordedAt":"2026-11-10T16:30:00Z",
            "socPercent":62,"odometerKm":20350.4,"drivableRangeKm":null}],"retentionDays":56,"completeDayCount":0}""",
        )
        val r = h.records.single()
        assertEquals(62, r.socPercent)
        assertEquals(20350.4, r.odometerKm, 0.0)
        assertNull(r.drivableRangeKm)
        assertEquals(56, h.retentionDays)
    }

    @Test
    fun 스냅샷_저장_응답() {
        val s = ServerJson.snapshotSaved("""{"snapshotId":"e2f6","saved":true,"recordedAt":"2026-11-10T16:30:00Z"}""")
        assertTrue(s.saved)
    }

    @Test
    fun 충전_이벤트_응답과_최근_충전() {
        val a = ServerJson.chargingEventAccepted(
            """{"eventId":"evt-demo-001","accepted":true,"matchedStationId":null,"visitCountAdded":0,"dailyDuplicate":false}""",
        )
        assertTrue(a.accepted)
        assertNull(a.matchedStationId)

        val latest = ServerJson.latestChargeHistory(
            """{"vehicleId":"mock-vehicle-001","lastChargeCompletedAt":"2026-11-10T16:00:00Z","odometerAtLastChargeKm":20310.0,
            "currentOdometerKm":20350.4,"distanceSinceChargeKm":40.4,"source":"MOCK","trusted":false}""",
        )
        assertEquals(40.4, latest.distanceSinceChargeKm!!, 0.0)

        val none = ServerJson.latestChargeHistory(
            """{"vehicleId":"mock-vehicle-001","lastChargeCompletedAt":null,"odometerAtLastChargeKm":null,"currentOdometerKm":null,
            "distanceSinceChargeKm":null,"source":null,"trusted":false}""",
        )
        assertNull(none.lastChargeCompletedAt)
    }

    @Test(expected = JSONException::class)
    fun 필수_값이_없으면_예외() {
        ServerJson.snapshotSaved("""{"saved":true}""")
    }

    @Test(expected = JSONException::class)
    fun 필수_문자열이_null이면_예외() {
        ServerJson.snapshotSaved("""{"snapshotId":null,"saved":true,"recordedAt":"2026-11-10T16:30:00Z"}""")
    }

    @Test
    fun 오류_본문() {
        val e = ServerJson.apiErrorOrNull("""{"error":"VEHICLE_NOT_FOUND","message":"해당 차량을 찾을 수 없습니다."}""")
        assertEquals("VEHICLE_NOT_FOUND", e!!.error)
        assertNull(ServerJson.apiErrorOrNull("<html>502</html>"))
        assertNull(ServerJson.apiErrorOrNull(null))
    }

    @Test
    fun 스냅샷_요청_본문() {
        val json = JSONObject(
            ServerJson.snapshotRequest(
                VehicleSnapshotRequest("mock-vehicle-001", Instant.parse("2026-11-12T05:30:00Z"), 62, 20350.4),
            ),
        )
        assertEquals("mock-vehicle-001", json.getString("vehicleId"))
        assertEquals("2026-11-12T05:30:00Z", json.getString("recordedAt"))
        assertEquals(20350.4, json.getDouble("odometerKm"), 0.0)
        assertTrue(json.isNull("drivableRangeKm"))
    }

    @Test
    fun 충전_이벤트_요청은_MOCK으로_보낸다() {
        val json = JSONObject(
            ServerJson.chargingEventRequest(
                ChargingEventRequest(
                    eventId = "evt-1",
                    vehicleId = "mock-vehicle-001",
                    eventType = ChargingEventType.COMPLETE,
                    occurredAt = Instant.parse("2026-11-12T05:30:00Z"),
                    lat = 37.55,
                    lng = 126.92,
                    socPercent = 80,
                ),
            ),
        )
        assertEquals("COMPLETE", json.getString("eventType"))
        assertEquals("MOCK", json.getString("source"))
        assertTrue(json.isNull("odometerKm"))
    }
}
