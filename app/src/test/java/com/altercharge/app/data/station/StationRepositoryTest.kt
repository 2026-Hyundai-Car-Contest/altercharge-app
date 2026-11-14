package com.altercharge.app.data.station

import com.altercharge.app.data.local.KeyValueStore
import com.altercharge.app.data.local.StationCacheStore
import com.altercharge.app.data.local.VisitCountStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.HttpClient
import com.altercharge.app.data.remote.HttpResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class StationRepositoryTest {

    private class MemoryStore : KeyValueStore {
        val map = mutableMapOf<String, String>()
        override fun getString(key: String): String? = map[key]
        override fun putStrings(values: Map<String, String?>) {
            values.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }
    }

    private class FakeServer : HttpClient {
        var online = true
        val urls = mutableListOf<String>()
        override fun request(method: String, url: String, body: String?): HttpResponse {
            urls += url
            if (!online) throw IOException("offline")
            return HttpResponse(
                200,
                """{"items":[{"stationId":"ST1","name":"가까운 곳","address":"a","lat":0,"lng":0,"distanceMeters":420,
                "availableCount":1,"totalValidCount":1,"chargerTypes":["DC_COMBO"],"maxOutputKw":50.0,"visitCount":null,
                "statusObservedAt":null}],"page":0,"size":200,"hasNext":false,"fetchedAt":"2026-11-14T05:00:00Z"}""",
            )
        }
    }

    /** 테스트마다 시각을 옮길 수 있는 시계 */
    private class MovableClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = now
    }

    private val server = FakeServer()
    private val clock = MovableClock(Instant.parse("2026-11-14T05:00:00Z"))
    private val memory = MemoryStore()
    private val repository = StationRepository(
        api = AlterChargeApi("https://example.test/api/v1/", server),
        cache = StationCacheStore(memory),
        visits = VisitCountStore(memory),
        clock = clock,
    )

    @Test
    fun 삼십초_안에_같은_위치를_다시_부르면_받아_둔_목록을_쓴다() = runBlocking {
        repository.nearby(37.5, 127.0)
        repository.nearby(37.5, 127.0)
        assertEquals(1, server.urls.size)
    }

    @Test
    fun 동시에_불러도_한_번만_받는다() = runBlocking {
        val a = async { repository.nearby(37.5, 127.0) }
        val b = async { repository.nearby(37.5, 127.0) }
        a.await(); b.await()
        assertEquals(1, server.urls.size)
    }

    @Test
    fun 삼십초가_지나거나_위치가_바뀌면_다시_받는다() = runBlocking {
        repository.nearby(37.5, 127.0)
        clock.now = clock.now.plus(Duration.ofSeconds(31))
        repository.nearby(37.5, 127.0)
        repository.nearby(37.6, 127.0)
        assertEquals(3, server.urls.size)
    }

    @Test
    fun 검색은_매번_서버에_묻는다() = runBlocking {
        repository.nearby(37.5, 127.0, keyword = "시청")
        repository.nearby(37.5, 127.0, keyword = "시청")
        assertEquals(2, server.urls.size)
    }

    @Test
    fun 오프라인으로_받은_값은_다시_쓰지_않는다() = runBlocking {
        server.online = false
        repository.nearby(37.5, 127.0)
        server.online = true
        repository.nearby(37.5, 127.0)
        assertEquals(2, server.urls.size)
    }
}
