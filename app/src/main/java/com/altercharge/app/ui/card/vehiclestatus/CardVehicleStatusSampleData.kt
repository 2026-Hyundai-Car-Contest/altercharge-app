package com.altercharge.app.ui.card.vehiclestatus

/** 상황 01~12 샘플. 실데이터가 아니다 (이름은 MVP 고정 이름, REQ_JD_003) */
internal object CardVehicleStatusSampleData {
    private const val NAME = "성진"

    val ChargeRecommended = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 22,
        rangeKm = 95,
        nearestStationKm = 1.2,
    )

    val Ready = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 82,
        rangeKm = 340,
        lastChargedAt = "07:30",
    )

    val Charging = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.CHARGING,
        socPercent = 62,
        rangeKm = 250,
        remainingMinutes = 63,
        fullAt = "21:40",
    )

    val ChargingLow = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.CHARGING,
        socPercent = 24,
        rangeKm = 98,
        remainingMinutes = 110,
        fullAt = "23:10",
    )

    val ChargingCritical = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.CHARGING,
        socPercent = 8,
        rangeKm = 30,
        remainingMinutes = 160,
        fullAt = "00:20",
    )

    val Charged = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.COMPLETED,
        socPercent = 100,
        rangeKm = 410,
        fullAt = "21:12",
    )

    val Paused = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.PAUSED,
        socPercent = 54,
        rangeKm = 220,
        pauseReason = PauseReason.GRID_DEMAND,
        autoResumeAt = "22:30",
    )

    val Scheduled = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.SCHEDULED,
        socPercent = 45,
        rangeKm = 180,
        scheduledAt = "23:00",
        fullAt = "06:30",
    )

    val CriticalUnplugged = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 14,
        rangeKm = 52,
    )

    val Depleted = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 0,
        rangeKm = 0,
    )

    val ChargeError = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.FAULT,
        socPercent = null,
        rangeKm = null,
        fault = ChargeFault.COMMUNICATION,
    )

    val NoData = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = null,
        rangeKm = null,
        lastUpdatedAt = "14:05",
        retryInSeconds = 30,
    )

    /** 배터리는 정상이지만 버틸 일수가 1일 이하라 충전 권장 (REQ_ST_005) */
    val ChargeRecommendedByDays = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 45,
        rangeKm = 160,
        nearestStationKm = 1.2,
        chargeRecommended = true,
    )

    val ChargeErrorConnector = ChargeError.copy(fault = ChargeFault.CONNECTOR)

    val ChargeErrorCharger = ChargeError.copy(fault = ChargeFault.CHARGER)

    val variants: List<CardVehicleStatusUiState> = listOf(ChargeRecommendedByDays, ChargeErrorConnector, ChargeErrorCharger)

    val all: List<CardVehicleStatusUiState> = listOf(
        ChargeRecommended, Ready, Charging, ChargingLow, ChargingCritical, Charged,
        Paused, Scheduled, CriticalUnplugged, Depleted, ChargeError, NoData,
    )
}
