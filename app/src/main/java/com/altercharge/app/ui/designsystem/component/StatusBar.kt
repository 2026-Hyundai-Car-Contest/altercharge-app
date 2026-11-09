package com.altercharge.app.ui.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** 차량 상태. 상단 바 칩에 쓴다. */
enum class DriveState { PARKED, DRIVING, CHARGING, ARRIVED }

/**
 * 숫자 값의 상태. 디자이너 확정: 불러오는 중이면 "확인 중", 제공되지 않거나 확인할 수 없으면 "정보 없음".
 */
sealed interface StatusValue {
    data class Ready(val value: Int) : StatusValue
    data object Loading : StatusValue
    data object Unavailable : StatusValue
}

/** 앱 상단 바에 넘기는 값. Figma status_bar (455:58782). */
@Immutable
data class StatusBarUiState(
    val driveState: DriveState,
    /** 배터리 % */
    val battery: StatusValue,
    /** 갈 수 있는 거리 km */
    val range: StatusValue,
    /** 실내 온도 °C. 모르면 null (온도 항목을 숨긴다) */
    val temperatureC: Int?,
)

// 디자인 값에 없는 상단 바 고유 치수 (Figma status_bar 444:40718)
private val StatusBarHeight = 56.dp
private val GearCircleSize = 28.dp
private val GearChipPaddingStart = 6.dp
private val GearChipPaddingEnd = 14.dp
private val DividerWidth = 1.5.dp
private val DividerHeight = 20.dp
private val BatteryGap = 6.dp
private val ConnectivityIconSize = 24.dp
private val ConnectivityGap = 12.dp

/** 기획 확정: 연결 아이콘과 시간은 실제 값 대신 더미로 표시한다 (시안 값). TODO 실제 시각을 연결하면 지운다. */
private const val DummyTime = "14:05"

/** 기어 원 안 글자. 시안 기준 흰색 고정 (라이트/다크 같음) */
private val GearLetterColor = Color.White

/**
 * 앱 상단 바. 왼쪽 앱 이름, 오른쪽 차량 상태 칩 · 배터리 · 온도 · 연결 아이콘 · 시간.
 * 폭이 좁으면 앱 이름이 말줄임되고 오른쪽 그룹은 그대로 둔다(줄바꿈 없음).
 * 연결 아이콘(블루투스 · 와이파이 · LTE)과 시간은 기획 확정에 따라 더미로 보여준다.
 */
@Composable
fun StatusBar(
    state: StatusBarUiState,
    modifier: Modifier = Modifier,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val labelStrong = typography.labelSmall.copy(fontWeight = AlterFontWeight.Strong)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(StatusBarHeight)
            .padding(horizontal = AlterPadding.XSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SingleLineText(
            text = stringResource(R.string.app_name),
            style = labelStrong,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f, fill = false),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GearChip(state.driveState)
            VerticalDivider()
            Row(
                horizontalArrangement = Arrangement.spacedBy(BatteryGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 둘 다 값이 없고 상태가 같으면 문구를 한 번만 보여준다 ("확인 중" 또는 "정보 없음")
                if (state.battery !is StatusValue.Ready && state.battery == state.range) {
                    SingleLineText(
                        text = state.battery.text(R.string.status_bar_battery_percent),
                        style = typography.labelSmall,
                        color = colors.textTertiary,
                    )
                } else {
                    SingleLineText(
                        text = state.battery.text(R.string.status_bar_battery_percent),
                        style = if (state.battery is StatusValue.Ready) labelStrong else typography.labelSmall,
                        color = if (state.battery is StatusValue.Ready) colors.textPrimary else colors.textTertiary,
                    )
                    SingleLineText(
                        text = state.range.text(R.string.status_bar_range),
                        style = typography.labelSmall,
                        color = colors.textTertiary,
                    )
                }
            }
            state.temperatureC?.let {
                SingleLineText(
                    text = stringResource(R.string.status_bar_temperature, it),
                    style = typography.labelSmall,
                    color = colors.textSecondary,
                )
            }
            VerticalDivider()
            Row(
                horizontalArrangement = Arrangement.spacedBy(ConnectivityGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(
                    R.drawable.ic_status_bluetooth,
                    R.drawable.ic_status_wifi,
                    R.drawable.ic_status_lte,
                ).forEach {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(ConnectivityIconSize),
                    )
                }
            }
            SingleLineText(text = DummyTime, style = labelStrong, color = colors.textPrimary)
        }
    }
}

/** 차량 상태 칩. 파란 원 안 기어 글자 + 상태 문구 */
@Composable
private fun GearChip(driveState: DriveState) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val capsule = RoundedCornerShape(AlterRadius.Full)
    Row(
        modifier = Modifier
            .background(colors.surfaceAccent, capsule)
            .padding(
                PaddingValues(
                    start = GearChipPaddingStart,
                    end = GearChipPaddingEnd,
                    top = AlterPadding.XXSmall,
                    bottom = AlterPadding.XXSmall,
                ),
            ),
        horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(GearCircleSize)
                .background(colors.blue500, capsule),
            contentAlignment = Alignment.Center,
        ) {
            driveState.gearLetterRes()?.let {
                SingleLineText(
                    text = stringResource(it),
                    style = typography.labelXSmall.copy(fontWeight = AlterFontWeight.Strong),
                    color = GearLetterColor,
                )
            }
        }
        SingleLineText(
            text = stringResource(driveState.labelRes()),
            style = typography.labelSmall,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        Modifier
            .width(DividerWidth)
            .height(DividerHeight)
            .background(AlterTheme.colors.surfaceAccent),
    )
}

@Composable
private fun SingleLineText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
    )
}

/** 값 상태를 문구로. 값이 있으면 [format]에 숫자를 넣는다. */
@Composable
private fun StatusValue.text(@StringRes format: Int): String = when (this) {
    is StatusValue.Ready -> stringResource(format, value.toString())
    StatusValue.Loading -> stringResource(R.string.common_value_loading)
    StatusValue.Unavailable -> stringResource(R.string.common_value_unavailable)
}

@StringRes
private fun DriveState.labelRes(): Int = when (this) {
    DriveState.PARKED -> R.string.status_bar_parked
    DriveState.DRIVING -> R.string.status_bar_driving
    DriveState.CHARGING -> R.string.status_bar_charging
    DriveState.ARRIVED -> R.string.status_bar_arrived
}

/** 원 안 기어 글자. 시안은 주차(P)만 있다. */
@StringRes
private fun DriveState.gearLetterRes(): Int? = when (this) {
    DriveState.PARKED -> R.string.status_bar_gear_parked
    // TODO 디자이너 확인: 주행 · 충전 · 도착일 때 원 안 글자(와 원 색). 확정 전까지 빈 원으로 둔다.
    DriveState.DRIVING, DriveState.CHARGING, DriveState.ARRIVED -> null
}
