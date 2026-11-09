package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.altercharge.app.ui.card.common.ChargeVerdict
import com.altercharge.app.ui.designsystem.component.PlaceholderBox
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme

/** C 차량 상태 카드. 담당: 서희님 */
@Composable
fun CardVehicleStatus(
    state: CardVehicleStatusUiState,
    modifier: Modifier = Modifier,
) {
    // TODO 서희님: Figma card_vehicle_status (366:39011), 상황별 시안 (455:58776) 구현
    PlaceholderBox("C card_vehicle_status · ${state.situation} · ${state.socPercent ?: "--"}%", modifier)
}

@Preview(widthDp = 790, heightDp = 1040)
@Composable
private fun CardVehicleStatusPreview() {
    AlterChargeTheme {
        CardVehicleStatus(
            CardVehicleStatusUiState(
                situation = VehicleSituation.CHARGE_RECOMMENDED,
                verdict = ChargeVerdict.TODAY,
                driverName = "운전자",
                plug = PlugState.UNPLUGGED,
                socPercent = 22,
                rangeKm = 95,
                remainingMinutes = 45,
                doneAt = null,
                scheduledAt = null,
            ),
        )
    }
}
