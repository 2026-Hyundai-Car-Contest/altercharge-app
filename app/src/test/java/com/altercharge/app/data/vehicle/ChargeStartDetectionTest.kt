package com.altercharge.app.data.vehicle

import com.altercharge.app.ui.card.vehiclestatus.ChargingState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 충전 시작 판정 (REQ_ST_008): 이때만 충전소 방문을 센다 */
class ChargeStartDetectionTest {

    @Test
    fun 앱을_켰을_때_이미_충전_중이면_시작으로_본다() {
        assertTrue(isChargeStart(previous = null, current = ChargingState.CHARGING))
    }

    @Test
    fun 미연결에서_충전_중으로_바뀌면_시작() {
        assertTrue(isChargeStart(ChargingState.UNPLUGGED, ChargingState.CHARGING))
    }

    @Test
    fun 충전_중이_계속되면_시작이_아니다() {
        assertFalse(isChargeStart(ChargingState.CHARGING, ChargingState.CHARGING))
    }

    @Test
    fun 충전이_끝나는_것은_시작이_아니다() {
        assertFalse(isChargeStart(ChargingState.CHARGING, ChargingState.UNPLUGGED))
        assertFalse(isChargeStart(ChargingState.CHARGING, ChargingState.COMPLETED))
    }

    @Test
    fun 충전_중이_아니면_시작이_아니다() {
        assertFalse(isChargeStart(null, ChargingState.UNPLUGGED))
        assertFalse(isChargeStart(ChargingState.SCHEDULED, ChargingState.PAUSED))
    }
}
