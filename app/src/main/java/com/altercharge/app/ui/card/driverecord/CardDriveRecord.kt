package com.altercharge.app.ui.card.driverecord

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.ShadowLayer
import com.altercharge.app.ui.designsystem.theme.alterShadow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ---------------------------------------------------------------------------------------------
// 카드 고유 값. Figma card_drive_record 변수 (366:38474) 중 디자인 시스템에 없는 것만 둔다.
// ---------------------------------------------------------------------------------------------

/** 기간 이동 · 펼침 버튼 크기 (period_nav/size) */
private val CircleButtonSize = 48.dp

/** 버튼 터치 영역. 보이는 크기는 48, 누르는 영역만 76 (AAOS 터치 76dp) */
private val CircleButtonTouchSize = 76.dp

/** 버튼 그림자 (drop-shadow 0 0 1 · 0 3 6). 디자인 시스템 Level1과 값이 달라 카드에 둔다 */
private val CircleButtonShadow = listOf(
    ShadowLayer(offsetY = 0.dp, blur = 1.dp, color = Color(0x0F12141F)),
    ShadowLayer(offsetY = 3.dp, blur = 6.dp, color = Color(0x1A12141F)),
)

private val ButtonIconSize = 24.dp

/** 막대 너비 (bar/width) */
private val BarWidth = 32.dp

/** 0km여도 값 라벨이 막대 안에 들어가도록 주는 최소 높이 */
private val BarMinHeight = 40.dp

/** 막대 안 값 라벨 위 여백 */
private val BarValueTopPadding = 8.dp

/** 막대 높이 계산에서 차트 높이에서 빼는 값: (차트 − 32) × 0.85 × (그날 ÷ 최대) */
private val BarHeightInset = 32.dp
private const val BAR_HEIGHT_RATIO = 0.85f

/** 지난 요일 막대 그라데이션: 위 3% → 아래 70% */
private const val BAR_ALPHA_TOP = 0.03f
private const val BAR_ALPHA_BOTTOM = 0.7f

/** 빗금 트랙: 10 간격, 1.5 두께, 오른쪽 위로 기우는 45도 */
private val HatchSpacing = 10.dp
private val HatchStroke = 1.5.dp

/** 평균선 점선 (blue_400, 1.5 두께, 약 5.9 간격) */
private val AverageLineStroke = 1.5.dp
private val AverageLineDash = 5.87.dp

/** 평균 칩 세로 여백 */
private val AverageChipVerticalPadding = 2.dp

/** 차트 위쪽 모서리 (chart_week rounded-top 24) */
private val ChartWeekTopRadius = AlterRadius.XXLarge

/** 타일 위 구분선 두께 */
private val TileDividerThickness = 1.dp

/** 패널 테두리 두께 */
private val ChartOutlineWidth = 1.dp

/**
 * 카드 전용 색. 디자인 시스템 AlterColors에 같은 값이 없는 card_drive_record 변수.
 * 라이트/다크 값은 Figma 변수(366:38510 라이트, 375:9932 다크) 그대로다.
 */
private data class DriveRecordColors(
    /** period/range */
    val periodRange: Color,
    /** bar/track */
    val track: Color,
    /** bar/hatch */
    val hatch: Color,
    /** basic_700: 지난 요일 막대 */
    val barPast: Color,
    /** basic_900: 그 주 최대값(peak) 막대 */
    val barPeak: Color,
    /** bar/value_today */
    val barValueToday: Color,
)

private val LightDriveRecordColors = DriveRecordColors(
    periodRange = Color(0xFF44464E),
    track = Color(0xFFF7F8FA),
    hatch = Color(0xFFE0E1E6),
    barPast = Color(0xFF313236),
    barPeak = Color(0xFF131417),
    barValueToday = Color(0xFFFFFFFF),
)

private val DarkDriveRecordColors = DriveRecordColors(
    periodRange = Color(0xFFE0E1E6),
    track = Color(0xFF313236),
    hatch = Color(0xFF313236),
    barPast = Color(0xFFEDEEF2),
    barPeak = Color(0xFFFFFFFF),
    barValueToday = Color(0xFFFFFFFF),
)

/**
 * 모드별 그래프(chart_week) 높이. 접힘 카드 높이(383×648 등)에서 거꾸로 계산한 값이다.
 * 카드 높이 = 8 + 패널(20 + 1 + 헤더 69 + 16 + 그래프 + 1 + 20) + 20 + 요약 132 + 8
 */
private val CardDriveRecordMode.chartWeekHeight: Dp
    get() = when (this) {
        CardDriveRecordMode.Landscape -> 353.dp
        CardDriveRecordMode.LandscapeTall -> 521.dp
        CardDriveRecordMode.Portrait -> 281.dp
        // TODO 디자이너 확인: 스펙 메모는 387×328, 인스턴스는 387×455. 인스턴스 기준으로 맞췄다
        CardDriveRecordMode.WfLandscape -> 160.dp
        CardDriveRecordMode.WfPortrait -> 281.dp
    }

/** 오른쪽 화살표 (ic_chevron_right 188:8). 방향은 회전으로 바꾼다 */
private val ChevronRight: ImageVector by lazy {
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

private const val DAYS_IN_WEEK = 7

private val WeekdayLabels = listOf(
    R.string.card_drive_record_weekday_mon,
    R.string.card_drive_record_weekday_tue,
    R.string.card_drive_record_weekday_wed,
    R.string.card_drive_record_weekday_thu,
    R.string.card_drive_record_weekday_fri,
    R.string.card_drive_record_weekday_sat,
    R.string.card_drive_record_weekday_sun,
)

/** 막대 하루치 상태 (chart_bar_day state) */
private enum class BarState { Past, Peak, Today, Future }

/**
 * L2 주간 주행 기록 카드. Figma card_drive_record (366:38474). 담당: 서희님
 *
 * 모드별로 높이가 정해져 있고, 펼치면 상세 통계 4개가 아래에 붙어 카드가 늘어난다(그래프 높이는 그대로).
 * 부모가 높이를 무한대로 줄 수 있어서(가로에서 왼쪽 열 스크롤) 높이를 채우는 modifier는 쓰지 않는다.
 */
@Composable
fun CardDriveRecord(
    state: CardDriveRecordUiState,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    mode: CardDriveRecordMode = CardDriveRecordMode.Landscape,
) {
    val colors = AlterTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AlterRadius.XXLarge))
            .background(colors.background)
            .padding(AlterPadding.XSmall),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Medium),
    ) {
        ChartPanel(
            state = state,
            chartHeight = mode.chartWeekHeight,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek,
        )
        // TODO 디자이너 확인: wf_landscape는 "타일 숨김"인데 펼침 시안(439:20131)은 타일이 있다. 지금은 펼치면 보여준다
        ContentBlock(state = state, onToggleExpanded = onToggleExpanded)
    }
}

// ---------------------------------------------------------------------------------------------
// 그래프 패널 (chart_panel)
// ---------------------------------------------------------------------------------------------

@Composable
private fun ChartPanel(
    state: CardDriveRecordUiState,
    chartHeight: Dp,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    val colors = AlterTheme.colors
    val shape = RoundedCornerShape(AlterRadius.XLarge)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(ChartOutlineWidth, colors.surfaceHigh, shape)
            .padding(AlterPadding.Medium),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        PeriodHeader(state, onPreviousWeek, onNextWeek)
        ChartWeek(state, chartHeight)
    }
}

@Composable
private fun PeriodHeader(
    state: CardDriveRecordUiState,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val cardColors = driveRecordColors()
    val locale = LocalConfiguration.current.locales[0]
    val formatter = DateTimeFormatter.ofPattern(
        stringResource(R.string.card_drive_record_period_date_pattern),
        locale,
    )
    val range = stringResource(
        R.string.card_drive_record_period_range,
        state.weekStart.format(formatter),
        state.weekStart.plusDays((DAYS_IN_WEEK - 1).toLong()).format(formatter),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            // TODO 디자이너 확인: 지난 주를 볼 때 기간 라벨("지난 주" 등) 시안이 없다. 지금은 날짜만 보인다
            if (state.todayIndex != null) {
                Text(
                    text = stringResource(R.string.card_drive_record_period_this_week),
                    style = typography.labelSmall,
                    color = colors.textTertiary,
                    maxLines = 1,
                )
            }
            Text(
                text = range,
                style = typography.bodySmall,
                color = cardColors.periodRange,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall)) {
            CircleIconButton(
                contentDescription = stringResource(R.string.card_drive_record_previous_week),
                iconRotation = 180f,
                enabled = state.hasPreviousWeek,
                onClick = onPreviousWeek,
            )
            CircleIconButton(
                contentDescription = stringResource(R.string.card_drive_record_next_week),
                iconRotation = 0f,
                enabled = state.hasNextWeek,
                onClick = onNextWeek,
            )
        }
    }
}

/** 요일 막대 7개 + 평균선 (chart_week) */
@Composable
private fun ChartWeek(state: CardDriveRecordUiState, chartHeight: Dp) {
    val typography = AlterTheme.typography
    val density = LocalDensity.current
    // 요일 라벨 줄 높이 + 간격. 막대 바닥선과 평균선 위치를 여기에 맞춘다
    val labelAreaHeight = with(density) { typography.labelSmall.lineHeight.toDp() } + AlterMargin.XSmall
    val barMaxHeight = (chartHeight - BarHeightInset) * BAR_HEIGHT_RATIO

    val days = (0 until DAYS_IN_WEEK).map { state.dailyKm.getOrNull(it) }
    val todayIndex = state.todayIndex
    val states = days.mapIndexed { index, km ->
        when {
            todayIndex != null && index == todayIndex -> BarState.Today
            km == null || (todayIndex != null && index > todayIndex) -> BarState.Future
            else -> BarState.Past
        }
    }
    val weekMax = days.filterIndexed { index, _ -> states[index] != BarState.Future }
        .maxOfOrNull { it ?: 0 } ?: 0
    // 최대값 요일(오늘 제외) 하나만 조금 더 진하게. 오늘이 최대면 오늘 파랑을 그대로 둔다
    val peakIndex = if (weekMax > 0) {
        days.indices.firstOrNull { states[it] == BarState.Past && days[it] == weekMax }
    } else {
        null
    }

    fun barHeight(km: Int): Dp {
        if (weekMax <= 0) return BarMinHeight
        val height = barMaxHeight * (km.toFloat() / weekMax)
        return if (height < BarMinHeight) BarMinHeight else height
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight)
            .clip(RoundedCornerShape(topStart = ChartWeekTopRadius, topEnd = ChartWeekTopRadius)),
    ) {
        Row(Modifier.fillMaxSize()) {
            days.forEachIndexed { index, km ->
                val barState = if (index == peakIndex) BarState.Peak else states[index]
                DayBar(
                    km = km,
                    state = barState,
                    barHeight = if (km == null || barState == BarState.Future) 0.dp else barHeight(km),
                    label = if (barState == BarState.Today) {
                        stringResource(R.string.card_drive_record_weekday_today)
                    } else {
                        stringResource(WeekdayLabels[index])
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        // 평균선: 지난 요일과 오늘 평균. 막대가 모두 0이면 위치를 정할 수 없어 그리지 않는다
        if (weekMax > 0) {
            val averageY = labelAreaHeight + barMaxHeight * (state.averageKm.toFloat() / weekMax)
            AverageLine(
                averageKm = state.averageKm,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .layout { measurable, constraints ->
                        // 줄 가운데가 평균 높이에 오도록 놓는다
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.place(0, placeable.height / 2 - averageY.roundToPx())
                        }
                    },
            )
        }
    }
}

/** 하루치 막대 (chart_bar_day). 뒤에 빗금 트랙, 위에 막대, 아래 요일 라벨 */
@Composable
private fun DayBar(
    km: Int?,
    state: BarState,
    barHeight: Dp,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val cardColors = driveRecordColors()
    Column(
        modifier = modifier
            .fillMaxHeight()
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .width(BarWidth),
        ) {
            HatchTrack(Modifier.fillMaxSize())
            if (state != BarState.Future && km != null) {
                val barShape = RoundedCornerShape(AlterRadius.Medium)
                val barBackground = when (state) {
                    BarState.Today -> Modifier.background(colors.blue500, barShape)
                    BarState.Peak -> Modifier.background(fadingBrush(cardColors.barPeak), barShape)
                    else -> Modifier.background(fadingBrush(cardColors.barPast), barShape)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(barHeight)
                        .then(barBackground)
                        .padding(top = BarValueTopPadding),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        text = km.toString(),
                        style = typography.labelXSmall,
                        color = if (state == BarState.Today) cardColors.barValueToday else colors.textSecondary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.wrapContentWidth(unbounded = true),
                    )
                }
            }
        }
        val labelStyle: TextStyle
        val labelColor: Color
        when (state) {
            BarState.Today -> {
                labelStyle = typography.labelSmall.copy(fontWeight = AlterFontWeight.Strong)
                labelColor = colors.textPrimary
            }
            BarState.Future -> {
                labelStyle = typography.labelSmall
                labelColor = colors.textDisabled
            }
            else -> {
                labelStyle = typography.labelSmall
                labelColor = colors.textTertiary
            }
        }
        Text(
            text = label,
            style = labelStyle,
            color = labelColor,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.wrapContentWidth(unbounded = true),
        )
    }
}

/** 지난 요일 막대: 위로 갈수록 거의 투명해지는 세로 그라데이션 */
private fun fadingBrush(color: Color): Brush = Brush.verticalGradient(
    listOf(color.copy(alpha = BAR_ALPHA_TOP), color.copy(alpha = BAR_ALPHA_BOTTOM)),
)

/** 빗금 트랙. 위는 차트 밖으로 이어져 잘리므로 아래 모서리만 둥글다 */
@Composable
private fun HatchTrack(modifier: Modifier = Modifier) {
    val cardColors = driveRecordColors()
    val shape = RoundedCornerShape(bottomStart = AlterRadius.Medium, bottomEnd = AlterRadius.Medium)
    Canvas(modifier.clip(shape)) {
        drawRect(cardColors.track)
        val spacing = HatchSpacing.toPx()
        val stroke = HatchStroke.toPx()
        val w = size.width
        var y = -w
        while (y < size.height + w) {
            drawLine(
                color = cardColors.hatch,
                start = Offset(0f, y + w),
                end = Offset(w, y),
                strokeWidth = stroke,
            )
            y += spacing
        }
    }
}

/** 평균선 (blue_400 점선) + 오른쪽 끝 "평균 NN km" 칩 */
@Composable
private fun AverageLine(averageKm: Int, modifier: Modifier = Modifier) {
    val colors = AlterTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(
            Modifier
                .weight(1f)
                .height(AverageLineStroke),
        ) {
            val dash = AverageLineDash.toPx()
            drawLine(
                color = colors.blue400,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = AverageLineStroke.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
            )
        }
        Text(
            text = stringResource(R.string.card_drive_record_average_chip, averageKm),
            style = AlterTheme.typography.labelXSmall,
            color = colors.blue700,
            maxLines = 1,
            modifier = Modifier
                .background(colors.blue50, RoundedCornerShape(AlterRadius.Full))
                .padding(horizontal = AlterPadding.Small, vertical = AverageChipVerticalPadding),
        )
    }
}

// ---------------------------------------------------------------------------------------------
// 요약 + 상세 통계 (summary_block / content_block)
// ---------------------------------------------------------------------------------------------

@Composable
private fun ContentBlock(state: CardDriveRecordUiState, onToggleExpanded: () -> Unit) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AlterPadding.Normal, end = AlterPadding.Normal, bottom = AlterPadding.Normal),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Medium),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AlterMargin.Small),
            ) {
                // TODO 디자이너 확인: 지난 주를 볼 때 제목 시안이 없다. 지금은 "이번 주 주행 기록" 그대로
                Text(
                    text = stringResource(R.string.card_drive_record_title),
                    style = typography.titleSmall,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                CircleIconButton(
                    contentDescription = stringResource(
                        if (state.expanded) R.string.card_drive_record_collapse else R.string.card_drive_record_expand,
                    ),
                    iconRotation = if (state.expanded) -90f else 90f,
                    enabled = true,
                    onClick = onToggleExpanded,
                    iconTint = colors.textSecondary,
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.card_drive_record_summary_average, state.averageKm),
                    style = typography.labelSmall,
                    color = colors.textTertiary,
                )
                // TODO 디자이너 확인: 지난주보다 덜 달렸을 때 · 같을 때 · 비교할 주가 없을 때 문구가 시안에 없다.
                // 지금은 더 달렸을 때만 두 번째 줄을 보여준다.
                val diff = state.diffFromLastWeekKm
                if (diff != null && diff > 0) {
                    Text(
                        text = stringResource(R.string.card_drive_record_summary_diff_more, diff),
                        style = typography.labelSmall,
                        color = colors.textTertiary,
                    )
                }
            }
        }
        if (state.expanded) {
            StatTiles(state)
        }
    }
}

/** 상세 통계 4개 (details). 2열 2줄 */
@Composable
private fun StatTiles(state: CardDriveRecordUiState) {
    val unknown = stringResource(R.string.card_drive_record_value_unknown)
    val km = stringResource(R.string.card_drive_record_unit_km)
    Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
            TileStat(
                label = stringResource(R.string.card_drive_record_tile_daily_average),
                value = state.averageKm.toString(),
                unit = km,
                modifier = Modifier.weight(1f),
            )
            TileStat(
                label = stringResource(R.string.card_drive_record_tile_weekly_total),
                value = state.totalKm.toString(),
                unit = km,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
            val days = state.daysSinceLastCharge
            TileStat(
                label = stringResource(R.string.card_drive_record_tile_last_charge),
                value = days?.toString() ?: unknown,
                unit = if (days != null) {
                    pluralStringResource(R.plurals.card_drive_record_unit_days_ago, days)
                } else {
                    null
                },
                emphasis = true,
                modifier = Modifier.weight(1f),
            )
            val sinceCharge = state.kmSinceLastCharge
            TileStat(
                label = stringResource(R.string.card_drive_record_tile_since_charge),
                value = sinceCharge?.toString() ?: unknown,
                unit = if (sinceCharge != null) km else null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** 숫자 타일 (tile_stat). 박스 없이 위 구분선. emphasis는 값만 한 단계 크게 */
@Composable
private fun TileStat(
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    emphasis: Boolean = false,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val divider = colors.surfaceAccent
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .drawBehind {
                drawRect(divider, size = Size(size.width, TileDividerThickness.toPx()))
            }
            .padding(top = AlterPadding.Normal),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
    ) {
        Text(text = label, style = typography.labelSmall, color = colors.textTertiary, maxLines = 1)
        Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.XXSmall)) {
            Text(
                text = value,
                style = if (emphasis) typography.numericMedium else typography.numericSmall,
                color = colors.textPrimary,
                maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
            if (unit != null) {
                Text(
                    text = unit,
                    style = typography.labelSmall,
                    color = colors.textTertiary,
                    maxLines = 1,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 원형 아이콘 버튼 (button_period_nav · button_expand)
// ---------------------------------------------------------------------------------------------

/**
 * 48 원형 버튼. 누르는 영역은 76이지만 레이아웃에서는 48만 차지한다(hit-slop).
 * 비활성이면 바탕과 그림자 없이 흐린 화살표만 보인다.
 */
@Composable
private fun CircleIconButton(
    contentDescription: String,
    iconRotation: Float,
    enabled: Boolean,
    onClick: () -> Unit,
    iconTint: Color = AlterTheme.colors.textPrimary,
) {
    val colors = AlterTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(CircleButtonSize)
            .layout { measurable, _ ->
                // 76 크기로 재고 48 자리에 가운데 맞춰 놓는다. 넘친 부분도 터치를 받는다
                val touch = CircleButtonTouchSize.roundToPx()
                val visible = CircleButtonSize.roundToPx()
                val placeable = measurable.measure(Constraints.fixed(touch, touch))
                layout(visible, visible) {
                    placeable.place((visible - touch) / 2, (visible - touch) / 2)
                }
            }
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = false, radius = CircleButtonSize / 2),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        val circle = if (enabled) {
            Modifier
                .alterShadow(CircleButtonShadow, CircleButtonSize / 2)
                .background(colors.surface, CircleShape)
        } else {
            Modifier
        }
        Box(
            modifier = Modifier
                .size(CircleButtonSize)
                .then(circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ChevronRight,
                contentDescription = null,
                tint = if (enabled) iconTint else colors.textDisabled,
                modifier = Modifier
                    .size(ButtonIconSize)
                    .rotate(iconRotation),
            )
        }
    }
}

@Composable
private fun driveRecordColors(): DriveRecordColors =
    if (AlterTheme.colors.isDark) DarkDriveRecordColors else LightDriveRecordColors

// ---------------------------------------------------------------------------------------------
// 미리보기: 접힘/펼침 × 라이트/다크 × 가로/세로, 이번 주(오늘 있음) · 지난 주(오늘 없음)
// ---------------------------------------------------------------------------------------------

private val PreviewThisWeek = CardDriveRecordUiState(
    weekStart = LocalDate.of(2026, 11, 2),
    dailyKm = listOf(42, 36, 55, 31, 18, null, null),
    todayIndex = 4,
    averageKm = 37,
    totalKm = 182,
    daysSinceLastCharge = 3,
    kmSinceLastCharge = 120,
    diffFromLastWeekKm = 12,
    hasPreviousWeek = true,
    hasNextWeek = false,
    expanded = false,
)

private val PreviewLastWeek = CardDriveRecordUiState(
    weekStart = LocalDate.of(2026, 10, 26),
    dailyKm = listOf(28, 0, 47, 33, 52, 64, 12),
    todayIndex = null,
    averageKm = 34,
    totalKm = 236,
    daysSinceLastCharge = 3,
    kmSinceLastCharge = 120,
    diffFromLastWeekKm = -5,
    hasPreviousWeek = true,
    hasNextWeek = true,
    expanded = false,
)

@Composable
private fun PreviewCard(
    state: CardDriveRecordUiState,
    mode: CardDriveRecordMode,
    darkTheme: Boolean,
) {
    AlterChargeTheme(darkTheme = darkTheme) {
        // 미리보기 인터랙티브 모드에서 펼침 버튼을 눌러볼 수 있게 상태를 둔다
        var expanded by remember { mutableStateOf(state.expanded) }
        Box(
            Modifier
                .background(AlterTheme.colors.background)
                .padding(AlterPadding.XSmall),
        ) {
            CardDriveRecord(
                state = state.copy(expanded = expanded),
                onPreviousWeek = {},
                onNextWeek = {},
                onToggleExpanded = { expanded = !expanded },
                mode = mode,
            )
        }
    }
}

@Preview(name = "가로 · 접힘 · 라이트", widthDp = 399)
@Composable
private fun LandscapeCollapsedLightPreview() =
    PreviewCard(PreviewThisWeek, CardDriveRecordMode.Landscape, darkTheme = false)

@Preview(name = "가로 · 접힘 · 다크", widthDp = 399)
@Composable
private fun LandscapeCollapsedDarkPreview() =
    PreviewCard(PreviewThisWeek, CardDriveRecordMode.Landscape, darkTheme = true)

@Preview(name = "가로 · 펼침 · 라이트", widthDp = 399)
@Composable
private fun LandscapeExpandedLightPreview() =
    PreviewCard(PreviewThisWeek.copy(expanded = true), CardDriveRecordMode.Landscape, darkTheme = false)

@Preview(name = "가로 · 펼침 · 다크", widthDp = 399)
@Composable
private fun LandscapeExpandedDarkPreview() =
    PreviewCard(PreviewThisWeek.copy(expanded = true), CardDriveRecordMode.Landscape, darkTheme = true)

@Preview(name = "세로 · 접힘 · 라이트", widthDp = 394)
@Composable
private fun PortraitCollapsedLightPreview() =
    PreviewCard(PreviewThisWeek, CardDriveRecordMode.Portrait, darkTheme = false)

@Preview(name = "세로 · 접힘 · 다크", widthDp = 394)
@Composable
private fun PortraitCollapsedDarkPreview() =
    PreviewCard(PreviewThisWeek, CardDriveRecordMode.Portrait, darkTheme = true)

@Preview(name = "세로 · 펼침 · 라이트", widthDp = 394)
@Composable
private fun PortraitExpandedLightPreview() =
    PreviewCard(PreviewThisWeek.copy(expanded = true), CardDriveRecordMode.Portrait, darkTheme = false)

@Preview(name = "세로 · 펼침 · 다크", widthDp = 394)
@Composable
private fun PortraitExpandedDarkPreview() =
    PreviewCard(PreviewThisWeek.copy(expanded = true), CardDriveRecordMode.Portrait, darkTheme = true)

@Preview(name = "지난 주 · 가로 · 라이트", widthDp = 399)
@Composable
private fun LastWeekLandscapeLightPreview() =
    PreviewCard(PreviewLastWeek, CardDriveRecordMode.Landscape, darkTheme = false)

@Preview(name = "지난 주 · 세로 · 펼침 · 다크", widthDp = 394)
@Composable
private fun LastWeekPortraitExpandedDarkPreview() =
    PreviewCard(PreviewLastWeek.copy(expanded = true), CardDriveRecordMode.Portrait, darkTheme = true)

@Preview(name = "영문 · 세로 · 펼침 · 라이트", widthDp = 394, locale = "en")
@Composable
private fun EnglishPortraitExpandedPreview() =
    PreviewCard(PreviewThisWeek.copy(expanded = true), CardDriveRecordMode.Portrait, darkTheme = false)

@Preview(name = "W-Ext 가로 · 접힘 · 라이트", widthDp = 391)
@Composable
private fun LandscapeTallPreview() =
    PreviewCard(PreviewThisWeek, CardDriveRecordMode.LandscapeTall, darkTheme = false)

@Preview(name = "WF 가로 · 접힘 · 다크", widthDp = 403)
@Composable
private fun WfLandscapePreview() =
    PreviewCard(PreviewThisWeek, CardDriveRecordMode.WfLandscape, darkTheme = true)

@Preview(name = "WF 세로 · 펼침 · 라이트", widthDp = 596)
@Composable
private fun WfPortraitPreview() =
    PreviewCard(PreviewThisWeek.copy(expanded = true), CardDriveRecordMode.WfPortrait, darkTheme = false)
