/** C 카드 충전 오류(REQ_EX_002) 문구 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.altercharge.app.R

internal class FaultText(val status: String, val cause: String, val action: String, val actionUnit: String)

/** 충전기 고장 · 커넥터 체결 불량은 06 Strings card_vehicle_status/fault/… 확정 문구다 (Q&A Q6) */
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
