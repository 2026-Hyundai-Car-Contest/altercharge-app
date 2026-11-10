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

/**
 * 차량 상태. 상단 바 칩의 원 안 기호와 문구를 정한다.
 * Figma status_bar (444:40718) state: parked(P) · charging(⚡) · arrived(✓). 주행 중은 시안에 없다.
 */
enum class DriveState { PARKED, CHARGING, ARRIVED }

/**
 * 숫자 값의 상태. 디자이너 확정: 불러오는 중이면 "확인 중", 제공되지 않거나 확인할 수 없으면 "정보 없음".
 * 응답이 없거나 시간이 지나면 Unavailable로 바꾼다 (Figma status_bar 설명 601:57722).
 */
sealed interface StatusValue {
    data class Ready(val value: Int) : StatusValue
    data object Loading : StatusValue
    data object Unavailable : StatusValue
}

/**
 * 앱 상단 바에 넘기는 값. Figma status_bar (444:40718)의 data variant는 [battery]가 정한다:
 * Ready면 "32%"(와 [range]가 Ready일 때 "98km"), Loading이면 "확인 중", Unavailable이면 "정보 없음".
 * 배터리 값이 없으면 km는 숨긴다.
 */
@Immutable
data class StatusBarUiState(
    val driveState: DriveState,
    /** 배터리 % */
    val battery: StatusValue,
    /** 갈 수 있는 거리 km. Ready가 아니면 숨긴다. */
    val range: StatusValue,
    /** 실내 온도 °C. 모르면 null (온도 항목을 숨긴다) */
    val temperatureC: Int?,
)

/**
 * 상단 바 크기 (Figma variant size).
 * - REGULAR: 가로형 화면(S-17 · W-Standard · W-Extended) · 전체 항목.
 * - COMPACT: WF 세로 등 좁은 화면. 앱 이름 · 상태 칩 · 배터리 % · 시간만 (km · 온도 · 연결 아이콘 생략).
 */
enum class StatusBarSize { REGULAR, COMPACT }

/** 폼팩터에 맞는 상단 바 크기. Figma 화면 기준 compact는 WF 세로(452:54321)뿐이다. */
fun statusBarSizeFor(layout: AppLayoutInfo): StatusBarSize =
    if (layout.formFactor == FormFactor.WF && !layout.isLandscape) StatusBarSize.COMPACT else StatusBarSize.REGULAR

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
private val BoltIconSize = 20.dp
private val CheckIconSize = 16.dp

/** 기획 확정: 연결 아이콘과 시간은 실제 값 대신 더미로 표시한다 (시안 값). TODO 실제 시각을 연결하면 지운다. */
private const val DummyTime = "14:05"

/**
 * 상태 원 색 (Figma 05 Components status_bar/gear/{parked, charging, arrived, icon, arrived_icon}). 상단 바에서만 쓰는 값이라 여기 둔다.
 * parked는 blue_500(L #3B6FE0 / D #3562C4)과 같다.
 */
private data class GearPalette(
    val parked: Color,
    val charging: Color,
    val arrived: Color,
    /** P 글자 · 번개 */
    val icon: Color,
    /** 도착 체크 */
    val arrivedIcon: Color,
)

private val GearPaletteLight = GearPalette(
    parked = Color(0xFF3B6FE0),
    charging = Color(0xFF00B975),
    arrived = Color(0xFF131417),
    icon = Color(0xFFFFFFFF),
    arrivedIcon = Color(0xFFFFFFFF),
)

private val GearPaletteDark = GearPalette(
    parked = Color(0xFF3562C4),
    charging = Color(0xFF00B975),
    arrived = Color(0xFFFFFFFF),
    icon = Color(0xFFFFFFFF),
    arrivedIcon = Color(0xFF131417),
)

/**
 * 앱 상단 바. 왼쪽 앱 이름, 오른쪽 차량 상태 칩 · 배터리 · 온도 · 연결 아이콘 · 시간.
 * 폭이 좁으면 앱 이름이 말줄임되고 오른쪽 그룹은 그대로 둔다(줄바꿈 없음).
 * 연결 아이콘(블루투스 · 와이파이 · LTE)과 시간은 기획 확정에 따라 더미로 보여준다.
 *
 * @param size 기본값은 [AlterChargeLayout][com.altercharge.app.ui.designsystem.layout.AlterChargeLayout]이
 *   고른 폼팩터로 정한다 ([statusBarSizeFor]).
 */
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
            // regular 24 (margin_large) · compact 16 (margin_normal)
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
            SingleLineText(text = DummyTime, style = labelStrong, color = colors.textPrimary)
        }
    }
}

/** 차량 상태 칩. 상태 원(P · ⚡ · ✓) + 상태 문구 */
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
    // Figma divider는 basic_200 (L #E0E1E6 / D #44464E) = surface_accent와 같은 값
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
