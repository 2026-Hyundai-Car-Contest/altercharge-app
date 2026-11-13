/** C 카드 S-17 가로 · 세로 배치 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterLetterSpacing
import com.altercharge.app.ui.designsystem.theme.AlterLineHeightStyle
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.AstaSans

@Composable
internal fun LandscapeLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
    ) {
        Headline(
            text = content.headline,
            maxLines = LANDSCAPE_HEADLINE_MAX_LINES,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlterPadding.XLarge, vertical = AlterPadding.Large),
        )
        StageWithPanel(
            overlap = LandscapePanelOverlap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            chip = state.chip,
            image = { vehicleImage(content.landscapeImage, Modifier.fillMaxSize()) },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.XLarge,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
                        Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.card_vehicle_status_battery),
                                    style = typography.labelSmall,
                                    color = colors.textSecondary,
                                )
                                BatteryGauge(level = content.gaugeLevel, fill = content.accent)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Soc(
                                    state = state,
                                    content = content,
                                    valueStyle = typography.numericHero,
                                    unitColor = colors.textTertiary,
                                    glow = true,
                                )
                                StatusText(
                                    status = content.status,
                                    numberStyle = typography.numericMedium.copy(color = colors.textPrimary),
                                    unitStyle = typography.labelMedium.copy(color = colors.textTertiary),
                                    plainStyle = typography.labelMedium.copy(color = colors.textTertiary),
                                    modifier = Modifier.alignByBaseline(),
                                )
                            }
                        }
                        // 타일이 하나뿐이면(09) 값은 왼쪽 반에 두고 구분선은 패널 폭 전체에 긋는다 (시안)
                        val singleTile = content.tiles.size == 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (singleTile) Modifier.tileDivider(colors.surfaceAccent) else Modifier),
                            horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large),
                        ) {
                            content.tiles.forEach { tile ->
                                InfoTile(
                                    tile = tile,
                                    valueStyle = landscapeTileValueStyle(typography.numericMedium),
                                    unitStyle = LandscapeTileUnitStyle,
                                    drawDivider = !singleTile,
                                )
                            }
                            if (singleTile) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            },
        )
    }
}

@Composable
internal fun PortraitLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AlterPadding.Large, start = WideSidePadding, end = WideSidePadding),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Q&A Q1
            AutoShrinkHeadline(
                text = content.portraitHeadline,
                styles = portraitHeadlineStyles(),
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
            ) {
                content.portraitTiles.forEach { tile ->
                    InfoTile(
                        tile = tile,
                        valueStyle = typography.numericSmall,
                        unitStyle = typography.labelSmall,
                    )
                }
            }
        }
        StageWithPanel(
            overlap = PortraitPanelOverlap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            chip = state.chip,
            image = {
                // Figma 세로 stage는 variant가 둘이다 (455:58776 세로 01~12)
                StageVehicle(
                    spec = if (content.isCharging) PortraitStageCharging else PortraitStageUnplugged,
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
                            socStyle = portraitSocStyle(typography.numericHero),
                            glow = true,
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
    }
}

/** 세로 충전기 쪽: 원 253, 세로 가운데에서 10.5 위 · 오른쪽 여백 19 (backdrop top = 50% - 10.5). 차 242×153 세로 가운데 */
private val PortraitStageCharging = StageImageSpec(
    backdropSize = 253.dp,
    backdropAlignment = Alignment.CenterEnd,
    backdropOffsetX = (-19).dp,
    backdropOffsetY = (-10.5).dp,
    imageWidth = 242.dp,
    imageHeight = 153.dp,
    imageAlignment = Alignment.CenterEnd,
    imageEndPadding = 24.dp,
)

/** 세로 미연결 · 오프라인 (01 · 02 · 09 · 12): 원 217.6, 세로 가운데 · 오른쪽 밖으로 15.17. 차 139×115(115×139를 -90도) */
private val PortraitStageUnplugged = StageImageSpec(
    backdropSize = 217.6.dp,
    backdropAlignment = Alignment.CenterEnd,
    backdropOffsetX = 15.17.dp,
    imageWidth = 139.dp,
    imageHeight = 115.dp,
    imageAlignment = Alignment.CenterEnd,
    imageEndPadding = 24.dp,
)

/** 18/27은 디자인 값에 없는 크기 */
private val LandscapeTileUnitStyle = TextStyle(
    fontFamily = AstaSans,
    fontWeight = AlterFontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 27.sp,
    letterSpacing = AlterLetterSpacing,
    lineHeightStyle = AlterLineHeightStyle,
)

/** numeric/medium에 줄 높이만 1.2배 (Figma tile_range · tile_*) */
private fun landscapeTileValueStyle(base: TextStyle): TextStyle = base.copy(lineHeight = 48.sp)
