package com.altercharge.app.ui.card.info

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** "N분 전 기준" 단계 나누기. Figma 798:56855 표 (1분 미만 / 1–59분 / 1–23시간 / 24시간 이상) */
class CardInfoLastCheckedTest {

    private val seoul = ZoneId.of("Asia/Seoul")
    private val now = Instant.parse("2026-11-12T05:30:00Z") // 서울 11.12 14:30

    private fun ago(duration: Duration) = lastCheckedOf(now.minus(duration), now, seoul)

    @Test
    fun 일분_미만은_방금() {
        assertEquals(LastChecked.JustNow, ago(Duration.ofSeconds(59)))
    }

    @Test
    fun 받은_시각이_미래여도_방금() {
        assertEquals(LastChecked.JustNow, lastCheckedOf(now.plusSeconds(120), now, seoul))
    }

    @Test
    fun 일분부터_분() {
        assertEquals(LastChecked.MinutesAgo(1), ago(Duration.ofMinutes(1)))
        assertEquals(LastChecked.MinutesAgo(59), ago(Duration.ofMinutes(59).plusSeconds(59)))
    }

    @Test
    fun 한시간부터_시간() {
        assertEquals(LastChecked.HoursAgo(1), ago(Duration.ofMinutes(60)))
        assertEquals(LastChecked.HoursAgo(23), ago(Duration.ofHours(23).plusMinutes(59)))
    }

    @Test
    fun 하루_이상은_받은_날짜() {
        assertEquals(LastChecked.OnDate(month = 11, day = 11), ago(Duration.ofHours(24)))
    }

    @Test
    fun 날짜는_지정한_시간대_기준() {
        // UTC 11.10 16:00 = 서울 11.11 01:00
        val checkedAt = Instant.parse("2026-11-10T16:00:00Z")
        assertEquals(LastChecked.OnDate(month = 11, day = 11), lastCheckedOf(checkedAt, now, seoul))
    }
}
