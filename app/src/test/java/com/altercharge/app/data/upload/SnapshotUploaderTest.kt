package com.altercharge.app.data.upload

import com.altercharge.app.data.home.VehicleReading
import com.altercharge.app.data.local.KeyValueStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.HttpClient
import com.altercharge.app.data.remote.HttpResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Duration
import java.time.Instant

class SnapshotUploaderTest {

    private class MemoryStore : KeyValueStore {
        val map = mutableMapOf<String, String>()
        override fun getString(key: String): String? = map[key]
        override fun putStrings(values: Map<String, String?>) {
            values.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }
    }

    private class FakeServer : HttpClient {
        var respond: () -> HttpResponse = { HttpResponse(201, """{"snapshotId":"s","saved":true,"recordedAt":"2026-11-12T00:00:00Z"}""") }
        val bodies = mutableListOf<String>()
        override fun request(method: String, url: String, body: String?): HttpResponse {
            bodies += body.orEmpty()
            return respond()
        }
    }

    private val t0: Instant = Instant.parse("2026-11-12T01:00:00Z") // 한국 10:00
    private val server = FakeServer()
    private val memory = MemoryStore()
    private val uploader = SnapshotUploader(AlterChargeApi("https://example.test/api/v1/", server), memory)
    private val reading = VehicleReading(socPercent = 70, rangeKm = 300, odometerKm = 1000.5)

    // ---- 보낼 때 ----

    @Test
    fun 처음이면_보낸다() = assertTrue(isDue(null, t0))

    @Test
    fun 삼십분이_지나야_다시_보낸다() {
        assertFalse(isDue(t0, t0.plus(Duration.ofMinutes(29))))
        assertTrue(isDue(t0, t0.plus(Duration.ofMinutes(30))))
    }

    @Test
    fun 한국시간_날짜가_바뀌면_바로_보낸다() {
        val beforeMidnight = Instant.parse("2026-11-12T14:55:00Z") // 23:55
        assertTrue(isDue(beforeMidnight, beforeMidnight.plus(Duration.ofMinutes(10)))) // 00:05
    }

    // ---- 대기열 ----

    @Test
    fun 자주_불러도_삼십분에_한_번만_쌓인다() {
        uploader.record(reading, t0)
        uploader.record(reading, t0.plus(Duration.ofMinutes(10)))
        uploader.record(reading, t0.plus(Duration.ofMinutes(31)))
        assertEquals(2, uploader.pendingCount())
    }

    @Test
    fun 보내면_대기열이_빈다() = runBlocking {
        uploader.record(reading, t0)
        assertEquals(1, uploader.flush(t0))
        assertEquals(0, uploader.pendingCount())
        assertTrue(server.bodies.single().contains("\"odometerKm\":1000.5"))
    }

    @Test
    fun 연결이_안_되면_남겨_두고_다음에_보낸다() = runBlocking {
        uploader.record(reading, t0)
        uploader.record(reading, t0.plus(Duration.ofHours(1)))
        server.respond = { throw IOException("offline") }
        assertEquals(0, uploader.flush(t0.plus(Duration.ofHours(1))))
        assertEquals(2, uploader.pendingCount())

        server.respond = { HttpResponse(201, """{"snapshotId":"s","saved":true,"recordedAt":"2026-11-12T00:00:00Z"}""") }
        assertEquals(2, uploader.flush(t0.plus(Duration.ofHours(2))))
        assertEquals(0, uploader.pendingCount())
    }

    @Test
    fun 서버_오류_5xx도_남겨_둔다() = runBlocking {
        uploader.record(reading, t0)
        server.respond = { HttpResponse(503, """{"error":"SERVICE_UNAVAILABLE","message":"x"}""") }
        uploader.flush(t0)
        assertEquals(1, uploader.pendingCount())
    }

    @Test
    fun 서버가_거절한_값은_버리고_다음으로() = runBlocking {
        uploader.record(reading, t0)
        uploader.record(reading, t0.plus(Duration.ofHours(1)))
        var call = 0
        server.respond = {
            if (call++ == 0) HttpResponse(409, """{"error":"SNAPSHOT_CONFLICT","message":"x"}""")
            else HttpResponse(201, """{"snapshotId":"s","saved":true,"recordedAt":"2026-11-12T00:00:00Z"}""")
        }
        assertEquals(1, uploader.flush(t0.plus(Duration.ofHours(1))))
        assertEquals(0, uploader.pendingCount())
    }

    @Test
    fun 서버_보관_기간보다_오래된_값은_보내지_않는다() = runBlocking {
        uploader.record(reading, t0)
        assertEquals(0, uploader.flush(t0.plus(Duration.ofDays(60))))
        assertTrue(server.bodies.isEmpty())
        assertEquals(0, uploader.pendingCount())
    }

    @Test
    fun 앱을_다시_켜도_대기열이_남아_있다() {
        uploader.record(reading, t0)
        val reopened = SnapshotUploader(AlterChargeApi("https://example.test/api/v1/", server), memory)
        assertEquals(1, reopened.pendingCount())
    }

    @Test
    fun 요청이_너무_많으면_429_남겨_둔다() = runBlocking {
        uploader.record(reading, t0)
        server.respond = { HttpResponse(429, "") }
        uploader.flush(t0)
        assertEquals(1, uploader.pendingCount())
    }

    @Test
    fun 보내는_동안_새로_쌓인_값은_지우지_않는다() = runBlocking {
        uploader.record(reading, t0)
        server.respond = {
            // 전송 중에 다음 값이 들어온다
            uploader.record(reading, t0.plus(Duration.ofHours(1)))
            HttpResponse(201, """{"snapshotId":"s","saved":true,"recordedAt":"2026-11-12T00:00:00Z"}""")
        }
        assertEquals(1, uploader.flush(t0.plus(Duration.ofHours(1))))
        assertEquals(1, uploader.pendingCount())
    }
}
