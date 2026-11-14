package com.altercharge.app.data.vehicle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MockFullAtTest {

    private fun short(time: LocalTime) = time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)).replace(' ', ' ')

    @Test
    fun 충전_완료_시각은_지금에_남은_시간을_더한다() {
        // 14:05에 45분 남았으면 14:50 (상단 바 시각 · "45분 남았어요"와 맞아야 한다)
        assertEquals(short(LocalTime.of(14, 50)), MockVehicleTelemetrySource.fullAtFrom(LocalTime.of(14, 5), 45))
    }

    @Test
    fun 자정을_넘겨도_시각만_넘어간다() {
        assertEquals(short(LocalTime.of(0, 20)), MockVehicleTelemetrySource.fullAtFrom(LocalTime.of(23, 35), 45))
    }

    @Test
    fun 남은_시간을_모르면_시각도_없다() {
        assertNull(MockVehicleTelemetrySource.fullAtFrom(LocalTime.of(14, 5), null))
    }
}
