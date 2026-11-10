package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.runtime.Immutable
import com.altercharge.app.domain.judgment.RECOMMEND_SOC_PERCENT

/**
 * 충전 상태. Figma card_vehicle_status 속성 charging (366:39011 상태 매트릭스).
 */
enum class ChargingState {
    /** 충전 중 */
    CHARGING,

    /** 충전 완료 (100% 또는 목표 도달) */
    COMPLETED,

    /** 충전 일시정지 */
    PAUSED,

    /** 예약 충전 대기 */
    SCHEDULED,

    /** 충전 오류 */
    FAULT,

    /** 충전기에 연결 안 됨 (주차 · 주행) */
    UNPLUGGED,
}

/**
 * 배터리 구간. Figma 속성 battery, 배터리 구간 표 (377:11454).
 * 경계는 기능명세서 REQ_VS_001: 0% 방전, 1~20% 위험, 21~30% 부족, 31~100% 정상.
 */
enum class BatteryLevel {
    /** 31~100% */
    NORMAL,

    /** 21~30% */
    LOW,

    /** 1~20% */
    CRITICAL,

    /** 0% */
    EMPTY,

    /** 값 없음 */
    UNKNOWN,
    ;

    companion object {
        /** 이 값(%) 이하면 위험 */
        private const val CRITICAL_MAX = 20

        /** 이 값(%) 이하면 부족 */
        private const val LOW_MAX = 30

        fun fromSoc(socPercent: Int?): BatteryLevel = when {
            socPercent == null -> UNKNOWN
            socPercent <= 0 -> EMPTY
            socPercent <= CRITICAL_MAX -> CRITICAL
            socPercent <= LOW_MAX -> LOW
            else -> NORMAL
        }
    }
}

/**
 * C 카드가 보여주는 상황. 15상황 보드(455:58776)의 01~12번이고, 상태 매트릭스의 유효 조합 12개와 같다.
 * 카드가 charging과 배터리 구간으로 고르고, 미연결 01 · 02만 충전 권장 판정(REQ_ST_005)으로 고른다.
 * 13~15번은 R1 · R2 · L2 쪽 상황이라 여기 없다.
 */
enum class VehicleSituation {
    /** 01 주차 · 충전 권장 (unplugged, 충전 권장 판정) */
    CHARGE_RECOMMENDED,

    /** 02 주차 · 출발 준비 완료 (unplugged, 충전 권장 아님) */
    READY,

    /** 03 충전 중 (charging × normal) */
    CHARGING,

    /** 04 충전 중 · 배터리 부족 (charging × low) */
    CHARGING_LOW,

    /** 05 충전 중 · 배터리 위험 (charging × critical) */
    CHARGING_CRITICAL,

    /** 06 충전 완료 (completed) */
    CHARGED,

    /** 07 충전 일시정지 (paused) */
    PAUSED,

    /** 08 예약 충전 (scheduled) */
    SCHEDULED,

    /** 09 배터리 위험 · 미연결 (unplugged × critical) */
    CRITICAL_UNPLUGGED,

    /** 10 배터리 방전 (unplugged × empty) */
    DEPLETED,

    /** 11 충전 오류 (fault) */
    CHARGE_ERROR,

    /** 12 차량 정보 없음 (battery = unknown) */
    NO_DATA,
}

/** 연결 칩 (chip_status). 카드가 상황으로 고른다. */
enum class ConnectionChip { CONNECTED, UNPLUGGED, STANDBY, ERROR, OFFLINE }

/**
 * 일시정지 사유. 시안 문구가 있는 것만 둔다.
 * 사유 · 자동 재개 시각 표시는 MVP 제외(REQ_VS_007)라 사유를 더 늘리지 않는다. 일시정지 상태 자체는 보여 준다.
 */
enum class PauseReason { GRID_DEMAND }

/**
 * 충전 오류 종류. 기능명세서 REQ_EX_002의 3가지이고, 원인과 조치를 카드 타일로 보여 준다.
 */
enum class ChargeFault {
    /** 통신 오류. 조치: 재연결 후 재시도 */
    COMMUNICATION,

    /** 커넥터 체결 불량. 조치: 커넥터 다시 연결 */
    CONNECTOR,

    /** 충전기 고장 · E-11. 조치: 다른 충전기 사용 */
    CHARGER,
}

/**
 * C 차량 상태 카드에 넘기는 값. Figma card_vehicle_status (366:39011).
 * 숫자와 상태만 담고, 문구 · 색 · 칩 · 게이지는 카드가 charging과 배터리 구간으로 고른다.
 * 시각은 이미 "21:40"처럼 표시용으로 만든 문자열이다.
 */
@Immutable
data class CardVehicleStatusUiState(
    /** 운전자 이름 (헤드라인에 들어간다) */
    val driverName: String,
    val charging: ChargingState,
    /** 배터리 %. 모르면 null ("—" 표시, 12 차량 정보 없음) */
    val socPercent: Int?,
    /** 갈 수 있는 거리 km. 모르면 null */
    val rangeKm: Int?,
    /**
     * 남은 시간 (분). 충전 중일 때 완충까지 남은 시간. 미연결 위험(09)에서는 표시하지 않는다(기획 · 디자이너 확정).
     */
    val remainingMinutes: Int? = null,
    /** 충전 완료 시각. 충전 중 · 예약이면 예정 시각, 완료면 실제 완료 시각 */
    val fullAt: String? = null,
    /** 마지막 충전 완료 시각 (02 출발 준비 완료) */
    val lastChargedAt: String? = null,
    /** 가까운 충전소까지 거리 km (01 충전 권장) */
    val nearestStationKm: Double? = null,
    /** 예약 충전 시작 시각 (08 헤드라인). 예약 시각 표시는 MVP 제외(REQ_VS_008)라 목업 값만 넣는다 */
    val scheduledAt: String? = null,
    /** 일시정지 사유 (07). MVP 제외(REQ_VS_007), 목업 값만 넣는다 */
    val pauseReason: PauseReason? = null,
    /** 자동 재개 시각 (07). null이면 수동 재개. MVP 제외(REQ_VS_007), 목업 값만 넣는다 */
    val autoResumeAt: String? = null,
    /** 오류 종류 (11) */
    val fault: ChargeFault? = null,
    /** 마지막으로 값을 받은 시각 (12) */
    val lastUpdatedAt: String? = null,
    /** 다시 시도까지 남은 초 (12) */
    val retryInSeconds: Int? = null,
    /**
     * 충전 권장 판정 (REQ_ST_005). 미연결일 때 01 충전 권장 · 02 출발 준비 완료를 이 값으로 고른다.
     * 판정 결과(Judgment.chargeRecommended)를 넘긴다. 넘기지 않으면 배터리 30% 이하만 본다.
     * 위험(09) · 방전(10) · 정보 없음(12)은 배터리 구간이 먼저다.
     * 기본값은 만들 때 한 번만 정해져 copy(socPercent = …)로는 다시 계산되지 않으니, 실제 연결 때는 judge() 결과를 항상 넘긴다.
     */
    val chargeRecommended: Boolean = socPercent != null && socPercent <= RECOMMEND_SOC_PERCENT,
) {
    val batteryLevel: BatteryLevel get() = BatteryLevel.fromSoc(socPercent)

    val situation: VehicleSituation
        get() = when (charging) {
            ChargingState.FAULT -> VehicleSituation.CHARGE_ERROR
            ChargingState.COMPLETED -> VehicleSituation.CHARGED
            ChargingState.PAUSED -> VehicleSituation.PAUSED
            ChargingState.SCHEDULED -> VehicleSituation.SCHEDULED
            ChargingState.CHARGING -> when (batteryLevel) {
                BatteryLevel.NORMAL -> VehicleSituation.CHARGING
                BatteryLevel.LOW -> VehicleSituation.CHARGING_LOW
                // TODO 디자이너 확인: 충전 중 0%는 시안이 없어 위험 구간으로 보여준다.
                BatteryLevel.CRITICAL, BatteryLevel.EMPTY -> VehicleSituation.CHARGING_CRITICAL
                // TODO 디자이너 확인: 충전 중인데 SOC를 모르는 경우는 시안이 없어 정보 없음으로 보여준다.
                BatteryLevel.UNKNOWN -> VehicleSituation.NO_DATA
            }
            ChargingState.UNPLUGGED -> when (batteryLevel) {
                BatteryLevel.NORMAL, BatteryLevel.LOW ->
                    if (chargeRecommended) VehicleSituation.CHARGE_RECOMMENDED else VehicleSituation.READY
                BatteryLevel.CRITICAL -> VehicleSituation.CRITICAL_UNPLUGGED
                BatteryLevel.EMPTY -> VehicleSituation.DEPLETED
                BatteryLevel.UNKNOWN -> VehicleSituation.NO_DATA
            }
        }

    val chip: ConnectionChip
        get() = when (situation) {
            VehicleSituation.CHARGING,
            VehicleSituation.CHARGING_LOW,
            VehicleSituation.CHARGING_CRITICAL,
            VehicleSituation.CHARGED,
            VehicleSituation.PAUSED,
            -> ConnectionChip.CONNECTED
            VehicleSituation.SCHEDULED -> ConnectionChip.STANDBY
            VehicleSituation.CHARGE_ERROR -> ConnectionChip.ERROR
            VehicleSituation.NO_DATA -> ConnectionChip.OFFLINE
            VehicleSituation.CHARGE_RECOMMENDED,
            VehicleSituation.READY,
            VehicleSituation.CRITICAL_UNPLUGGED,
            VehicleSituation.DEPLETED,
            -> ConnectionChip.UNPLUGGED
        }
}
