package com.altercharge.app.ui.card.station

data class CardStationUiState(
    // ChipTone과 텍스트를 저장하는 객체
    val chipState: ChipState,
    // 충전소 잔여 자리
    val availableChargerCount: Int,
    // 충전소 사용중인 자리
    val occupiedChargerCount: Int,
    // 충전소 상태
    val chargerStatus: ChargerStatus,
    // 충전소명
    val stationName: String,
    // 충전소 정보(급속, Kw)
    val chargerInfo: List<ChargeInfoTag>,
    // 충전소까지 남은 거리
    val distance: Double,
    // 내 차 충전 횟수
    val myChargingCount: Int
) {
}

data class ChipState(
    val text: String,
    val chipTone: ChipTone
) {}

