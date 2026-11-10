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
    fun 미연결_01_02는_충전_권장_판정으로_고른다() {
        // 기본값: 배터리 30% 이하면 충전 권장 (REQ_ST_005)
        assertEquals(VehicleSituation.CHARGE_RECOMMENDED, state(ChargingState.UNPLUGGED, 30).situation)
        assertEquals(VehicleSituation.READY, state(ChargingState.UNPLUGGED, 31).situation)
        // 판정 결과를 넘기면 배터리 구간보다 판정이 먼저다
        val recommendedAt80 = state(ChargingState.UNPLUGGED, 80).copy(chargeRecommended = true)
        assertEquals(VehicleSituation.CHARGE_RECOMMENDED, recommendedAt80.situation)
        assertEquals(ConnectionChip.UNPLUGGED, recommendedAt80.chip)
        assertEquals(
            VehicleSituation.READY,
            state(ChargingState.UNPLUGGED, 25).copy(chargeRecommended = false).situation,
        )
    }

    @Test
    fun 미연결_위험_방전_정보없음은_배터리_구간이_먼저() {
        listOf(true, false).forEach { recommended ->
            fun make(soc: Int?) = state(ChargingState.UNPLUGGED, soc).copy(chargeRecommended = recommended)
            assertEquals(VehicleSituation.CRITICAL_UNPLUGGED, make(20).situation)
            assertEquals(VehicleSituation.CRITICAL_UNPLUGGED, make(1).situation)
            assertEquals(VehicleSituation.DEPLETED, make(0).situation)
            assertEquals(VehicleSituation.NO_DATA, make(null).situation)
        }
    }

    @Test
    fun 충전_중_구간_경계() {
        assertEquals(VehicleSituation.CHARGING, state(ChargingState.CHARGING, 31).situation)
        assertEquals(VehicleSituation.CHARGING_LOW, state(ChargingState.CHARGING, 30).situation)
        assertEquals(VehicleSituation.CHARGING_LOW, state(ChargingState.CHARGING, 21).situation)
        assertEquals(VehicleSituation.CHARGING_CRITICAL, state(ChargingState.CHARGING, 20).situation)
    }

    @Test
    fun 오류_종류와_상관없이_11() {
        ChargeFault.entries.forEach { fault ->
            val s = state(ChargingState.FAULT, null).copy(fault = fault)
            assertEquals(fault.name, VehicleSituation.CHARGE_ERROR, s.situation)
            assertEquals(fault.name, ConnectionChip.ERROR, s.chip)
        }
    }

    @Test
    fun 변형_샘플() {
        assertEquals(
            listOf(VehicleSituation.CHARGE_RECOMMENDED, VehicleSituation.CHARGE_ERROR, VehicleSituation.CHARGE_ERROR),
            CardVehicleStatusSampleData.variants.map { it.situation },
        )
    }

    @Test
    fun 샘플_데이터는_01부터_12_순서() {
        assertEquals(
            VehicleSituation.entries.toList(),
            CardVehicleStatusSampleData.all.map { it.situation },
        )
    }
}
