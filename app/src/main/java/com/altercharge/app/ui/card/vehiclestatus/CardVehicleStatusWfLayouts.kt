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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Headline(
            text = content.headline,
            maxLines = WF_LANDSCAPE_HEADLINE_MAX_LINES,
            style = typography.headlineSmall,
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
            image = { StageVehicle(spec = WfLandscapeStage, vehicleImage = vehicleImage) },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.Large,
                    glassAlpha = FlatStageGlassAlpha,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(BatteryTileGap)) {
                        BatteryValue(
                            state = state,
                            content = content,
                            labelStyle = typography.labelSmall,
                            socStyle = portraitSocStyle(typography.numericHero),
                        )
                        BatteryDetail(
                            content = content,
                            statusStyle = typography.bodyLarge,
                            horizontalAlignment = Alignment.Start,
                        )
                        InfoSummary(content.summary)
                    }
                }
            },
        )
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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Headline(
            text = content.headline,
            maxLines = WF_PORTRAIT_HEADLINE_MAX_LINES,
            style = typography.headlineMedium,
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
            image = { StageVehicle(spec = WfPortraitStage, vehicleImage = vehicleImage) },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.Large,
                    glassAlpha = FlatStageGlassAlpha,
                ) {
                    // Figma 노드 위치상 요약은 battery_row 바로 아래(y 178)에 붙는다 (battery_tile 높이 238 고정)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            BatteryValue(
                                state = state,
                                content = content,
                                labelStyle = typography.labelSmall,
                                socStyle = portraitSocStyle(typography.numericHero),
                            )
                            BatteryDetail(
                                content = content,
                                statusStyle = typography.bodyLarge,
                                horizontalAlignment = Alignment.End,
                            )
                        }
                        InfoSummary(content.summary)
                    }
                }
            },
        )
    }
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
