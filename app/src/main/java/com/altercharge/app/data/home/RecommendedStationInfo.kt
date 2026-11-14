/** R1 추천 충전소 카드 값 (REQ_ST_007). 추천 응답(03)에 없는 주소 · 충전기는 상세(02)로 채운다 */
package com.altercharge.app.data.home

import com.altercharge.app.data.navi.RouteDestination
import com.altercharge.app.data.remote.model.ChargerDetail
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.Recommendation
import com.altercharge.app.data.remote.model.RecommendationReason
import com.altercharge.app.data.remote.model.SpeedCategory
import com.altercharge.app.data.remote.model.StationDetail

/**
 * R1 추천 충전소 카드에 필요한 값 (REQ_ST_007).
 * 추천 응답(03)에 없는 주소 · 충전기별 연결 타입 · 출력은 상세(02)로 채운다. 상세를 못 받으면 null · 빈 목록이다.
 */
data class RecommendedStationInfo(
    val stationId: String,
    val name: String,
    val distanceMeters: Int,
    val availableCount: Int,
    val visitCount: Int?,
    val speedCategory: SpeedCategory?,
    val maxOutputKw: Double?,
    val fallbackUsed: Boolean,
    val reasonCode: RecommendationReason,
    val address: String? = null,
    val totalValidCount: Int? = null,
    val connectorTypes: List<ConnectorType> = emptyList(),
    val chargers: List<ChargerDetail> = emptyList(),
    /** 길 안내 목적지 (REQ_BR_005). 추천 응답에는 없고 상세를 받아야 채워진다 */
    val lat: Double? = null,
    val lng: Double? = null,
) {
    /** 충전기별 연결 타입 합집합. 상세의 충전기 목록이 비면 추천 응답에 없으므로 빈 목록 */
    val allConnectorTypes: List<ConnectorType>
        get() = if (connectorTypes.isNotEmpty()) connectorTypes else chargers.flatMap { it.connectorTypes }.distinct()
}

/** 추천 응답 → R1 카드 값. 주소 · 충전기 정보는 상세 응답이 있을 때만 채워진다 */
fun Recommendation.toRecommendedStationInfo(detail: StationDetail? = null): RecommendedStationInfo? {
    val station = station ?: return null
    return RecommendedStationInfo(
        stationId = station.stationId,
        name = station.name,
        distanceMeters = station.distanceMeters,
        availableCount = station.availableCount,
        visitCount = station.visitCount,
        speedCategory = station.speedCategory,
        maxOutputKw = station.maxOutputKw,
        fallbackUsed = fallbackUsed,
        reasonCode = reasonCode,
        address = detail?.address,
        lat = detail?.lat,
        lng = detail?.lng,
        totalValidCount = detail?.totalValidCount,
        connectorTypes = detail?.chargers?.flatMap { it.connectorTypes }?.distinct().orEmpty(),
        chargers = detail?.chargers.orEmpty(),
    )
}

/** 길 안내 목적지. 좌표는 상세를 받아야 있어서, 없으면 안내할 수 없다 (null) */
fun RecommendedStationInfo.toRouteDestination(): RouteDestination? {
    val lat = lat ?: return null
    val lng = lng ?: return null
    return RouteDestination(stationId = stationId, name = name, lat = lat, lng = lng, address = address)
}
