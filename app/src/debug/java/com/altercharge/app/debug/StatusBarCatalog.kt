package com.altercharge.app.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.component.DriveState
import com.altercharge.app.ui.designsystem.component.StatusBar
import com.altercharge.app.ui.designsystem.component.StatusBarSize
import com.altercharge.app.ui.designsystem.component.StatusBarUiState
import com.altercharge.app.ui.designsystem.component.StatusValue
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * 상단 바 18 Variant 모음. Figma status_bar 컴포넌트 세트(444:40718)와 같은 순서 · 같은 간격이다:
 * state(parked · charging · arrived) × data(ready · loading · unavailable) × size(regular 1604 · compact 580),
 * 줄 간격 16 (미리보기는 여백 40). 맨 아래에 S-17 세로 폭(776) regular를 하나 더 둔다.
 * 미리보기와 디버그 확인 화면에서 같이 쓴다. 디버그 빌드에만 들어간다.
 */
@Composable
fun StatusBarCatalog(modifier: Modifier = Modifier) {
    Column(
        modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        DriveState.entries.forEach { driveState ->
            listOf(SampleReady, StatusValue.Loading, StatusValue.Unavailable).forEach { data ->
                val state = StatusBarUiState(driveState, battery = data, range = SampleRange(data), temperatureC = 12)
                StatusBar(state, Modifier.width(RegularWidth), size = StatusBarSize.REGULAR)
                StatusBar(state, Modifier.width(CompactWidth), size = StatusBarSize.COMPACT)
            }
        }
        StatusBar(
            StatusBarUiState(DriveState.CHARGING, battery = SampleReady, range = StatusValue.Ready(98), temperatureC = 12),
            Modifier.width(PortraitWidth),
            size = StatusBarSize.REGULAR,
        )
    }
}

// 시안 값: 32% · 98km · 12°. 배터리 값이 없으면 거리도 같은 상태로 둔다(어차피 숨긴다).
private val SampleReady = StatusValue.Ready(32)
private fun SampleRange(battery: StatusValue): StatusValue =
    if (battery is StatusValue.Ready) StatusValue.Ready(98) else battery

private val CatalogPadding = 40.dp
private val RegularWidth = 1604.dp
private val CompactWidth = 580.dp
private val PortraitWidth = 776.dp

@Preview(name = "StatusBar · 라이트", widthDp = 1684, heightDp = 1500)
@Composable
private fun StatusBarLightPreview() {
    AlterChargeTheme(darkTheme = false) {
        StatusBarCatalog(Modifier.background(AlterTheme.colors.background).padding(CatalogPadding))
    }
}

@Preview(name = "StatusBar · 다크", widthDp = 1684, heightDp = 1500)
@Composable
private fun StatusBarDarkPreview() {
    AlterChargeTheme(darkTheme = true) {
        StatusBarCatalog(Modifier.background(AlterTheme.colors.background).padding(CatalogPadding))
    }
}
