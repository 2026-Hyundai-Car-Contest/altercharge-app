package com.altercharge.app.ui.card.vehiclestatus

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryLevelTest {

    @Test
    fun fromSoc_경계값() {
        val cases = listOf(
            null to BatteryLevel.UNKNOWN,
            0 to BatteryLevel.EMPTY,
            1 to BatteryLevel.CRITICAL,
            14 to BatteryLevel.CRITICAL,
            15 to BatteryLevel.LOW,
            29 to BatteryLevel.LOW,
            30 to BatteryLevel.NORMAL,
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
