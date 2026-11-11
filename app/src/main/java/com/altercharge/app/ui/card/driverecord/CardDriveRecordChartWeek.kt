/**
 * L2 그래프 본체(chart_week): 요일 막대 7개(DayBar · 빗금 트랙 · 그라데이션)와 평균선 + 평균 칩.
 * 치수 · 색은 CardDriveRecordSpec.kt, 기록 없음 덮개는 CardDriveRecordEmpty.kt에 있다.
 */
package com.altercharge.app.ui.card.driverecord

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** 요일 막대 7개 + 평균선 (chart_week) */
@Composable
internal fun ChartWeek(state: CardDriveRecordUiState, mode: CardDriveRecordMode, chartHeight: Dp) {
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
