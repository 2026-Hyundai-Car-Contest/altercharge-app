package com.altercharge.app.ui.card.driverecord

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.alterShadow
import java.time.format.DateTimeFormatter

/**
 * L2 주간 주행 기록 카드. Figma card_drive_record (366:38474). 담당: 서희님
 *
 * 모드별로 높이가 정해져 있고, 펼치면 상세 통계 4개가 아래에 붙어 카드가 늘어난다(그래프는 Figma 펼침 인스턴스대로 23 줄어든다, 카드는 +207).
 * 부모가 높이를 무한대로 줄 수 있어서(가로에서 왼쪽 열 스크롤) 높이를 채우는 modifier는 쓰지 않는다.
 *
 * 사용 예:
 * - 가로에서는 펼쳤을 때 왼쪽 열이 스크롤되게 `AlterChargeLayout(leftColumnScrollable = state.expanded, ...)`로 넘긴다.
 * - 펼침 상태는 화면에 들어올 때마다 접힘(expanded = false)으로 시작한다(시안 규칙).
 *
 * @param mode 레이아웃 틀 안에서는 cardDriveRecordModeFor(LocalAppLayout.current)로 고른다.
 */
@Composable
fun CardDriveRecord(
    state: CardDriveRecordUiState,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    mode: CardDriveRecordMode = CardDriveRecordMode.LANDSCAPE,
) {
    val colors = AlterTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AlterRadius.XXLarge))
            // card_drive_record/container/background → common/container/background_subtle (다크 #1D1E21)
            .background(colors.containerBackgroundSubtle)
            .padding(AlterPadding.XSmall),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Medium),
    ) {
        ChartPanel(
            state = state,
            mode = mode,
            // 펼치면 그래프 높이가 모드별로 달라진다 (expandedChartDelta: 가로 · 세로 −23, WF 가로 0, WF 세로 +33)
            chartHeight = if (state.expanded) mode.chartWeekHeight + mode.expandedChartDelta else mode.chartWeekHeight,
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
    mode: CardDriveRecordMode,
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
            // Figma는 테두리 1이 레이아웃에 들어간다(헤더가 패널 안 21, 21에서 시작). Compose border는 자리를 차지하지 않아 따로 띄운다
            .padding(ChartOutlineWidth)
            .padding(AlterPadding.Medium),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        PeriodHeader(state, onPreviousWeek, onNextWeek)
        ChartWeek(state, mode, chartHeight)
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
    val pattern = stringResource(R.string.card_drive_record_period_date_pattern)
    val formatter = remember(pattern, locale) { DateTimeFormatter.ofPattern(pattern, locale) }
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
        // 두 버튼의 터치 영역은 원 사이 간격 가운데에서 맞닿고 겹치지 않는다.
        val halfGap = PeriodNavGap / 2
        Row(Modifier.bleedTouchInset()) {
            CircleIconButton(
                contentDescription = stringResource(R.string.card_drive_record_previous_week),
                iconRotation = 180f,
                enabled = state.hasPreviousWeek && !state.empty,
                onClick = onPreviousWeek,
                touchPadding = PaddingValues(
                    start = CircleButtonTouchInset,
                    top = CircleButtonTouchInset,
                    end = halfGap,
                    bottom = CircleButtonTouchInset,
                ),
            )
            CircleIconButton(
                contentDescription = stringResource(R.string.card_drive_record_next_week),
                iconRotation = 0f,
                enabled = state.hasNextWeek && !state.empty,
                onClick = onNextWeek,
                touchPadding = PaddingValues(
                    start = halfGap,
                    top = CircleButtonTouchInset,
                    end = CircleButtonTouchInset,
                    bottom = CircleButtonTouchInset,
                ),
            )
        }
    }
}

/** 요일 막대 7개 + 평균선 (chart_week) */
@Composable
private fun ChartWeek(state: CardDriveRecordUiState, mode: CardDriveRecordMode, chartHeight: Dp) {
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
        // Figma chart_week: 막대 32 일곱 개를 양 끝에 붙여 고르게 벌린다(justify-between)
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                    description = if (km == null || barState == BarState.Future) {
                        stringResource(WeekdayLabels[index])
                    } else {
                        stringResource(R.string.card_drive_record_bar_description, stringResource(WeekdayLabels[index]), km)
                    },
                    modifier = Modifier.width(BarWidth),
                )
            }
        }
        if (state.empty) {
            ChartEmptyOverlay(mode, Modifier.matchParentSize())
        }
        // 평균선: 지난 요일과 오늘 평균. 막대가 모두 0이면 위치를 정할 수 없어 그리지 않는다
        if (weekMax > 0 && !state.empty) {
            // 평균이 그 주 최대값보다 커도(데이터 오류) 차트 밖으로 나가지 않게 막는다
            val averageY = labelAreaHeight + barMaxHeight * (state.averageKm.toFloat() / weekMax).coerceIn(0f, 1f)
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
    /** 화면 읽기 문구 (예: "월 42km"). 막대 숫자 · 요일 글자 대신 읽는다 */
    description: String,
    modifier: Modifier = Modifier,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val cardColors = driveRecordColors()
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clearAndSetSemantics { contentDescription = description },
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
                    BarState.Peak -> Modifier.background(fadingBrush(colors.textPrimary), barShape)
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
                // 기록 없음에서는 펼칠 내용이 없어 펼침 버튼을 숨긴다 (Figma state = empty)
                if (!state.empty) {
                    CircleIconButton(
                        contentDescription = stringResource(
                            if (state.expanded) R.string.card_drive_record_collapse else R.string.card_drive_record_expand,
                        ),
                        iconRotation = if (state.expanded) -90f else 90f,
                        enabled = true,
                        onClick = onToggleExpanded,
                        iconTint = colors.textSecondary,
                        modifier = Modifier.bleedTouchInset(),
                    )
                }
            }
            if (state.empty) {
                Text(
                    text = stringResource(R.string.card_drive_record_empty_description),
                    style = typography.labelSmall,
                    color = colors.textTertiary,
                )
            } else {
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
        }
        if (state.expanded && !state.empty) {
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
                // Figma 펼침 인스턴스는 마지막 충전도 emphasis=false(28, 타일 높이 97)다
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
            // Figma는 위 구분선 1이 자리를 차지한다(border-top 1 + 여백 16 → 타일 97)
            .padding(top = TileDividerThickness + AlterPadding.Normal),
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
 * 48 원형 버튼. 레이아웃에서 누르는 영역(기본 76 = 원 + 사방 14)을 그대로 차지하고, 보이는 원만 48로 그린다.
 * 시안의 원 자리에 맞추려면 부르는 쪽에서 [bleedTouchInset]으로 터치 여백만큼 바깥으로 번지게 놓는다.
 * 비활성이면 바탕과 그림자 없이 흐린 화살표만 보인다.
 *
 * @param touchPadding 원 바깥 터치 여백. 버튼이 나란히 있으면 맞닿는 쪽을 간격 절반으로 줄여 겹치지 않게 한다.
 */
@Composable
private fun CircleIconButton(
    contentDescription: String,
    iconRotation: Float,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = AlterTheme.colors.textPrimary,
    touchPadding: PaddingValues = PaddingValues(CircleButtonTouchInset),
) {
    val colors = AlterTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clickable(
                interactionSource = interaction,
                // 물결은 원에 그린다 (터치 영역이 한쪽으로 치우쳐도 원 가운데에서 퍼지게)
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription }
            .padding(touchPadding),
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
                .then(circle)
                .indication(interaction, ripple(bounded = false, radius = CircleButtonSize / 2)),
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

/**
 * 버튼(묶음)의 터치 여백만큼 사방으로 작게 보고하고 그만큼 바깥으로 옮겨 그린다.
 * 그래서 원의 화면 위치와 줄 높이는 시안(48 기준) 그대로다.
 * 번진 14는 카드 안쪽 여백(그래프 패널 20, 요약 오른쪽 16 + 카드 8, 요약 위 간격 20) 위에 놓여
 * 잘리지 않고 터치도 받는다. Compose는 부모 영역 밖이라도 잘리지(clip) 않은 자식에 터치를 전달한다.
 */
private fun Modifier.bleedTouchInset(): Modifier = layout { measurable, constraints ->
    val inset = CircleButtonTouchInset.roundToPx()
    val placeable = measurable.measure(constraints.offset(horizontal = inset * 2, vertical = inset * 2))
    val width = (placeable.width - inset * 2).coerceAtLeast(0)
    val height = (placeable.height - inset * 2).coerceAtLeast(0)
    layout(width, height) {
        placeable.place(-inset, -inset)
    }
}
