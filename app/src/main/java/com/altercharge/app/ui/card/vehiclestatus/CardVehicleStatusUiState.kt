package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.runtime.Immutable
import com.altercharge.app.domain.judgment.RECOMMEND_SOC_PERCENT

/** Figma card_vehicle_status 속성 charging (366:39011 상태 매트릭스) */
enum class ChargingState {
    CHARGING,

    /** 100% 또는 목표 도달 */
    COMPLETED,
    PAUSED,
    SCHEDULED,
    FAULT,

    /** 주차 · 주행 */
    UNPLUGGED,
}

/**
 * Figma 속성 battery, 배터리 구간 표 (377:11454).
 * 경계는 REQ_VS_001: 0% 방전, 1~20% 위험, 21~30% 부족, 31~100% 정상.
 */
enum class BatteryLevel {
    NORMAL,
    LOW,
    CRITICAL,
    EMPTY,
    UNKNOWN,
    ;

    companion object {
        private const val CRITICAL_MAX = 20
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
 * 15상황 보드(455:58776)의 01~12번 = 상태 매트릭스의 유효 조합 12개. 13~15번은 R1 · R2 · L2 쪽이라 없다.
 * 미연결 01 · 02만 충전 권장 판정(REQ_ST_005)으로 가른다.
 */
enum class VehicleSituation {
    /** 01 */
    CHARGE_RECOMMENDED,

    /** 02 */
    READY,

    /** 03 */
    CHARGING,

    /** 04 */
    CHARGING_LOW,

    /** 05 */
    CHARGING_CRITICAL,

    /** 06 */
    CHARGED,

    /** 07 */
    PAUSED,

    /** 08 */
    SCHEDULED,

    /** 09 */
    CRITICAL_UNPLUGGED,

    /** 10 */
    DEPLETED,

    /** 11 */
    CHARGE_ERROR,

    /** 12 */
    NO_DATA,
}

/** Figma chip_status */
enum class ConnectionChip { CONNECTED, UNPLUGGED, STANDBY, ERROR, OFFLINE }

/** 시안 문구가 있는 것만. 사유 표시는 MVP 제외(REQ_VS_007)라 늘리지 않는다 */
enum class PauseReason { GRID_DEMAND }

/** REQ_EX_002의 3가지 */
enum class ChargeFault {
    COMMUNICATION,
    CONNECTOR,

    /** E-11 */
    CHARGER,
}

/**
 * Figma card_vehicle_status (366:39011). 숫자와 상태만 담고 문구 · 색 · 칩 · 게이지는 카드가 고른다.
 * 시각은 "21:40"처럼 표시용 문자열이다.
 */
@Immutable
data class CardVehicleStatusUiState(
    /** MVP는 고정값이라 stringResource(R.string.common_driver_name)을 넘긴다 */
    val driverName: String,
    val charging: ChargingState,
    /** null이면 12 차량 정보 없음 */
    val socPercent: Int?,
    val rangeKm: Int?,
    /** 미연결 위험(09)에서는 표시하지 않는다(기획 · 디자이너 확정) */
    val remainingMinutes: Int? = null,
    /** 충전 중 · 예약이면 예정 시각, 완료면 실제 완료 시각 */
    val fullAt: String? = null,
    val lastChargedAt: String? = null,
    val nearestStationKm: Double? = null,
    /** MVP 제외(REQ_VS_008)라 목업 값만 */
    val scheduledAt: String? = null,
    /** MVP 제외(REQ_VS_007)라 목업 값만 */
    val pauseReason: PauseReason? = null,
    /** null이면 수동 재개. MVP 제외(REQ_VS_007)라 목업 값만 */
    val autoResumeAt: String? = null,
    val fault: ChargeFault? = null,
    val lastUpdatedAt: String? = null,
    val retryInSeconds: Int? = null,
    /**
     * REQ_ST_005 판정 결과. 미연결 01 · 02만 이 값으로 가르고, 09 · 10 · 12는 배터리 구간이 먼저다.
     * 기본값은 생성 때 한 번만 계산돼 copy(socPercent = …)로 바뀌지 않으니 실제 연결에서는 judge() 결과를 넘긴다.
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
