package com.altercharge.app.ui.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme

/** 차량 상태. 상단 바 칩에 쓴다. */
enum class DriveState { PARKED, DRIVING, CHARGING, ARRIVED }

/** 앱 상단 바에 넘기는 값. Figma status_bar (455:58782). */
@Immutable
data class StatusBarUiState(
    val driveState: DriveState,
    /** 배터리 %. 모르면 null */
    val socPercent: Int?,
    /** 갈 수 있는 거리 km. 모르면 null */
    val rangeKm: Int?,
    /** 실내 온도 °C. 모르면 null */
    val temperatureC: Int?,
)

/**
 * 앱 상단 바. 로고 · 차량 상태 칩 · 배터리 · 온도를 보여준다.
 *
 * 지금은 뼈대다. 칩 모양과 디자인 값은 디자인 값 작업 때 채운다.
 * TODO 기획 확인: 시안의 블루투스 · 와이파이 · 신호 · 시간을 앱이 실제 값으로 보여줄지
 */
@Composable
fun StatusBar(
    state: StatusBarUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.app_name))
        Spacer(Modifier.weight(1f))
        Text(stringResource(state.driveState.labelRes()))
        Text(
            stringResource(
                R.string.status_bar_battery,
                state.socPercent?.toString() ?: "--",
                state.rangeKm?.toString() ?: "--",
            ),
        )
        state.temperatureC?.let { Text(stringResource(R.string.status_bar_temperature, it)) }
    }
}

private fun DriveState.labelRes(): Int = when (this) {
    DriveState.PARKED -> R.string.status_bar_parked
    DriveState.DRIVING -> R.string.status_bar_driving
    DriveState.CHARGING -> R.string.status_bar_charging
    DriveState.ARRIVED -> R.string.status_bar_arrived
}

@Preview(widthDp = 1604)
@Composable
private fun StatusBarPreview() {
    AlterChargeTheme {
        StatusBar(StatusBarUiState(DriveState.PARKED, socPercent = 32, rangeKm = 98, temperatureC = 12))
    }
}
