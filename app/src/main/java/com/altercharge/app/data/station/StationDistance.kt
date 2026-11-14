/**
 * 충전소까지 직선거리와 방문 판정 (REQ_SL_001 · REQ_ST_008).
 * 목록의 distanceMeters는 받은 시점 위치로 서버가 계산한 값이다. 1차는 위치가 고정이라 저장한 목록도 그 값을 그대로 쓰고,
 * 실제 위치를 쓰는 2차에서 저장 목록 거리를 여기서 다시 계산한다 (REQ_CM_006).
 */
package com.altercharge.app.data.station

import com.altercharge.app.data.remote.model.StationSummary
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** 충전 시작 위치가 충전소 반경 100m 안이면 그 충전소를 방문한 것으로 센다 (REQ_ST_008) */
const val VISIT_RADIUS_METERS: Double = 100.0

private const val EARTH_RADIUS_METERS = 6_371_000.0

/** 두 좌표 사이 직선거리(m). Haversine */
fun distanceMeters(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Double {
    val dLat = Math.toRadians(toLat - fromLat)
    val dLng = Math.toRadians(toLng - fromLng)
    val a = sin(dLat / 2).let { it * it } +
        cos(Math.toRadians(fromLat)) * cos(Math.toRadians(toLat)) * sin(dLng / 2).let { it * it }
    return 2 * EARTH_RADIUS_METERS * asin(min(1.0, sqrt(a)))
}

/** 반경 안에서 가장 가까운 충전소. 같은 거리면 ID 순 (서버 이벤트 매칭과 같은 기준) */
fun nearestStationWithin(
    stations: List<StationSummary>,
    lat: Double,
    lng: Double,
    radiusMeters: Double = VISIT_RADIUS_METERS,
): StationSummary? = stations
    .map { it to distanceMeters(lat, lng, it.lat, it.lng) }
    .filter { (_, distance) -> distance <= radiusMeters }
    .minWithOrNull(compareBy({ it.second }, { it.first.stationId }))
    ?.first
