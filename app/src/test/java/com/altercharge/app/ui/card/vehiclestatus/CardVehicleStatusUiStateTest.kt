package com.altercharge.app.ui.card.vehiclestatus

import org.junit.Assert.assertEquals
import org.junit.Test

class CardVehicleStatusUiStateTest {

    private fun state(charging: ChargingState, soc: Int?) = CardVehicleStatusUiState(
        driverName = "운전자",
        charging = charging,
        socPercent = soc,
        rangeKm = null,
    )

    private data class Case(
        val charging: ChargingState,
        val soc: Int?,
        val situation: VehicleSituation,
        val chip: ConnectionChip,
    )

    /** 상황 01~12 각각의 대표 조합 */
    private val cases = listOf(
        Case(ChargingState.UNPLUGGED, 22, VehicleSituation.CHARGE_RECOMMENDED, ConnectionChip.UNPLUGGED),
        Case(ChargingState.UNPLUGGED, 80, VehicleSituation.READY, ConnectionChip.UNPLUGGED),
        Case(ChargingState.CHARGING, 62, VehicleSituation.CHARGING, ConnectionChip.CONNECTED),
        Case(ChargingState.CHARGING, 24, VehicleSituation.CHARGING_LOW, ConnectionChip.CONNECTED),
        Case(ChargingState.CHARGING, 8, VehicleSituation.CHARGING_CRITICAL, ConnectionChip.CONNECTED),
        Case(ChargingState.COMPLETED, 100, VehicleSituation.CHARGED, ConnectionChip.CONNECTED),
        Case(ChargingState.PAUSED, 54, VehicleSituation.PAUSED, ConnectionChip.CONNECTED),
        Case(ChargingState.SCHEDULED, 45, VehicleSituation.SCHEDULED, ConnectionChip.STANDBY),
        Case(ChargingState.UNPLUGGED, 14, VehicleSituation.CRITICAL_UNPLUGGED, ConnectionChip.UNPLUGGED),
        Case(ChargingState.UNPLUGGED, 0, VehicleSituation.DEPLETED, ConnectionChip.UNPLUGGED),
        Case(ChargingState.FAULT, null, VehicleSituation.CHARGE_ERROR, ConnectionChip.ERROR),
        Case(ChargingState.UNPLUGGED, null, VehicleSituation.NO_DATA, ConnectionChip.OFFLINE),
    )

    @Test
    fun 상황_12개와_칩() {
        assertEquals(VehicleSituation.entries.toSet(), cases.map { it.situation }.toSet())
        cases.forEach { case ->
            val s = state(case.charging, case.soc)
            val label = "${case.charging} × ${case.soc}"
            assertEquals(label, case.situation, s.situation)
            assertEquals(label, case.chip, s.chip)
        }
    }

    @Test
    fun 시안_없는_조합() {
        // 충전 중 0% → 위험 구간, 충전 중 SOC 모름 → 정보 없음
        assertEquals(VehicleSituation.CHARGING_CRITICAL, state(ChargingState.CHARGING, 0).situation)
        assertEquals(VehicleSituation.NO_DATA, state(ChargingState.CHARGING, null).situation)
        // 완료 · 일시정지 · 예약 · 오류는 배터리 구간과 상관없다
        assertEquals(VehicleSituation.CHARGED, state(ChargingState.COMPLETED, null).situation)
        assertEquals(VehicleSituation.PAUSED, state(ChargingState.PAUSED, 5).situation)
        assertEquals(VehicleSituation.SCHEDULED, state(ChargingState.SCHEDULED, 0).situation)
        assertEquals(VehicleSituation.CHARGE_ERROR, state(ChargingState.FAULT, 80).situation)
    }

    @Test
    fun 샘플_데이터는_01부터_12_순서() {
        assertEquals(
            VehicleSituation.entries.toList(),
            CardVehicleStatusSampleData.all.map { it.situation },
        )
    }
}
