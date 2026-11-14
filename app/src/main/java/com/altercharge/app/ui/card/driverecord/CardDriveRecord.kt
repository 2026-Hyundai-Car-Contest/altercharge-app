/** L2 주간 주행 기록 카드 */
package com.altercharge.app.ui.card.driverecord

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * Figma card_drive_record (366:38474). 펼치면 상세 통계 4개가 붙어 카드가 +207 늘어난다.
 * 부모가 높이를 무한대로 줄 수 있어(가로 왼쪽 열 스크롤) 높이를 채우는 modifier는 쓰지 않는다.
 * 가로에서는 `AlterChargeLayout(leftColumnScrollable = state.expanded, ...)`로 넘긴다.
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
            // card_drive_record/container/background → common/container/background_subtle
            .background(colors.containerBackgroundSubtle)
            .padding(AlterPadding.XSmall),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Medium),
    ) {
        ChartPanel(
            state = state,
            mode = mode,
            chartHeight = if (state.expanded) mode.chartWeekHeight + mode.expandedChartDelta else mode.chartWeekHeight,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek,
        )
        // 시안 메모: wf_landscape는 "타일 숨김"인데 펼침 시안(439:20131)은 타일이 있다. 지금은 펼치면 보여준다
        ContentBlock(state = state, onToggleExpanded = onToggleExpanded)
    }
}

/** chart_panel */
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
            // Figma는 테두리 1이 자리를 차지한다. Compose border는 자리를 안 차지해 따로 띄운다
            .padding(ChartOutlineWidth)
            .padding(AlterPadding.Medium),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        PeriodHeader(state, onPreviousWeek, onNextWeek)
        ChartWeek(state, mode, chartHeight)
    }
}
