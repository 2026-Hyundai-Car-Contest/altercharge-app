package com.altercharge.app.ui.card.stationitem

import com.altercharge.app.ui.card.station.ChargingType

data class CardItemUiState(
    // 충전소명
    val stationName: String,
    // 충전소 잔여 자리
    val availableChargerCount: Int,
    // 충전소 사용중인 자리
    val occupiedChargerCount: Int,
    // 충전타입(완속/급속/초급속)
    var chargingType: ChargingType,
    // 충전소까지 거리
    val distance: Double,
    // 충전소 도로명 주소
    val chargeStationAddress: String
) {}
