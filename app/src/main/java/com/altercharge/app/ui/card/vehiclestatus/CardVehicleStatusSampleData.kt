package com.altercharge.app.ui.card.vehiclestatus

/**
 * C 카드 샘플 값. 상황 01~12 하나씩. 미리보기 · 디버그 카탈로그 · 화면 개발 중 임시 데이터로 쓴다.
 * 실데이터가 아니다 (이름은 MVP 고정 이름 "성진", Figma 시안과 같음, REQ_JD_003).
 */
internal object CardVehicleStatusSampleData {
    private const val NAME = "성진"

    /** 01 주차 · 충전 권장 */
    val ChargeRecommended = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 22,
        rangeKm = 95,
        nearestStationKm = 1.2,
    )

    /** 02 주차 · 출발 준비 완료 */
    val Ready = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 82,
        rangeKm = 340,
        lastChargedAt = "07:30",
    )

    /** 03 충전 중 */
    val Charging = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.CHARGING,
        socPercent = 62,
        rangeKm = 250,
        remainingMinutes = 63,
        fullAt = "21:40",
    )

    /** 04 충전 중 · 배터리 부족 */
    val ChargingLow = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.CHARGING,
        socPercent = 24,
        rangeKm = 98,
        remainingMinutes = 110,
        fullAt = "23:10",
    )

    /** 05 충전 중 · 배터리 위험 */
    val ChargingCritical = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.CHARGING,
        socPercent = 8,
        rangeKm = 30,
        remainingMinutes = 160,
        fullAt = "00:20",
    )

    /** 06 충전 완료 */
    val Charged = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.COMPLETED,
        socPercent = 100,
        rangeKm = 410,
        fullAt = "21:12",
    )

    /** 07 충전 일시정지 */
    val Paused = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.PAUSED,
        socPercent = 54,
        rangeKm = 220,
        pauseReason = PauseReason.GRID_DEMAND,
        autoResumeAt = "22:30",
    )

    /** 08 예약 충전 */
    val Scheduled = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.SCHEDULED,
        socPercent = 45,
        rangeKm = 180,
        scheduledAt = "23:00",
        fullAt = "06:30",
    )

    /** 09 배터리 위험 · 미연결 */
    val CriticalUnplugged = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 14,
        rangeKm = 52,
    )

    /** 10 배터리 방전 */
    val Depleted = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 0,
        rangeKm = 0,
    )

    /** 11 충전 오류 */
    val ChargeError = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.FAULT,
        socPercent = null,
        rangeKm = null,
        fault = ChargeFault.COMMUNICATION,
    )

    /** 12 차량 정보 없음 */
    val NoData = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = null,
        rangeKm = null,
        lastUpdatedAt = "14:05",
        retryInSeconds = 30,
    )

    /** 01 충전 권장 · 배터리는 정상이지만 버틸 일수가 1일 이하라 판정이 충전 권장 (REQ_ST_005) */
    val ChargeRecommendedByDays = CardVehicleStatusUiState(
        driverName = NAME,
        charging = ChargingState.UNPLUGGED,
        socPercent = 45,
        rangeKm = 160,
        nearestStationKm = 1.2,
        chargeRecommended = true,
    )

    /** 11 충전 오류 · 커넥터 체결 불량 */
    val ChargeErrorConnector = ChargeError.copy(fault = ChargeFault.CONNECTOR)

    /** 11 충전 오류 · 충전기 고장 */
    val ChargeErrorCharger = ChargeError.copy(fault = ChargeFault.CHARGER)

    /** 01~12 대표 샘플 밖의 변형 (판정 연결 · 오류 종류) */
    val variants: List<CardVehicleStatusUiState> = listOf(ChargeRecommendedByDays, ChargeErrorConnector, ChargeErrorCharger)

    /** 01~12 순서 */
    val all: List<CardVehicleStatusUiState> = listOf(
        ChargeRecommended, Ready, Charging, ChargingLow, ChargingCritical, Charged,
        Paused, Scheduled, CriticalUnplugged, Depleted, ChargeError, NoData,
    )
}
