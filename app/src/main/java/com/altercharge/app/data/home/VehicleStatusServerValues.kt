/** C 카드 중 서버 · 판정에서 오는 값 (REQ_VS_005 · REQ_VS_006 · REQ_ST_005). 모르면 null ("--") */
package com.altercharge.app.data.home

data class VehicleStatusServerValues(
    val chargeRecommended: Boolean,
    val nearestStationKm: Double?,
    val lastChargedAt: String?,
)
