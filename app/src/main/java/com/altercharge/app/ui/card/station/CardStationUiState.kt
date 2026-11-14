package com.altercharge.app.ui.card.station

import com.altercharge.app.data.remote.model.ConnectorType

/**
 * R1 추천 충전소 카드 (REQ_ST_007).
 * 이름 · 충전 속도와 출력 · 가용 대수 · 거리 · 방문 횟수를 보여 주고, 상세로 주소와 충전기 연결 타입을 함께 보여 준다.
 */
data class CardStationUiState(
    // ChipTone과 텍스트를 저장하는 객체 ("추천" / "대체 추천")
    val chipState: ChipState,
    // 충전 대기 중인 충전기 수
    val availableChargerCount: Int,
    // 삭제 · 무효를 뺀 충전기 수
    val totalChargerCount: Int,
    // 충전소 상태
    val chargerStatus: ChargerStatus,
    // 충전소명
    val stationName: String,
    // 충전소 정보(급속, Kw)
    val chargerInfo: List<ChargeInfoTag>,
    // 충전소까지 남은 거리 (km)
    val distance: Double,
    // 내 차 충전 횟수 (REQ_ST_008)
    val myChargingCount: Int,
    // 충전소 주소. 상세를 못 받았으면 null
    val stationAddress: String? = null,
    // 충전기 연결 타입 요약 (REQ_ST_007)
    val connectorTypes: List<ConnectorType> = emptyList(),
) {}

data class ChipState(
    val text: String,
    val chipTone: ChipTone
) {}
