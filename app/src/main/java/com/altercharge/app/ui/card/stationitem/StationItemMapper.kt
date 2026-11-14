/** 목록 응답 → R2 한 줄 (REQ_SL_001 · REQ_SL_004) */
package com.altercharge.app.ui.card.stationitem

import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.SpeedCategory
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.data.station.displayKm
import com.altercharge.app.data.station.speedCategoryOf
import com.altercharge.app.ui.card.station.ChargingType

/**
 * @param visitCount 화면에 쓸 방문 횟수 ([com.altercharge.app.data.station.visitCountOf])
 * @param showVisitCount 방문순이면 true. 거리 자리에 방문 횟수를 보여 준다
 * @param availabilityUnknown 가용 대수를 확인할 수 없으면 true (REQ_CM_009)
 * @param preferredConnectors 연결 타입 필터에서 고른 타입. 목록 한 줄에서 대표 타입으로 앞에 둔다 (Figma V2-5)
 */
fun StationSummary.toCardItemUiState(
    visitCount: Int = 0,
    showVisitCount: Boolean = false,
    availabilityUnknown: Boolean = false,
    preferredConnectors: Set<ConnectorType> = emptySet(),
): CardItemUiState = CardItemUiState(
    stationId = stationId,
    stationName = name,
    availableChargerCount = availableCount,
    totalChargerCount = totalValidCount,
    chargingType = speedCategoryOf(maxOutputKw).toChargingType(),
    distance = displayKm(distanceMeters),
    chargeStationAddress = address,
    connectorTypes = chargerTypes.sortedByDescending { it in preferredConnectors },
    visitCount = visitCount,
    showVisitCount = showVisitCount,
    availabilityUnknown = availabilityUnknown,
)

/** 시안 아이콘은 완속 · 급속 · 초급속 세 가지다. 출력을 모르면 완속 아이콘으로 자리만 채운다 */
internal fun SpeedCategory?.toChargingType(): ChargingType = when (this) {
    SpeedCategory.SLOW -> ChargingType.AC
    SpeedCategory.FAST -> ChargingType.DC
    SpeedCategory.ULTRA_FAST -> ChargingType.ULTRA_FAST
    null -> ChargingType.AC
}
