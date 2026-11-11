/**
 * L2 주간 주행 기록 카드의 공개 진입점(CardDriveRecord)과 그래프 패널 틀(ChartPanel).
 * 그래프는 ChartWeek, 기간 헤더는 PeriodHeader, 요약 · 펼침은 Summary, 원형 버튼은 CircleButton 파일에 있다.
 */
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
