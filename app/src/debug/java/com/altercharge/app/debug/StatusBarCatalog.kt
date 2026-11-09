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
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** 상단 바 상태 모음. 미리보기와 디버그 확인 화면에서 같이 쓴다. 디버그 빌드에만 들어간다. */
@Composable
fun StatusBarCatalog(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
        // 기본 (시안 값)
        StatusBar(StatusBarUiState(DriveState.PARKED, socPercent = 32, rangeKm = 98, temperatureC = 12))
        // 값 없음: 배터리/거리 "--", 온도 숨김
        StatusBar(StatusBarUiState(DriveState.PARKED, socPercent = null, rangeKm = null, temperatureC = null))
        // 차량 상태별 칩 문구
        StatusBar(StatusBarUiState(DriveState.DRIVING, socPercent = 64, rangeKm = 210, temperatureC = -3))
        StatusBar(StatusBarUiState(DriveState.CHARGING, socPercent = 80, rangeKm = 300, temperatureC = 22))
        StatusBar(StatusBarUiState(DriveState.ARRIVED, socPercent = 5, rangeKm = 12, temperatureC = 30))
        // 세로 화면 폭 (776)
        StatusBar(
            StatusBarUiState(DriveState.PARKED, socPercent = 32, rangeKm = 98, temperatureC = 12),
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
