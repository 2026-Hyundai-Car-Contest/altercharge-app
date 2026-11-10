package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

// ---------------------------------------------------------------------------
// 미리보기 (샘플: CardVehicleStatusSampleData)
// ---------------------------------------------------------------------------

@Composable
private fun LandscapePreview(state: CardVehicleStatusUiState, dark: Boolean = false) {
    AlterChargeTheme(darkTheme = dark) {
        Box(
            Modifier
                .background(AlterTheme.colors.background)
                .padding(AlterPadding.Normal),
        ) {
            CardVehicleStatus(
                state = state,
                modifier = Modifier.size(width = 790.dp, height = 1040.dp),
            )
        }
    }
}

@Composable
private fun PortraitPreview(state: CardVehicleStatusUiState, dark: Boolean = false) {
    AlterChargeTheme(darkTheme = dark) {
        Box(
            Modifier
                .background(AlterTheme.colors.background)
                .padding(AlterPadding.Normal),
        ) {
            CardVehicleStatus(
                state = state,
                mode = CardVehicleStatusMode.PORTRAIT,
                modifier = Modifier.size(width = 776.dp, height = 652.dp),
            )
        }
    }
}

@Preview(name = "01 충전 권장 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewChargeRecommended() = LandscapePreview(CardVehicleStatusSampleData.ChargeRecommended)

@Preview(name = "03 충전 중 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewCharging() = LandscapePreview(CardVehicleStatusSampleData.Charging)

@Preview(name = "03 충전 중 · 가로 · 다크", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewChargingDark() = LandscapePreview(CardVehicleStatusSampleData.Charging, dark = true)

@Preview(name = "03 충전 중 · 가로 · EN", widthDp = 822, heightDp = 1072, locale = "en")
@Composable
private fun PreviewChargingEn() = LandscapePreview(CardVehicleStatusSampleData.Charging)

@Preview(name = "05 배터리 위험 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewChargingCritical() = LandscapePreview(CardVehicleStatusSampleData.ChargingCritical)

@Preview(name = "06 충전 완료 · 가로 · 다크", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewCharged() = LandscapePreview(CardVehicleStatusSampleData.Charged, dark = true)

@Preview(name = "07 일시정지 · 가로 · EN", widthDp = 822, heightDp = 1072, locale = "en")
@Composable
private fun PreviewPaused() = LandscapePreview(CardVehicleStatusSampleData.Paused)

@Preview(name = "09 위험 · 미연결 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewCriticalUnplugged() = LandscapePreview(CardVehicleStatusSampleData.CriticalUnplugged)

@Preview(name = "10 방전 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewDepleted() = LandscapePreview(CardVehicleStatusSampleData.Depleted)

@Preview(name = "11 충전 오류 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewChargeError() = LandscapePreview(CardVehicleStatusSampleData.ChargeError)

@Preview(name = "12 정보 없음 · 가로 · 다크", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewNoData() = LandscapePreview(CardVehicleStatusSampleData.NoData, dark = true)

@Preview(name = "01 충전 권장 · 세로", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitChargeRecommended() = PortraitPreview(CardVehicleStatusSampleData.ChargeRecommended)

@Preview(name = "03 충전 중 · 세로", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitCharging() = PortraitPreview(CardVehicleStatusSampleData.Charging)

@Preview(name = "03 충전 중 · 세로 · 다크 · EN", widthDp = 808, heightDp = 684, locale = "en")
@Composable
private fun PreviewPortraitChargingDarkEn() = PortraitPreview(CardVehicleStatusSampleData.Charging, dark = true)

@Preview(name = "05 배터리 위험 · 세로 · 다크", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitCritical() = PortraitPreview(CardVehicleStatusSampleData.ChargingCritical, dark = true)

@Preview(name = "11 충전 오류 · 세로", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitError() = PortraitPreview(CardVehicleStatusSampleData.ChargeError)

@Preview(name = "12 정보 없음 · 세로", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitNoData() = PortraitPreview(CardVehicleStatusSampleData.NoData)

@Preview(name = "07 일시정지 · 세로 · EN (헤드라인 줄바꿈)", widthDp = 808, heightDp = 684, locale = "en")
@Composable
private fun PreviewPortraitPausedEn() = PortraitPreview(CardVehicleStatusSampleData.Paused)
