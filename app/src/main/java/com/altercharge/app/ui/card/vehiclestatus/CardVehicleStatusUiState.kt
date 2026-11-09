package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.runtime.Immutable
import com.altercharge.app.ui.card.common.ChargeVerdict

/**
 * C 카드가 보여주는 상황. 15상황 보드(455:58776)의 01~12번이다.
 * 13~15번은 R1 · R2 · L2 쪽 상황이라 여기 없다.
 */
enum class VehicleSituation {
    /** 01 주차 · 충전 권장 */
    CHARGE_RECOMMENDED,

    /** 02 주차 · 출발 준비 완료 */
    READY,

    /** 03 충전 중 */
    CHARGING,

    /** 04 충전 중 · 배터리 부족 */
    CHARGING_LOW,

    /** 05 충전 중 · 배터리 위험 */
    CHARGING_CRITICAL,

    /** 06 충전 완료 */
    CHARGED,

    /** 07 충전 일시정지 */
    PAUSED,

    /** 08 예약 충전 */
    SCHEDULED,

    /** 09 배터리 위험 · 미연결 */
    CRITICAL_UNPLUGGED,

    /** 10 배터리 방전 */
    DEPLETED,

    /** 11 충전 오류 */
    CHARGE_ERROR,

    /** 12 차량 정보 없음 */
    NO_DATA,
}

/** 충전기 연결 칩 */
enum class PlugState { PLUGGED, UNPLUGGED, UNKNOWN }

/**
 * C 차량 상태 카드에 넘기는 값. Figma card_vehicle_status (366:39011).
 * 숫자와 상태만 담고, 문구 · 색 · 차량 이미지는 카드가 situation과 verdict로 고른다.
 *
 * 초안이다. 타일 항목은 C 카드 구현 때 Figma 상황별 시안을 보고 확정한다.
 */
@Immutable
data class CardVehicleStatusUiState(
    val situation: VehicleSituation,
    /** 주차 상황(01, 02)에서 헤드라인을 고른다: 오늘 / 내일 / 여유 */
    val verdict: ChargeVerdict,
    val driverName: String,
    val plug: PlugState,
    /** 배터리 %. 모르면 null ("--" 표시) */
    val socPercent: Int?,
    /** 갈 수 있는 거리 km. 모르면 null */
    val rangeKm: Int?,
    /** 충전 남은 시간, 또는 급속 80%까지 걸리는 시간 (분) */
    val remainingMinutes: Int?,
    /** 완충 · 완료 예정 시각 (예: "21:40") */
    val doneAt: String?,
    /** 예약 충전 시각 (예: "23:00") */
    val scheduledAt: String?,
)
