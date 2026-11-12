package com.altercharge.app.data.remote.freshness

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

/** REQ_CM_009: 가용 대수 · 운영 상태 15분, 나머지 충전소 정보 7일 */
class DataValidityTest {

    private val now: Instant = Instant.parse("2026-11-12T05:30:00Z")
    private fun ago(d: Duration): Instant = now.minus(d)

    @Test
    fun 받은_정보가_없으면_NONE() {
        assertEquals(Validity.NONE, validityOf(null, now, AVAILABILITY_VALIDITY))
    }

    @Test
    fun 십오분까지는_가용_대수_유효_그_뒤는_만료() {
        assertEquals(Validity.VALID, validityOf(ago(Duration.ofMinutes(15)), now, AVAILABILITY_VALIDITY))
        assertEquals(Validity.EXPIRED, validityOf(ago(Duration.ofMinutes(15).plusSeconds(1)), now, AVAILABILITY_VALIDITY))
    }

    @Test
    fun 칠일까지는_충전소_정보_유효_그_뒤는_만료() {
        assertEquals(Validity.VALID, validityOf(ago(Duration.ofDays(7)), now, STATION_INFO_VALIDITY))
        assertEquals(Validity.EXPIRED, validityOf(ago(Duration.ofDays(7).plusMinutes(1)), now, STATION_INFO_VALIDITY))
    }

    @Test
    fun 받은_시각이_미래여도_유효() {
        assertEquals(Validity.VALID, validityOf(now.plusSeconds(600), now, AVAILABILITY_VALIDITY))
    }

    @Test
    fun 십오분_넘고_칠일_안이면_가용_대수만_정보_없음() {
        val v = stationValidity(ago(Duration.ofMinutes(20)), now)
        assertEquals(Validity.EXPIRED, v.availability)
        assertEquals(Validity.VALID, v.info)
        assertTrue(v.showAvailabilityUnknown)
        assertFalse(v.isEmpty)
    }

    @Test
    fun 십오분_안이면_모두_그대로() {
        val v = stationValidity(ago(Duration.ofMinutes(10)), now)
        assertFalse(v.showAvailabilityUnknown)
        assertFalse(v.isEmpty)
    }

    @Test
    fun 칠일이_지나거나_정보가_없으면_빈_상태() {
        assertTrue(stationValidity(ago(Duration.ofDays(8)), now).isEmpty)
        assertTrue(stationValidity(null, now).isEmpty)
    }

    // ---- 다시 요청 ----

    @Test
    fun 한_번도_안_했으면_바로_요청() {
        assertTrue(RefreshRule.isDue(null, now))
    }

    @Test
    fun 삼십초마다_자동_재요청() {
        assertFalse(RefreshRule.isDue(ago(Duration.ofSeconds(29)), now))
        assertTrue(RefreshRule.isDue(ago(Duration.ofSeconds(30)), now))
    }

    @Test
    fun 연결이_돌아오면_즉시_요청() {
        assertTrue(RefreshRule.isDue(ago(Duration.ofSeconds(1)), now, reconnected = true))
    }
}
