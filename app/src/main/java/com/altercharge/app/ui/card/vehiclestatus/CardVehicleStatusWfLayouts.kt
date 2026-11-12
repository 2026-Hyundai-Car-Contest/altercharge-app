/**
 * WF 가로 · 세로(WF_LANDSCAPE · WF_PORTRAIT) 배치와 그 mode에서만 쓰는 치수.
 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** WF 가로 (폭 387). 배터리 패널 안에 값 / 게이지 · 남은 시간 / 요약 한 줄을 세로로 쌓는다. */
@Composable
internal fun WfLandscapeLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val typography = AlterTheme.typography
    WfLayout(
        state = state,
        content = content,
        headlineStyle = typography.headlineSmall,
        headlineMaxLines = WF_LANDSCAPE_HEADLINE_MAX_LINES,
        stage = WfLandscapeStage,
        vehicleImage = vehicleImage,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(BatteryTileGap)) {
            WfBatteryValue(state, content)
            WfBatteryDetail(content, horizontalAlignment = Alignment.Start)
            InfoSummary(content.summary)
        }
    }
}

/** WF 세로 (584). 배터리 값 · 게이지 줄 아래에 요약 한 줄. */
@Composable
internal fun WfPortraitLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val typography = AlterTheme.typography
    WfLayout(
        state = state,
        content = content,
        headlineStyle = typography.headlineMedium,
        headlineMaxLines = WF_PORTRAIT_HEADLINE_MAX_LINES,
        stage = WfPortraitStage,
        vehicleImage = vehicleImage,
    ) {
        // Figma 노드 위치상 요약은 battery_row 바로 아래(y 178)에 붙는다 (battery_tile 높이 238 고정)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                WfBatteryValue(state, content)
                WfBatteryDetail(content, horizontalAlignment = Alignment.End)
            }
            InfoSummary(content.summary)
        }
    }
}

/**
 * WF 가로 · 세로 공통 뼈대: 위에 헤드라인, 아래 차 무대(stage)에 겹친 유리 패널.
 * 두 mode는 헤드라인 글자 · 줄 수, 차 이미지 자리, 패널 안 배터리 배치([panelContent])만 다르다.
 */
@Composable
private fun WfLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    headlineStyle: TextStyle,
    headlineMaxLines: Int,
    stage: StageImageSpec,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
    panelContent: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Headline(
            text = content.headline,
            maxLines = headlineMaxLines,
            style = headlineStyle,
            lineBreak = PortraitHeadlineLineBreak,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AlterPadding.Large, start = AlterPadding.Large, end = AlterPadding.Large),
        )
        StageWithPanel(
            overlap = PortraitPanelOverlap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            chip = state.chip,
            chipInset = AlterPadding.Normal,
            image = { StageVehicle(spec = stage, vehicleImage = vehicleImage) },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.Large,
                    glassAlpha = FlatStageGlassAlpha,
                ) {
                    panelContent()
                }
            },
        )
    }
}

/** WF 배터리 값: 라벨 label/small, SOC는 세로 카드와 같은 큰 숫자 */
@Composable
private fun WfBatteryValue(state: CardVehicleStatusUiState, content: CardContent) {
    val typography = AlterTheme.typography
    BatteryValue(
        state = state,
        content = content,
        labelStyle = typography.labelSmall,
        socStyle = portraitSocStyle(typography.numericHero),
    )
}

/** WF 게이지 · 남은 시간. 가로는 왼쪽, 세로는 오른쪽 정렬 */
@Composable
private fun WfBatteryDetail(content: CardContent, horizontalAlignment: Alignment.Horizontal) {
    BatteryDetail(
        content = content,
        statusStyle = AlterTheme.typography.bodyLarge,
        horizontalAlignment = horizontalAlignment,
    )
}

/** WF 가로 헤드라인 칸은 높이가 내용을 따른다(시안 2줄). 긴 문구는 3줄까지 */
private const val WF_LANDSCAPE_HEADLINE_MAX_LINES = 3

/** WF 세로 헤드라인 칸은 높이 192 고정(2줄) */
private const val WF_PORTRAIT_HEADLINE_MAX_LINES = 2

/** WF 가로 (366:37801 · 366:37818, 두 variant 같음): 원 226.1 세로 가운데 · 오른쪽 밖으로 8.3, 차 162×134(-90도) 오른쪽 여백 24 */
private val WfLandscapeStage = StageImageSpec(
    backdropSize = 226.1.dp,
    backdropAlignment = Alignment.CenterEnd,
    backdropOffsetX = 8.3.dp,
    imageWidth = 162.dp,
    imageHeight = 134.dp,
    imageAlignment = Alignment.CenterEnd,
    imageEndPadding = 24.dp,
)

/** WF 세로 (366:37835 · 366:37853, 두 variant 같음): 원 265.2 가운데, 차 170×140(-90도) 가운데 */
private val WfPortraitStage = StageImageSpec(backdropSize = 265.2.dp, imageWidth = 170.dp, imageHeight = 140.dp)
