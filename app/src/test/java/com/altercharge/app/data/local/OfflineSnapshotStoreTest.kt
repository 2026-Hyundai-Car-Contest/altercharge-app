package com.altercharge.app.data.local

import com.altercharge.app.domain.judgment.Judgment
import com.altercharge.app.domain.judgment.JudgmentResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class OfflineSnapshotStoreTest {

    private class MemoryStore : KeyValueStore {
        val map = mutableMapOf<String, String>()
        override fun getString(key: String): String? = map[key]
        override fun putStrings(values: Map<String, String?>) {
            values.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }
    }

    private val memory = MemoryStore()
    private val store = OfflineSnapshotStore(memory)

    @Test
    fun 저장_전에는_모두_null() {
        assertEquals(OfflineSnapshot(null, null, null, null), store.load())
    }

    @Test
    fun 판정_결과를_저장하고_다시_읽는다() {
        val ready = JudgmentResult.Ready(Judgment(averageDailyKm = 37, reserveKm = 35, daysLeft = 5, chargeRecommended = false))
        store.saveJudgment(ready)
        assertEquals(ready, store.load().judgment)

        val collecting = JudgmentResult.Collecting(recordedDays = 3, chargeRecommended = true)
        store.saveJudgment(collecting)
        assertEquals(collecting, store.load().judgment)
    }

    @Test
    fun 추천_충전소와_받은_시각을_같이_저장한다() {
        val at = Instant.parse("2026-11-12T05:20:00Z")
        store.saveStation("예시 공영주차장", 1.2, at)
        val s = store.load()
        assertEquals("예시 공영주차장", s.placeName)
        assertEquals(1.2, s.nearestStationKm!!, 0.0001)
        assertEquals(at, s.stationReceivedAt)
    }

    @Test
    fun 거리가_없으면_지운다() {
        store.saveStation("A", 1.2, Instant.EPOCH)
        store.saveStation("", null, Instant.EPOCH)
        assertEquals("", store.load().placeName)
        assertNull(store.load().nearestStationKm)
    }

    @Test
    fun 깨진_값은_저장_전처럼_null() {
        memory.map["judgment"] = "ready|abc"
        memory.map["station_received_at"] = "not-a-time"
        assertNull(store.load().judgment)
        assertNull(store.load().stationReceivedAt)
        assertNull(decodeJudgment("unknown|1"))
    }
}
