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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/** 앱 상단 바에 넘기는 값. Figma status_bar (455:58782). */
@Immutable
data class StatusBarUiState(
    val driveState: DriveState,
    /** 배터리 %. 모르면 null */
    val socPercent: Int?,
    /** 갈 수 있는 거리 km. 모르면 null */
    val rangeKm: Int?,
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

/** 기어 원 안 글자. 시안 기준 흰색 고정 (라이트/다크 같음) */
private val GearLetterColor = Color.White

/**
 * 앱 상단 바. 왼쪽 앱 이름, 오른쪽 차량 상태 칩 · 배터리 · 온도.
 * 폭이 좁으면 앱 이름이 말줄임되고 오른쪽 그룹은 그대로 둔다(줄바꿈 없음).
 *
 * TODO 기획 확인: 시안의 블루투스 · 와이파이 · LTE 아이콘과 시간을 앱이 실제 값으로 보여줄지.
 * 확정되면 온도 뒤에 구분선과 함께 넣는다(아이콘 24, 간격 12, 시간 label/small Bold textPrimary). 아이콘 에셋도 아직 없다.
 */
@Composable
fun StatusBar(
    state: StatusBarUiState,
    modifier: Modifier = Modifier,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val labelStrong = typography.labelSmall.copy(fontWeight = AlterFontWeight.Strong)
    val unknown = stringResource(R.string.status_bar_value_unknown)

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
                SingleLineText(
                    text = stringResource(
                        R.string.status_bar_battery_percent,
                        state.socPercent?.toString() ?: unknown,
                    ),
                    style = labelStrong,
                    color = colors.textPrimary,
                )
                SingleLineText(
                    text = stringResource(
                        R.string.status_bar_range,
                        state.rangeKm?.toString() ?: unknown,
                    ),
                    style = typography.labelSmall,
                    color = colors.textTertiary,
                )
            }
            state.temperatureC?.let {
                SingleLineText(
                    text = stringResource(R.string.status_bar_temperature, it),
                    style = typography.labelSmall,
                    color = colors.textSecondary,
                )
            }
            // TODO 기획 확인: 여기에 구분선 + 연결 아이콘 + 시간 (위 KDoc 참고)
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
