/**
 * C 카드 충전 오류(REQ_EX_002) 문구: 오류 종류별 상태 줄 · 원인 · 조치(값 + 단위). buildContent가 11 충전 오류에서 쓴다.
 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.altercharge.app.R

/** 충전 오류 문구: 상태 줄 · 원인 타일 · 조치 타일(값 + 단위) */
internal class FaultText(val status: String, val cause: String, val action: String, val actionUnit: String)

/**
 * 오류 종류별 문구 (REQ_EX_002). 충전기 고장 · 커넥터 체결 불량은 06 Strings card_vehicle_status/fault/…
 * (charger_fault(_action), connector_error(_action)) 확정 문구다 (개발자 Q&A Q6).
 */
@Composable
internal fun faultText(fault: ChargeFault): FaultText = when (fault) {
    ChargeFault.COMMUNICATION -> FaultText(
        status = stringResource(R.string.card_vehicle_status_status_fault_communication),
        cause = stringResource(R.string.card_vehicle_status_tile_cause_communication),
        action = stringResource(R.string.card_vehicle_status_tile_action_reconnect),
        actionUnit = stringResource(R.string.card_vehicle_status_unit_then_retry),
    )
    ChargeFault.CONNECTOR -> FaultText(
        status = stringResource(R.string.card_vehicle_status_status_fault_connector),
        cause = stringResource(R.string.card_vehicle_status_tile_cause_connector),
        action = stringResource(R.string.card_vehicle_status_tile_action_connector),
        actionUnit = stringResource(R.string.card_vehicle_status_unit_reconnect_connector),
    )
    ChargeFault.CHARGER -> FaultText(
        status = stringResource(R.string.card_vehicle_status_status_fault_charger),
        cause = stringResource(R.string.card_vehicle_status_tile_cause_charger),
        action = stringResource(R.string.card_vehicle_status_tile_action_another_charger),
        actionUnit = stringResource(R.string.card_vehicle_status_unit_use_another_charger),
    )
}
