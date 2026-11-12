package com.altercharge.app.ui.card.driverecord

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.ShadowLayer

// Figma card_drive_record 변수 (366:38474) 중 디자인 시스템에 없는 것만 둔다.

/** period_nav/size */
internal val CircleButtonSize = 48.dp

/** AAOS 터치 76dp */
private val CircleButtonTouchSize = 76.dp

internal val CircleButtonTouchInset = (CircleButtonTouchSize - CircleButtonSize) / 2

/** 터치 영역은 이 간격 가운데에서 나눈다 */
internal val PeriodNavGap = AlterMargin.XSmall

/** 디자인 시스템 Level1과 값이 달라 카드에 둔다 */
internal val CircleButtonShadow = listOf(
    ShadowLayer(offsetY = 0.dp, blur = 1.dp, color = Color(0x0F12141F)),
    ShadowLayer(offsetY = 3.dp, blur = 6.dp, color = Color(0x1A12141F)),
)

internal val ButtonIconSize = 24.dp

/** bar/width */
internal val BarWidth = 32.dp

/** 0km여도 값 라벨이 막대 안에 들어가도록 주는 최소 높이 */
internal val BarMinHeight = 40.dp

internal val BarValueTopPadding = 8.dp

/** 막대 높이 = (차트 − 32) × 0.85 × (그날 ÷ 최대) */
internal val BarHeightInset = 32.dp
internal const val BAR_HEIGHT_RATIO = 0.85f

internal const val BAR_ALPHA_TOP = 0.03f
internal const val BAR_ALPHA_BOTTOM = 0.7f

internal val HatchSpacing = 10.dp
internal val HatchStroke = 1.5.dp

internal val AverageLineStroke = 1.5.dp
internal val AverageLineDash = 5.87.dp

internal val AverageChipVerticalPadding = 2.dp

/** chart_week rounded-top 24 */
internal val ChartWeekTopRadius = AlterRadius.XXLarge

/**
 * Figma expanded=true − expanded=false의 chart_week 높이.
 * 가로(366:37434) · W-Ext 가로(366:37467) · 세로(366:37500) −23, WF 가로(366:37533) 0, WF 세로(366:37566) +33.
 */
internal val CardDriveRecordMode.expandedChartDelta: Dp
    get() = when (this) {
        CardDriveRecordMode.LANDSCAPE, CardDriveRecordMode.LANDSCAPE_TALL, CardDriveRecordMode.PORTRAIT -> (-23).dp
        CardDriveRecordMode.WF_LANDSCAPE -> 0.dp
        CardDriveRecordMode.WF_PORTRAIT -> 33.dp
    }

internal val TileDividerThickness = 1.dp

internal val ChartOutlineWidth = 1.dp

/**
 * Figma card_drive_record 변수(366:38510 라이트, 375:9932 다크).
 * 라이트 값 일부는 AlterColors와 같지만 다크에서 짝이 다른 토큰이라 카드 변수로 둔다.
 */
internal data class DriveRecordColors(
    val periodRange: Color,
    val track: Color,
    val hatch: Color,
    /** basic_700 */
    val barPast: Color,
    val barValueToday: Color,
)

private val LightDriveRecordColors = DriveRecordColors(
    periodRange = Color(0xFF44464E),
    track = Color(0xFFF7F8FA),
    hatch = Color(0xFFE0E1E6),
    barPast = Color(0xFF313236),
    barValueToday = Color(0xFFFFFFFF),
)

private val DarkDriveRecordColors = DriveRecordColors(
    periodRange = Color(0xFFE0E1E6),
    track = Color(0xFF313236),
    hatch = Color(0xFF313236),
    barPast = Color(0xFFEDEEF2),
    barValueToday = Color(0xFFFFFFFF),
)

/**
 * 접힘 카드 높이(383×648 등)에서 거꾸로 계산한 값.
 * 카드 높이 = 8 + 패널(20 + 1 + 헤더 69 + 16 + 그래프 + 1 + 20) + 20 + 요약 132 + 8
 */
internal val CardDriveRecordMode.chartWeekHeight: Dp
    get() = when (this) {
        CardDriveRecordMode.LANDSCAPE -> 353.dp
        CardDriveRecordMode.LANDSCAPE_TALL -> 521.dp
        CardDriveRecordMode.PORTRAIT -> 281.dp
        // 스펙 메모(387×328)와 인스턴스(387×455)가 달라 인스턴스 기준
        CardDriveRecordMode.WF_LANDSCAPE -> 160.dp
        CardDriveRecordMode.WF_PORTRAIT -> 281.dp
    }

/** ic_chevron_right 188:8. 방향은 회전으로 바꾼다 */
internal val ChevronRight: ImageVector by lazy {
    ImageVector.Builder(
        name = "ChevronRight",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes(
            "M8.37628 5.37628C8.71799 5.03457 9.27285 5.03457 9.61456 5.37628L15.6146 11.3763" +
                "C15.9563 11.718 15.9563 12.2729 15.6146 12.6146L9.61456 18.6146" +
                "C9.27285 18.9563 8.71799 18.9563 8.37628 18.6146C8.03457 18.2729 8.03457 17.718 8.37628 17.3763" +
                "L13.7571 11.9954L8.37628 6.61456C8.03457 6.27285 8.03457 5.71799 8.37628 5.37628Z",
        ),
        fill = SolidColor(Color.Black),
    ).build()
}

internal const val DAYS_IN_WEEK = 7

internal val WeekdayLabels = listOf(
    R.string.card_drive_record_weekday_mon,
    R.string.card_drive_record_weekday_tue,
    R.string.card_drive_record_weekday_wed,
    R.string.card_drive_record_weekday_thu,
    R.string.card_drive_record_weekday_fri,
    R.string.card_drive_record_weekday_sat,
    R.string.card_drive_record_weekday_sun,
)

/** chart_bar_day state */
internal enum class BarState { Past, Peak, Today, Future }

@Composable
internal fun driveRecordColors(): DriveRecordColors =
    if (AlterTheme.colors.isDark) DarkDriveRecordColors else LightDriveRecordColors
