package com.altercharge.app.ui.card.vehiclestatus

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryLevelTest {

    @Test
    fun fromSoc_경계값() {
        val cases = listOf(
            null to BatteryLevel.UNKNOWN,
            0 to BatteryLevel.EMPTY,
            // REQ_VS_001: 0 방전 / 1~20 위험 / 21~30 부족 / 31~100 정상
            1 to BatteryLevel.CRITICAL,
            20 to BatteryLevel.CRITICAL,
            21 to BatteryLevel.LOW,
            30 to BatteryLevel.LOW,
            31 to BatteryLevel.NORMAL,
            100 to BatteryLevel.NORMAL,
        )
        cases.forEach { (soc, expected) ->
            assertEquals("soc=$soc", expected, BatteryLevel.fromSoc(soc))
        }
    }

    @Test
    fun fromSoc_범위_밖() {
        assertEquals(BatteryLevel.EMPTY, BatteryLevel.fromSoc(-5))
        assertEquals(BatteryLevel.NORMAL, BatteryLevel.fromSoc(120))
    }
}
