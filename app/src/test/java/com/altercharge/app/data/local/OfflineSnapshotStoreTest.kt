package com.altercharge.app.data.local

import com.altercharge.app.data.home.RecommendedStationInfo
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.RecommendationReason
import com.altercharge.app.data.remote.model.SpeedCategory
import com.altercharge.app.domain.judgment.Judgment
import com.altercharge.app.domain.judgment.JudgmentResult
import com.altercharge.app.domain.judgment.recomputeReason
import com.altercharge.app.domain.judgment.stamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

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
        assertEquals(OfflineSnapshot(null, null, null, null, null), store.load())
    }

    private val at = Instant.parse("2026-11-12T00:30:00Z")

    @Test
    fun 판정_결과와_계산_기록을_같이_저장하고_다시_읽는다() {
        val ready = JudgmentResult.Ready(Judgment(averageDailyKm = 37, reserveKm = 35, daysLeft = 5, chargeRecommended = false))
        val readyStamp = ready.stamp(at, todayKm = 12)
        store.saveJudgment(ready, readyStamp)
        assertEquals(ready, store.load().judgment)
        assertEquals(readyStamp, store.load().judgmentStamp)

        val collecting = JudgmentResult.Collecting(recordedDays = 3, chargeRecommended = true)
        val collectingStamp = collecting.stamp(at, todayKm = 0) // 예비 거리 없음
        store.saveJudgment(collecting, collectingStamp)
        assertEquals(collecting, store.load().judgment)
        assertEquals(collectingStamp, store.load().judgmentStamp)
    }

    @Test
    fun 다시_켜도_같은_날이면_판정을_유지한다() {
        // REQ_JD_004: 저장한 계산 기록으로 판단하므로 앱을 다시 켜도 NEVER_COMPUTED가 아니다
        val ready = JudgmentResult.Ready(Judgment(averageDailyKm = 37, reserveKm = 35, daysLeft = 5, chargeRecommended = false))
        store.saveJudgment(ready, ready.stamp(at, todayKm = 12))
        val reopened = OfflineSnapshotStore(memory).load()
        assertNull(recomputeReason(reopened.judgmentStamp, LocalDate.of(2026, 11, 12), todayKm = 20, lastChargeCompletedAt = null))
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
        memory.map["judgment_stamp"] = "2026-11-12|bad"
        assertNull(store.load().judgment)
        assertNull(store.load().stationReceivedAt)
        assertNull(store.load().judgmentStamp)
        assertNull(decodeJudgment("unknown|1"))
    }

    @Test
    fun 추천_충전소를_모든_값_그대로_다시_읽는다() {
        // 충전기 목록(chargers)은 저장하지 않는다: 연결 타입은 connectorTypes로 남긴다
        val info = RecommendedStationInfo(
            stationId = "ST1", name = "예시 충전소", distanceMeters = 1200, availableCount = 3, visitCount = 2,
            speedCategory = SpeedCategory.FAST, maxOutputKw = 100.0, fallbackUsed = true,
            reasonCode = RecommendationReason.NEAREST_FALLBACK, address = "예시 주소", totalValidCount = 4,
            connectorTypes = listOf(ConnectorType.DC_COMBO, ConnectorType.AC_SLOW),
            lat = 37.5, lng = 127.0, statusObservedAt = Instant.parse("2026-11-12T05:00:00Z"),
        )
        store.saveStation("예시", 1.2, at, recommended = info)
        assertEquals(info, store.load().recommended)
    }

    @Test
    fun 깨진_추천_값은_null() {
        memory.map["recommended_station"] = "{not json"
        assertNull(store.load().recommended)
    }
}
