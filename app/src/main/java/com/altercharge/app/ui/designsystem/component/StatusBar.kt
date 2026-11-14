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
import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import com.altercharge.app.ui.designsystem.layout.LocalAppLayout
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.DarkAlterColors
import com.altercharge.app.ui.designsystem.theme.LightAlterColors

/**
 * Figma status_bar (444:40718) state. 주행 중은 시안에 없다.
 * ARRIVED는 안내한 충전소 도착용이라 MVP(주차 화면)에서는 쓰지 않는다.
 */
enum class DriveState { PARKED, CHARGING, ARRIVED }

/** 응답이 없거나 시간이 지나면 Unavailable로 바꾼다 (Figma status_bar 설명 601:57722) */
sealed interface StatusValue {
    data class Ready(val value: Int) : StatusValue
    data object Loading : StatusValue
    data object Unavailable : StatusValue
}

/** Figma status_bar의 data variant는 [battery]가 정한다. 배터리 값이 없으면 km도 숨긴다 */
@Immutable
data class StatusBarUiState(
    val driveState: DriveState,
    val battery: StatusValue,
    val range: StatusValue,
    /** null이면 숨긴다 */
    val temperatureC: Int?,
    /** 상단 바 시각 "HH:mm". null이면 시안 값(디자인 카탈로그 · 미리보기용) */
    val time: String? = null,
)

/** Figma variant size. COMPACT는 km · 온도 · 연결 아이콘을 뺀다 */
enum class StatusBarSize { REGULAR, COMPACT }

/** Figma 화면 기준 compact는 WF 세로(452:54321)뿐이다 */
fun statusBarSizeFor(layout: AppLayoutInfo): StatusBarSize =
    if (layout.formFactor == FormFactor.WF && !layout.isLandscape) StatusBarSize.COMPACT else StatusBarSize.REGULAR

// 디자인 토큰에 없는 고유 치수 (Figma status_bar 444:40718)
private val StatusBarHeight = 56.dp
private val GearCircleSize = 28.dp
private val GearChipPaddingStart = 6.dp
private val GearChipPaddingEnd = 14.dp
private val DividerWidth = 1.5.dp
private val DividerHeight = 20.dp
private val BatteryGap = 6.dp
private val ConnectivityIconSize = 24.dp
private val ConnectivityGap = 12.dp
private val BoltIconSize = 20.dp
private val CheckIconSize = 16.dp

/** 연결 아이콘은 시안 값 더미. 시각은 홈에서 실제 시각을 넘기고, 안 넘기면 이 시안 값을 쓴다 */
private const val DummyTime = "14:05"

/** Figma 05 Components status_bar/gear/{…}. 상단 바에서만 써서 여기 둔다 */
private data class GearPalette(
    val parked: Color,
    val charging: Color,
    val arrived: Color,
    /** P 글자 · 번개 */
    val icon: Color,
    val arrivedIcon: Color,
)

private val GearPaletteLight = GearPalette(
    parked = LightAlterColors.blue500,
    charging = Color(0xFF00B975),
    arrived = Color(0xFF131417),
    icon = Color(0xFFFFFFFF),
    arrivedIcon = Color(0xFFFFFFFF),
)

private val GearPaletteDark = GearPalette(
    parked = DarkAlterColors.blue500,
    charging = Color(0xFF00B975),
    arrived = Color(0xFFFFFFFF),
    icon = Color(0xFFFFFFFF),
    arrivedIcon = Color(0xFF131417),
)

/** 폭이 좁으면 앱 이름만 말줄임되고 오른쪽 그룹은 그대로 둔다 */
@Composable
fun StatusBar(
    state: StatusBarUiState,
    modifier: Modifier = Modifier,
    size: StatusBarSize = statusBarSizeFor(LocalAppLayout.current),
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val labelStrong = typography.labelSmall.copy(fontWeight = AlterFontWeight.Strong)
    val regular = size == StatusBarSize.REGULAR

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
            horizontalArrangement = Arrangement.spacedBy(if (regular) AlterMargin.Large else AlterMargin.Normal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GearChip(state.driveState)
            VerticalDivider()
            Row(
                horizontalArrangement = Arrangement.spacedBy(BatteryGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val battery = state.battery
                if (battery is StatusValue.Ready) {
                    SingleLineText(
                        text = stringResource(R.string.status_bar_battery_percent, battery.value.toString()),
                        style = labelStrong,
                        color = colors.textPrimary,
                    )
                    val range = state.range
                    if (regular && range is StatusValue.Ready) {
                        SingleLineText(
                            text = stringResource(R.string.status_bar_range, range.value.toString()),
                            style = typography.labelSmall,
                            color = colors.textTertiary,
                        )
                    }
                } else {
                    SingleLineText(
                        text = stringResource(
                            if (battery == StatusValue.Loading) R.string.common_value_loading else R.string.common_value_unavailable,
                        ),
                        style = typography.labelSmall,
                        color = colors.textTertiary,
                    )
                }
            }
            if (regular) {
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
            }
            SingleLineText(text = state.time ?: DummyTime, style = labelStrong, color = colors.textPrimary)
        }
    }
}

@Composable
private fun GearChip(driveState: DriveState) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val gear = if (colors.isDark) GearPaletteDark else GearPaletteLight
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
        val circle = when (driveState) {
            DriveState.PARKED -> gear.parked
            DriveState.CHARGING -> gear.charging
            DriveState.ARRIVED -> gear.arrived
        }
        Box(
            modifier = Modifier
                .size(GearCircleSize)
                .background(circle, capsule),
            contentAlignment = Alignment.Center,
        ) {
            when (driveState) {
                DriveState.PARKED -> SingleLineText(
                    text = stringResource(R.string.status_bar_gear_parked),
                    style = typography.labelXSmall.copy(fontWeight = AlterFontWeight.Strong),
                    color = gear.icon,
                )
                DriveState.CHARGING -> Icon(
                    painter = painterResource(R.drawable.ic_status_bolt),
                    contentDescription = null,
                    tint = gear.icon,
                    modifier = Modifier.size(BoltIconSize),
                )
                DriveState.ARRIVED -> Icon(
                    painter = painterResource(R.drawable.ic_status_check),
                    contentDescription = null,
                    tint = gear.arrivedIcon,
                    modifier = Modifier.size(CheckIconSize),
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
    // Figma divider basic_200 = surface_accent와 같은 값
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

@StringRes
private fun DriveState.labelRes(): Int = when (this) {
    DriveState.PARKED -> R.string.status_bar_parked
    DriveState.CHARGING -> R.string.status_bar_charging
    DriveState.ARRIVED -> R.string.status_bar_arrived
}
