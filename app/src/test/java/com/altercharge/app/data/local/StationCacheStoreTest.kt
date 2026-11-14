package com.altercharge.app.data.local

import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.StationSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** 오프라인 목록 저장 (REQ_CM_006) */
class StationCacheStoreTest {

    private class MemoryStore : KeyValueStore {
        val map = mutableMapOf<String, String>()
        override fun getString(key: String): String? = map[key]
        override fun putStrings(values: Map<String, String?>) {
            values.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }
    }

    private val station = StationSummary(
        stationId = "NT101224",
        name = "강동 암사e편한세상아파트",
        address = "서울특별시 강동구 구천면로 365-13",
        lat = 37.5511368,
        lng = 127.13926,
        distanceMeters = 142,
        availableCount = 1,
        totalValidCount = 3,
        chargerTypes = listOf(ConnectorType.AC_SLOW, ConnectorType.DC_COMBO),
        maxOutputKw = 7.0,
        visitCount = null,
        statusObservedAt = Instant.parse("2026-11-14T09:33:41Z"),
    )

    @Test
    fun 저장한_목록과_받은_시각을_그대로_읽는다() {
        val store = MemoryStore()
        val cache = StationCacheStore(store)
        val at = Instant.parse("2026-11-14T09:40:00Z")
        cache.save(listOf(station), at)

        val loaded = cache.load()
        assertNotNull(loaded)
        assertEquals(at, loaded!!.receivedAt)
        assertEquals(1, loaded.stations.size)
        assertEquals(station, loaded.stations.single())
    }

    @Test
    fun 저장한_값이_없으면_null() {
        assertNull(StationCacheStore(MemoryStore()).load())
    }

    @Test
    fun 깨진_줄은_건너뛰고_남은_목록을_읽는다() {
        val store = MemoryStore()
        StationCacheStore(store).save(listOf(station), Instant.parse("2026-11-14T09:40:00Z"))
        val key = "station_cache"
        store.putStrings(mapOf(key to "broken\n" + store.map.getValue(key)))

        val loaded = StationCacheStore(store).load()
        assertEquals(listOf(station), loaded!!.stations)
    }

    @Test
    fun 출력이_없는_충전소도_왕복한다() {
        val noPower = station.copy(stationId = "X", maxOutputKw = null, visitCount = 3, statusObservedAt = null)
        assertEquals(listOf(noPower), decodeStations(encodeStations(listOf(noPower))))
    }

    @Test
    fun 모르는_연결타입은_UNKNOWN으로_읽는다() {
        val encoded = encodeStations(listOf(station)).replace("AC_SLOW", "FUTURE_TYPE")
        assertTrue(ConnectorType.UNKNOWN in decodeStations(encoded).single().chargerTypes)
    }

    @Test
    fun 빈_목록은_빈_문자열로_저장한다() {
        assertEquals(emptyList<StationSummary>(), decodeStations(encodeStations(emptyList())))
    }
}
