package com.altercharge.app.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.component.DriveState
import com.altercharge.app.ui.designsystem.component.StatusBar
import com.altercharge.app.ui.designsystem.component.StatusBarUiState
import com.altercharge.app.ui.designsystem.component.StatusValue
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** 상단 바 상태 모음. 미리보기와 디버그 확인 화면에서 같이 쓴다. 디버그 빌드에만 들어간다. */
@Composable
fun StatusBarCatalog(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
        // 기본 (시안 값)
        StatusBar(StatusBarUiState(DriveState.PARKED, battery = StatusValue.Ready(32), range = StatusValue.Ready(98), temperatureC = 12))
        // 확인 중: 불러오는 중
        StatusBar(StatusBarUiState(DriveState.PARKED, battery = StatusValue.Loading, range = StatusValue.Loading, temperatureC = 12))
        // 정보 없음: 제공되지 않음, 온도 숨김
        StatusBar(StatusBarUiState(DriveState.PARKED, battery = StatusValue.Unavailable, range = StatusValue.Unavailable, temperatureC = null))
        // 차량 상태별 칩 문구
        StatusBar(StatusBarUiState(DriveState.DRIVING, battery = StatusValue.Ready(64), range = StatusValue.Ready(210), temperatureC = -3))
        StatusBar(StatusBarUiState(DriveState.CHARGING, battery = StatusValue.Ready(80), range = StatusValue.Ready(300), temperatureC = 22))
        StatusBar(StatusBarUiState(DriveState.ARRIVED, battery = StatusValue.Ready(5), range = StatusValue.Ready(12), temperatureC = 30))
        // 세로 화면 폭 (776)
        StatusBar(
            StatusBarUiState(DriveState.PARKED, battery = StatusValue.Ready(32), range = StatusValue.Ready(98), temperatureC = 12),
            Modifier.width(PortraitPreviewWidth),
        )
    }
}

private val PortraitPreviewWidth = 776.dp

@Preview(name = "StatusBar · 라이트", widthDp = 1604)
@Composable
private fun StatusBarLightPreview() {
    AlterChargeTheme(darkTheme = false) {
        StatusBarCatalog(Modifier.background(AlterTheme.colors.background).padding(AlterPadding.XLarge))
    }
}

@Preview(name = "StatusBar · 다크", widthDp = 1604)
@Composable
private fun StatusBarDarkPreview() {
    AlterChargeTheme(darkTheme = true) {
        StatusBarCatalog(Modifier.background(AlterTheme.colors.background).padding(AlterPadding.XLarge))
    }
}
