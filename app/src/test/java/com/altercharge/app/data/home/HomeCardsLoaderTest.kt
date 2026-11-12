package com.altercharge.app.data.home

import com.altercharge.app.data.local.KeyValueStore
import com.altercharge.app.data.local.OfflineSnapshotStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.HttpClient
import com.altercharge.app.data.remote.HttpResponse
import com.altercharge.app.domain.time.KoreaZone
import com.altercharge.app.ui.card.common.ChargeVerdict
import com.altercharge.app.ui.card.info.LastChecked
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class HomeCardsLoaderTest {

    private class MemoryStore : KeyValueStore {
        val map = mutableMapOf<String, String>()
        override fun getString(key: String): String? = map[key]
        override fun putStrings(values: Map<String, String?>) {
            values.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }
    }

    /** 경로별 가짜 서버. online = false면 모든 요청이 연결 실패 */
    private class FakeServer(var online: Boolean = true) : HttpClient {
        var snapshots = """{"vehicleId":"v","records":[],"retentionDays":56,"completeDayCount":0}"""
        var latest = """{"vehicleId":"v","lastChargeCompletedAt":null,"odometerAtLastChargeKm":null,"currentOdometerKm":null,
            "distanceSinceChargeKm":null,"source":null,"trusted":false}"""
        var recommendation = """{"station":{"stationId":"ST1","name":"예시 공영주차장","distanceMeters":900,"availableCount":1,
            "visitCount":null,"speedCategory":"FAST","maxOutputKw":100.0},"reasonCode":"VISITS_THEN_DISTANCE","fallbackUsed":false,
            "statusObservedAt":null}"""
        var stations = """{"items":[{"stationId":"ST2","name":"가까운 곳","address":"a","lat":0,"lng":0,"distanceMeters":420,
            "availableCount":1,"totalValidCount":1,"chargerTypes":["DC_COMBO"],"maxOutputKw":50.0,"visitCount":null,
            "statusObservedAt":null}],"page":0,"size":5,"hasNext":false,"fetchedAt":"2026-11-12T05:00:00Z"}"""
        val urls = mutableListOf<String>()

        override fun request(method: String, url: String, body: String?): HttpResponse {
            urls += url
            if (!online) throw IOException("offline")
            val text = when {
                "vehicles/snapshots" in url -> snapshots
                "charging-events/latest" in url -> latest
                "stations/recommendation" in url -> recommendation
                "stations?" in url -> stations
                else -> return HttpResponse(404, "")
            }
            return HttpResponse(200, text)
        }
    }

    private val now: Instant = Instant.parse("2026-11-12T05:30:00Z") // 한국 11.12(목) 14:30
    private val today: LocalDate = LocalDate.of(2026, 11, 12)
    private val server = FakeServer()
    private val memory = MemoryStore()
    private val store = OfflineSnapshotStore(memory)

    private fun loader(at: Instant = now) =
        HomeCardsLoader(AlterChargeApi("https://example.test/api/v1/", server), store, Clock.fixed(at, ZoneOffset.UTC))

    private val vehicle = VehicleReading(socPercent = 70, rangeKm = 300, odometerKm = 1100.0)
    private val here = LatLng(37.55, 126.92)

    /** 14일 동안 하루 20km씩. 마지막 기록은 어제 22시 */
    private fun fourteenDays(): String {
        val records = (14 downTo 1).flatMap { d ->
            val date = today.minusDays(d.toLong())
            val start = 1100.0 - d * 20
            listOf(date.atTime(7, 0) to start, date.atTime(22, 0) to start + 20)
        }.mapIndexed { i, (time, km) ->
            """{"snapshotId":"s$i","recordedAt":"${time.atZone(KoreaZone).toInstant()}","socPercent":70,"odometerKm":$km,"drivableRangeKm":null}"""
        }
        return """{"vehicleId":"v","records":[${records.joinToString(",")}],"retentionDays":56,"completeDayCount":14}"""
    }

    @Test
    fun 온라인이면_서버_값으로_카드를_만든다() = runBlocking {
        server.snapshots = fourteenDays()
        val cards = loader().load(vehicle, here)

        // L1: 판정 카드(수집 중 아님), 추천 충전소 이름, 방금 받았으니 마지막 확인 시각 없음
        assertNull(cards.cardInfo.recordedDays)
        assertEquals("예시 공영주차장", cards.cardInfo.placeName)
        assertNull(cards.cardInfo.placeLastChecked)
        // C: 가까운 충전소는 목록 기준(420m), 추천(900m)이 아니다
        assertEquals(0.4, cards.vehicleStatus.nearestStationKm!!, 0.0001)
        assertNull(cards.vehicleStatus.lastChargedAt)
        assertFalse(cards.offline)
        // 판정 · 충전소 저장
        val saved = store.load()
        assertTrue(saved.judgment != null && saved.judgmentStamp != null)
        assertEquals(now, saved.stationReceivedAt)
    }

    @Test
    fun 같은_날_다시_불러도_판정은_저장한_값을_쓴다() = runBlocking {
        server.snapshots = fourteenDays()
        val first = loader().load(vehicle, here)
        // 주행가능거리가 크게 줄어도 같은 날 · 예비 거리 안이면 판정 유지 (REQ_JD_004)
        val second = loader(now.plusSeconds(600)).load(vehicle.copy(rangeKm = 40, odometerKm = 1105.0), here)
        assertEquals(first.cardInfo.verdict, second.cardInfo.verdict)
        assertEquals(first.cardInfo.daysUntilCharge, second.cardInfo.daysUntilCharge)
    }

    @Test
    fun 다음_날에는_다시_계산한다() = runBlocking {
        server.snapshots = fourteenDays()
        loader().load(vehicle, here)
        val nextDay = loader(now.plusSeconds(24 * 3600)).load(vehicle.copy(rangeKm = 40), here)
        assertEquals(ChargeVerdict.TODAY, nextDay.cardInfo.verdict)
    }

    @Test
    fun 오프라인이면_저장한_충전소와_N분_전_기준() = runBlocking {
        server.snapshots = fourteenDays() // 판정 카드여야 장소 줄이 있다 (수집 중 카드는 장소 줄 없음)
        loader().load(vehicle, here)
        server.online = false
        val cards = loader(now.plusSeconds(10 * 60)).load(vehicle, here)
        assertTrue(cards.offline)
        assertEquals("예시 공영주차장", cards.cardInfo.placeName)
        assertEquals(LastChecked.MinutesAgo(10), cards.cardInfo.placeLastChecked)
        assertEquals(0.4, cards.vehicleStatus.nearestStationKm!!, 0.0001)
    }

    @Test
    fun 받아_둔_충전소가_7일을_넘으면_비운다() = runBlocking {
        loader().load(vehicle, here)
        server.online = false
        val cards = loader(now.plusSeconds(8 * 24 * 3600)).load(vehicle, here)
        assertEquals("", cards.cardInfo.placeName) // L1 장소 줄 숨김
        assertNull(cards.vehicleStatus.nearestStationKm)
    }

    @Test
    fun 처음부터_오프라인이면_기록_수집_중과_빈_충전소() = runBlocking {
        server.online = false
        val cards = loader().load(vehicle, here)
        assertTrue(cards.offline)
        assertEquals(0, cards.cardInfo.recordedDays)
        assertEquals("", cards.cardInfo.placeName)
        assertTrue(cards.driveRecord.empty)
        assertNull(store.load().judgment) // 못 받은 상태의 판정은 저장하지 않는다
    }

    @Test
    fun 위치를_모르면_충전소는_부르지_않고_저장값을_쓴다() = runBlocking {
        server.snapshots = fourteenDays()
        loader().load(vehicle, here)
        server.urls.clear()
        val cards = loader(now.plusSeconds(60)).load(vehicle, location = null)
        assertTrue(server.urls.none { "stations" in it })
        assertEquals(LastChecked.MinutesAgo(1), cards.cardInfo.placeLastChecked)
        assertFalse(cards.offline) // 위치가 없어 안 부른 것은 오프라인이 아니다
    }

    @Test
    fun 마지막_충전이_있으면_C_시각_글자와_L2_값() = runBlocking {
        server.latest = """{"vehicleId":"v","lastChargeCompletedAt":"2026-11-11T22:30:00Z","odometerAtLastChargeKm":1000.0,
            "currentOdometerKm":1040.0,"distanceSinceChargeKm":40.0,"source":"MOCK","trusted":false}"""
        val cards = loader().load(vehicle, here)
        assertEquals("07:30", cards.vehicleStatus.lastChargedAt) // 한국 11.12 07:30 = 오늘
        assertEquals(0, cards.driveRecord.daysSinceLastCharge)
        assertEquals(40, cards.driveRecord.kmSinceLastCharge)
    }

    @Test
    fun 지난_주_보기는_받아_둔_기록으로_서버를_다시_부르지_않는다() = runBlocking {
        server.snapshots = fourteenDays()
        val l = loader()
        val thisWeek = l.load(vehicle, here)
        server.urls.clear()

        val lastWeek = l.driveRecordFor(thisWeek.driveRecord.weekStart.minusWeeks(1), today, expanded = true)
        assertTrue(server.urls.isEmpty())
        assertEquals(LocalDate.of(2026, 11, 2), lastWeek.weekStart)
        assertEquals(List(7) { 20 }, lastWeek.dailyKm)
        assertNull(lastWeek.todayIndex)
        assertTrue(lastWeek.hasNextWeek)
        assertTrue(lastWeek.expanded)
    }
}
