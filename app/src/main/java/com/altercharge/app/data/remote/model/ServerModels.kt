/**
 * 백엔드 API 응답 · 요청 값. 서버 OpenAPI(/v3/api-docs, 2026-11-12) 스키마를 그대로 옮겼다.
 * 이름 · 주소는 환경공단 원본 한국어라 번역하지 않는다 (백엔드 답변 9).
 * JSON ↔ 이 클래스 변환은 ServerJson.kt, 카드 값으로 바꾸는 건 mapper/ 에 있다.
 */
package com.altercharge.app.data.remote.model

import java.time.Instant

// ---- 06 · 07 차량 스냅샷 ----

/** POST /vehicles/snapshots 요청. 앱이 주기적으로 차량 값을 올려 서버가 주행 기록을 쌓는다 (CM_006 결정: 주행 이력은 서버 보관). */
data class VehicleSnapshotRequest(
    val vehicleId: String,
    /** 측정 시각. 서버는 최근 56일 ~ 현재+5분만 받는다 */
    val recordedAt: Instant,
    val socPercent: Int,
    /** 누적 주행거리 km */
    val odometerKm: Double,
    /** 주행 가능 거리 km. SDK 출처가 확정되지 않은 선택 값 */
    val drivableRangeKm: Int? = null,
)

data class VehicleSnapshotSaved(
    val snapshotId: String,
    /** 신규 저장 또는 같은 값 재전송 성공 */
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

/** GET /vehicles/snapshots. 측정 시각 오름차순, 없으면 빈 목록 */
data class VehicleSnapshotHistory(
    val vehicleId: String,
    val records: List<VehicleSnapshotRecord>,
    /** 서버 보관 기간(한국시간 날짜 수) */
    val retentionDays: Int,
    /** 서버가 세어 준 유효 기록일 수(하루 2건 이상 · 첫 기록 01:00 이하 · 마지막 23:00 이상 …). 참고값이고 앱 판정은 쓰지 않는다 */
    val completeDayCount: Int,
)

// ---- 05 · 08 충전 이벤트 ----

enum class ChargingEventType { START, COMPLETE }

/** POST /charging-events 요청. source는 지금 MOCK만 받는다 (REAL은 서버가 거부) */
data class ChargingEventRequest(
    /** 앱이 만든 멱등 키 1~100자. 같은 키로 다시 보내면 서버가 같은 결과를 돌려준다 */
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
    /** 100m 안 가장 가까운 관측 충전소. 없으면 null (비방문 증명은 아니다) */
    val matchedStationId: String?,
    val visitCountAdded: Int,
    val dailyDuplicate: Boolean,
)

/** GET /charging-events/latest. 완료 이벤트가 없으면 시각 · 거리 값이 null */
data class LatestChargeHistory(
    val vehicleId: String,
    val lastChargeCompletedAt: Instant?,
    val odometerAtLastChargeKm: Double?,
    val currentOdometerKm: Double?,
    /** 마지막 충전 완료 후 주행 km. 양쪽 거리를 알고 차가 0 이상일 때만 값이 있다 */
    val distanceSinceChargeKm: Double?,
    /** 검증된 실제 충전 완료인지. 지금 시연 경로는 항상 false */
    val trusted: Boolean,
)

// ---- 01 ~ 04 충전소 ----

/** 서버가 정규화한 연결 규격. 모르는 값은 UNKNOWN */
enum class ConnectorType { DC_CHADEMO, AC_SLOW, AC_3PHASE, DC_COMBO, DC_COMBO_SLOW, NACS, DC_COMBO_2, UNKNOWN }

/** 충전기 상태. 가용 집계(availableCount)는 AVAILABLE만 센다 */
enum class ChargerStatus { UNKNOWN, COMMUNICATION_ERROR, AVAILABLE, CHARGING, STOPPED, MAINTENANCE, RESERVED, UNCONFIRMED }

/** GET /stations 목록 항목 */
data class StationSummary(
    val stationId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    /** 요청 위치에서 직선거리 m */
    val distanceMeters: Int,
    /** 지금 쓸 수 있는 충전기 수 (주차면 빈자리 보장은 아니다) */
    val availableCount: Int,
    /** 삭제 · 무효 제외 충전기 수 */
    val totalValidCount: Int,
    /** 중복 제거 · 이름 오름차순 */
    val chargerTypes: List<ConnectorType>,
    val maxOutputKw: Double?,
    /** 검증된 실제 방문 횟수. 집계 원천이 없으면 null */
    val visitCount: Int?,
    /** 유효 충전기 중 가장 오래된 상태 관측 시각. 하나라도 미관측이면 null */
    val statusObservedAt: Instant?,
)

data class StationPage(
    val items: List<StationSummary>,
    /** 0부터 */
    val page: Int,
    val size: Int,
    val hasNext: Boolean,
    /** 서버가 목록을 만든 시각. 오프라인 "N분 전 기준"의 기준값으로 쓴다 */
    val fetchedAt: Instant,
)

data class ChargerDetail(
    val chargerId: String,
    /** 단일 규격 이름. 복합 규격은 +로 연결 */
    val connectorType: String,
    val connectorTypes: List<ConnectorType>,
    /** 환경공단 원본 chgerType 코드 */
    val chgerType: String,
    val outputKw: Double?,
    val status: ChargerStatus,
    /** 환경공단 원본 stat 코드. 2만 가용 */
    val stat: String,
    val statusUpdatedAt: Instant?,
    val statusObservedAt: Instant?,
)

/** GET /stations/{id} */
data class StationDetail(
    val stationId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val availableCount: Int,
    val totalValidCount: Int,
    /** chargerId 오름차순 */
    val chargers: List<ChargerDetail>,
    val statusObservedAt: Instant?,
)

/** 길 안내를 막는 이유. OK가 아니면 막는다 (EX_004). 우선순위 STATUS_STALE > OUT_OF_SERVICE > MAINTENANCE > COMM_ERROR > STATUS_UNKNOWN > NO_AVAILABLE_CHARGER */
enum class NavigationReason { OK, OUT_OF_SERVICE, MAINTENANCE, COMM_ERROR, NO_AVAILABLE_CHARGER, STATUS_UNKNOWN, STATUS_STALE }

/** GET /stations/{id}/navigation-check. 길 안내 버튼을 누르기 직전에 한 번 묻는다 */
data class NavigationCheck(
    val stationId: String,
    /** 유효기간 안에 실제 관측된 AVAILABLE 충전기가 하나 이상일 때만 true */
    val navigationAllowed: Boolean,
    val reasonCode: NavigationReason,
    val latestStatuses: List<ChargerStatus>,
    val checkedAt: Instant,
)

/** 출력 기반 속도 구간. 50kW 미만 SLOW, 50~200 FAST, 200 이상 ULTRA_FAST */
enum class SpeedCategory { SLOW, FAST, ULTRA_FAST }

data class RecommendedStation(
    val stationId: String,
    val name: String,
    val distanceMeters: Int,
    /** 대체 후보(fallback)는 0일 수 있다 */
    val availableCount: Int,
    val visitCount: Int?,
    val speedCategory: SpeedCategory?,
    val maxOutputKw: Double?,
)

/** 추천 근거. NO_STATIONS_IN_RADIUS면 station이 null. UNKNOWN은 서버가 새로 추가한 값 (화면은 station 유무로 판단한다) */
enum class RecommendationReason { VISITS_THEN_DISTANCE, NEAREST_FALLBACK, NO_STATIONS_IN_RADIUS, UNKNOWN }

/** GET /stations/recommendation. R1 카드와 L1 장소 줄에 쓰는 추천 충전소 1곳 */
data class Recommendation(
    val station: RecommendedStation?,
    val reasonCode: RecommendationReason,
    /** 가용 · 속도 조건을 못 채워 최단거리로 대체했는지 */
    val fallbackUsed: Boolean,
    val statusObservedAt: Instant?,
)

/** 오류 응답 본문. message는 한국어 설명 */
data class ApiError(val error: String, val message: String)
