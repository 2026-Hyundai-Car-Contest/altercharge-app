/** C 카드 W-Extended 가로 · 세로 배치. 두 mode 차이는 TallSpec 값으로 준다 */
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
internal fun TallLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
    spec: TallSpec,
) {
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        val headlineModifier = Modifier
            .fillMaxWidth()
            .padding(top = AlterPadding.Large, start = WideSidePadding, end = WideSidePadding)
        if (spec.autoShrinkHeadline) {
            AutoShrinkHeadline(
                text = content.headline,
                styles = portraitHeadlineStyles(),
                modifier = headlineModifier,
            )
        } else {
            Headline(
                text = content.headline,
                maxLines = LANDSCAPE_HEADLINE_MAX_LINES,
                modifier = headlineModifier,
            )
        }
        // TODO 디자이너 확인(변수 연결): landscape_tall 충전 중(366:37686) stage만 배경이
        //   panel_station_list/item/enabled/container(흰색)에 연결돼 있다. 다른 variant는 stage/background(#F7F8FA).
        //   화면에 보이는 값(흰색)을 따랐다.
        val stageBackground = if (content.isCharging && spec.chargingStageOnSurface) {
            AlterTheme.colors.surface
        } else {
            AlterTheme.colors.background
        }
        StageWithPanel(
            overlap = spec.panelOverlap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            chip = state.chip,
            stageBackground = stageBackground,
            image = {
                StageVehicle(
                    spec = if (content.isCharging) spec.stageCharging else spec.stageUnplugged,
                    vehicleImage = vehicleImage,
                )
            },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.Large,
                    glassAlpha = FlatStageGlassAlpha,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        BatteryValue(
                            state = state,
                            content = content,
                            labelStyle = typography.labelMedium,
                            socStyle = spec.socStyle(typography.numericHero),
                        )
                        BatteryDetail(
                            content = content,
                            statusStyle = typography.titleSmall,
                            horizontalAlignment = Alignment.End,
                        )
                    }
                }
            },
        )
        // 타일이 하나면 폭 전체를 쓴다 (unplugged variant의 tile_range 674/640)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WideSidePadding),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
        ) {
            content.tiles.forEach { tile ->
                InfoTile(
                    tile = tile,
                    valueStyle = typography.numericSmall,
                    unitStyle = typography.labelSmall,
                )
            }
        }
    }
}

/** W-Extended 모드별 값. Figma card_vehicle_status mode=landscape_tall (366:37686 · 366:37704) / portrait_tall (366:37763 · 366:37782) */
internal class TallSpec(
    val panelOverlap: Dp,
    /** portrait_tall만 (Q&A Q1) */
    val autoShrinkHeadline: Boolean,
    val chargingStageOnSurface: Boolean,
    val stageCharging: StageImageSpec,
    val stageUnplugged: StageImageSpec,
    val socStyle: (TextStyle) -> TextStyle,
)

/**
 * W-Ext 가로 770×1256: 패널 겹침 56, SOC 112(numericHero 그대로).
 * 충전기 쪽은 원 없이 차 626×441 가운데, 미연결은 원 529.22 가운데 · 차 391×473.
 */
internal val LandscapeTall = TallSpec(
    panelOverlap = LandscapePanelOverlap,
    autoShrinkHeadline = false,
    chargingStageOnSurface = true,
    stageCharging = StageImageSpec(backdropSize = null, imageWidth = 626.dp, imageHeight = 441.dp),
    stageUnplugged = StageImageSpec(backdropSize = 529.22.dp, imageWidth = 391.dp, imageHeight = 473.dp),
    socStyle = { it },
)

/** W-Ext 세로 736×816: 패널 겹침 24, SOC 80. 원 290.7 가운데, 차 282×233(충전기 쪽) · 186×154(미연결), 둘 다 -90도 */
internal val PortraitTall = TallSpec(
    panelOverlap = PortraitPanelOverlap,
    autoShrinkHeadline = true,
    chargingStageOnSurface = false,
    stageCharging = StageImageSpec(backdropSize = 290.7.dp, imageWidth = 282.dp, imageHeight = 233.dp),
    stageUnplugged = StageImageSpec(backdropSize = 290.7.dp, imageWidth = 186.dp, imageHeight = 154.dp),
    socStyle = ::portraitSocStyle,
)
