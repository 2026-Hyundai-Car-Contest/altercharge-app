/** 차에서 읽은 값 + 서버 값 → C 차량 상태 카드 (REQ_VS_001 ~ REQ_VS_006) */
package com.altercharge.app.data.home

import com.altercharge.app.data.vehicle.VehicleTelemetry
import com.altercharge.app.domain.judgment.RECOMMEND_SOC_PERCENT
import com.altercharge.app.domain.time.KoreaZone
import com.altercharge.app.ui.card.vehiclestatus.CardVehicleStatusUiState
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * C 카드 값. 차량에서 읽는 값(SOC · 주행가능거리 · 충전 상태)과 서버에서 오는 값
 * (충전 권장 판정 · 가장 가까운 충전소 거리 · 마지막 충전 시각)을 합친다.
 * 예약 충전 시각 · 일시정지 사유는 MVP 제외라 넣지 않는다 (REQ_VS_007 · REQ_VS_008).
 */
fun VehicleTelemetry.toCardVehicleStatusUiState(
    driverName: String,
    serverValues: VehicleStatusServerValues? = null,
): CardVehicleStatusUiState = CardVehicleStatusUiState(
    driverName = driverName,
    charging = charging,
    socPercent = socPercent,
    rangeKm = rangeKm,
    remainingMinutes = remainingMinutes,
    fullAt = fullAt,
    lastChargedAt = serverValues?.lastChargedAt,
    nearestStationKm = serverValues?.nearestStationKm,
    fault = fault,
    lastUpdatedAt = observedAt?.let { it.atZone(KoreaZone).format(ClockFormat) },
    // 판정을 못 받았을 때는 배터리 구간으로만 가른다 (REQ_ST_005 예외와 같은 기준)
    chargeRecommended = serverValues?.chargeRecommended
        ?: (socPercent?.let { it <= RECOMMEND_SOC_PERCENT } ?: false),
)

/** 시안 표기 "오후 1:45" */
private val ClockFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
