package com.altercharge.app.ui.card.stationitem

import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.ui.card.station.ChargingType

/**
 * R2 목록 한 줄 (REQ_SL_001 · REQ_SL_004).
 * 거리 · 가용 대수 · 연결 타입 · 방문 횟수는 받은 값이고, 방문순이면 거리 대신 방문 횟수를 보여 준다.
 */
data class CardItemUiState(
    // 충전소 ID (서버 statId). 선택 · 방문 기록에 쓴다
    val stationId: String = "",
    val stationName: String,
    // 충전 대기 중인 충전기 수 (stat=2)
    val availableChargerCount: Int,
    // 삭제 · 무효를 뺀 충전기 수
    val totalChargerCount: Int,
    var chargingType: ChargingType,
    // km, 직선거리
    val distance: Double,
    val chargeStationAddress: String,
    // 충전소가 가진 연결 타입 (REQ_SL_001 항목)
    val connectorTypes: List<ConnectorType> = emptyList(),
    // 화면에 쓰는 방문 횟수. 방문 기록이 없으면 0 (REQ_SL_004)
    val visitCount: Int = 0,
    // 방문순이면 거리 대신 방문 횟수를 보여 준다
    val showVisitCount: Boolean = false,
    // 15분이 지나 가용 대수를 확인할 수 없음 (REQ_CM_009)
    val availabilityUnknown: Boolean = false,
)
