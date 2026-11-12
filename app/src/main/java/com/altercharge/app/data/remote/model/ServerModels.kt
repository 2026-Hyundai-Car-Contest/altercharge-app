/**
 * 서버 OpenAPI(/v3/api-docs) 스키마를 옮긴 값. 거리는 km · m를 이름에 적는다.
 * 충전소 이름 · 주소는 환경공단 원본 한국어라 번역하지 않는다.
 */
package com.altercharge.app.data.remote.model

import java.time.Instant

// ---- 06 · 07 차량 스냅샷 ----

/** 서버는 최근 56일 ~ 현재+5분 측정값만 받는다 */
data class VehicleSnapshotRequest(
    val vehicleId: String,
    val recordedAt: Instant,
    val socPercent: Int,
    val odometerKm: Double,
    val drivableRangeKm: Int? = null,
)

data class VehicleSnapshotSaved(
    val snapshotId: String,
    val saved: Boolean,
    val recordedAt: Instant,
)

data class VehicleSnapshotRecord(
    val snapshotId: String,
    val recordedAt: Instant,
    val socPercent: Int,
    val odometerKm: Double,
    val drivableRangeKm: Int?,
)

/** completeDayCount는 서버 참고값이고 앱 판정은 쓰지 않는다 */
data class VehicleSnapshotHistory(
    val vehicleId: String,
    val records: List<VehicleSnapshotRecord>,
    val retentionDays: Int,
    val completeDayCount: Int,
)

// ---- 05 · 08 충전 이벤트 ----

enum class ChargingEventType { START, COMPLETE }

data class ChargingEventRequest(
    /** 멱등 키: 같은 키로 다시 보내도 안전하다 */
    val eventId: String,
    val vehicleId: String,
    val eventType: ChargingEventType,
    val occurredAt: Instant,
    val lat: Double,
    val lng: Double,
    val socPercent: Int,
    val odometerKm: Double? = null,
)

data class ChargingEventAccepted(
    val eventId: String,
    val accepted: Boolean,
    /** null이어도 방문하지 않았다는 뜻은 아니다 */
    val matchedStationId: String?,
    val visitCountAdded: Int,
    val dailyDuplicate: Boolean,
)

/** 완료 이벤트가 없으면 시각 · 거리가 null */
data class LatestChargeHistory(
    val vehicleId: String,
    val lastChargeCompletedAt: Instant?,
    val odometerAtLastChargeKm: Double?,
    val currentOdometerKm: Double?,
    val distanceSinceChargeKm: Double?,
    val trusted: Boolean,
)

// ---- 01 ~ 04 충전소 ----

enum class ConnectorType { DC_CHADEMO, AC_SLOW, AC_3PHASE, DC_COMBO, DC_COMBO_SLOW, NACS, DC_COMBO_2, UNKNOWN }

enum class ChargerStatus { UNKNOWN, COMMUNICATION_ERROR, AVAILABLE, CHARGING, STOPPED, MAINTENANCE, RESERVED, UNCONFIRMED }

data class StationSummary(
    val stationId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val distanceMeters: Int,
    val availableCount: Int,
    val totalValidCount: Int,
    val chargerTypes: List<ConnectorType>,
    val maxOutputKw: Double?,
    val visitCount: Int?,
    val statusObservedAt: Instant?,
)

data class StationPage(
    val items: List<StationSummary>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean,
    val fetchedAt: Instant,
)

data class ChargerDetail(
    val chargerId: String,
    val connectorType: String,
    val connectorTypes: List<ConnectorType>,
    val chgerType: String,
    val outputKw: Double?,
    val status: ChargerStatus,
    val stat: String,
    val statusUpdatedAt: Instant?,
    val statusObservedAt: Instant?,
)

data class StationDetail(
    val stationId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val availableCount: Int,
    val totalValidCount: Int,
    val chargers: List<ChargerDetail>,
    val statusObservedAt: Instant?,
)

/** OK가 아니면 길 안내를 막는다 (EX_004) */
enum class NavigationReason { OK, OUT_OF_SERVICE, MAINTENANCE, COMM_ERROR, NO_AVAILABLE_CHARGER, STATUS_UNKNOWN, STATUS_STALE }

data class NavigationCheck(
    val stationId: String,
    val navigationAllowed: Boolean,
    val reasonCode: NavigationReason,
    val latestStatuses: List<ChargerStatus>,
    val checkedAt: Instant,
)

/** 50kW 미만 SLOW, 50~200 FAST, 200 이상 ULTRA_FAST */
enum class SpeedCategory { SLOW, FAST, ULTRA_FAST }

data class RecommendedStation(
    val stationId: String,
    val name: String,
    val distanceMeters: Int,
    val availableCount: Int,
    val visitCount: Int?,
    val speedCategory: SpeedCategory?,
    val maxOutputKw: Double?,
)

/** UNKNOWN은 서버가 새로 추가한 값. 화면은 station 유무로 판단한다 */
enum class RecommendationReason { VISITS_THEN_DISTANCE, NEAREST_FALLBACK, NO_STATIONS_IN_RADIUS, UNKNOWN }

data class Recommendation(
    val station: RecommendedStation?,
    val reasonCode: RecommendationReason,
    val fallbackUsed: Boolean,
    val statusObservedAt: Instant?,
)

data class ApiError(val error: String, val message: String)
