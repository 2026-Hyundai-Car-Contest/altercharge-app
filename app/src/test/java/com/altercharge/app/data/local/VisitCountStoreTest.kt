package com.altercharge.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** 충전소 방문 횟수 (REQ_ST_008): 같은 충전소는 하루 1회, 화면은 바로 +1 */
class VisitCountStoreTest {

    private class MemoryStore : KeyValueStore {
        val map = mutableMapOf<String, String>()
        override fun getString(key: String): String? = map[key]
        override fun putStrings(values: Map<String, String?>) {
            values.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }
    }

    private val store = MemoryStore()
    private val visits = VisitCountStore(store)
    private val day = LocalDate.of(2026, 11, 14)

    @Test
    fun 처음_방문하면_1회() {
        assertEquals(1, visits.recordIfFirstToday("ST-1", day))
        assertEquals(1, visits.count("ST-1"))
    }

    @Test
    fun 같은_날_다시_방문해도_늘지_않는다() {
        visits.recordIfFirstToday("ST-1", day)
        assertNull(visits.recordIfFirstToday("ST-1", day))
        assertEquals(1, visits.count("ST-1"))
    }

    @Test
    fun 다음_날_방문하면_또_센다() {
        visits.recordIfFirstToday("ST-1", day)
        assertEquals(2, visits.recordIfFirstToday("ST-1", day.plusDays(1)))
    }

    @Test
    fun 충전소끼리_따로_센다() {
        visits.recordIfFirstToday("ST-1", day)
        visits.recordIfFirstToday("ST-2", day)
        assertEquals(1, visits.count("ST-1"))
        assertEquals(1, visits.count("ST-2"))
    }

    @Test
    fun 다시_만들어도_저장한_횟수를_읽는다() {
        visits.recordIfFirstToday("ST-1", day)
        visits.recordIfFirstToday("ST-1", day.plusDays(1))
        val again = VisitCountStore(store)
        assertEquals(2, again.count("ST-1"))
        assertEquals(mapOf("ST-1" to 2), again.counts())
    }

    @Test
    fun 저장값이_깨져도_읽을_수_있는_줄만_쓴다() {
        store.putStrings(mapOf("station_visits" to "ST-1\u001F3\u001F2026-11-13\nbroken\nST-2\u001Fx\u001F"))
        val loaded = VisitCountStore(store).counts()
        assertEquals(mapOf("ST-1" to 3), loaded)
    }

    @Test
    fun 저장_형식은_왕복한다() {
        val entries = mapOf(
            "ST-1" to VisitEntry(total = 2, lastDate = day),
            "ST-2" to VisitEntry(total = 1, lastDate = null),
        )
        assertEquals(entries, decodeVisits(encodeVisits(entries)))
        assertTrue(decodeVisits(null).isEmpty())
    }
}
