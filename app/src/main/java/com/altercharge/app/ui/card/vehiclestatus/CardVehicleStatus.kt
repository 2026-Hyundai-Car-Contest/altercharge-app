package com.altercharge.app.ui.card.vehiclestatus

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterColors
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterLetterSpacing
import com.altercharge.app.ui.designsystem.theme.AlterLineHeightStyle
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterShadows
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.AstaSans
import com.altercharge.app.ui.designsystem.theme.ShadowLayer
import com.altercharge.app.ui.designsystem.theme.alterShadow
import java.util.Locale
import kotlin.math.ceil

/**
 * 카드 배치. Figma card_vehicle_status 속성 mode.
 * - LANDSCAPE: S-17 가로 790×1040 (세로로 긴 카드)
 * - PORTRAIT: S-17 세로 776×652 (가로로 넓은 카드)
 *
 * TODO 서희님: landscape_tall(W-Ext 가로) · portrait_tall(W-Ext 세로) · wf_landscape · wf_portrait 모드.
 */
enum class CardVehicleStatusMode { LANDSCAPE, PORTRAIT }

/**
 * 레이아웃 정보로 mode를 고른다. 지금 구현은 S-17 가로 · 세로 두 모드뿐이라 방향만 본다.
 * TODO 디자이너 확인: W-Standard · W-Extended · WF는 구현한 mode가 없어 같은 방향의 S-17 모드로 대신한다.
 *   W-Ext(landscape_tall · portrait_tall)와 WF(wf_landscape · wf_portrait) 모드를 만들면 여기서 나눈다.
 */
fun cardVehicleStatusModeFor(info: AppLayoutInfo): CardVehicleStatusMode =
    if (info.isLandscape) CardVehicleStatusMode.LANDSCAPE else CardVehicleStatusMode.PORTRAIT

/** 차량 이미지 종류. 상황에 따라 카드가 고른다. */
enum class VehicleImageKind {
    /** 가로 카드 · 충전기에 꽂힌 측면 (vehicle_charging) */
    CHARGING,

    /** 가로 카드 · 도로 위 측면 (vehicle_driving) */
    DRIVING,

    /** 세로 카드 · 위에서 본 차 (vehicle_top_view) */
    TOP_VIEW,
}

/**
 * C 차량 상태 카드. Figma card_vehicle_status (366:39011), 상황별 시안 (455:58776 01~12).
 * 보기 전용 카드다(눌러서 하는 동작이 없다). 화면 읽기에서는 카드 전체를 한 덩어리로 읽는다.
 *
 * @param mode 레이아웃 틀 안에서는 cardVehicleStatusModeFor(LocalAppLayout.current)로 고른다.
 * @param vehicleImage 차량 이미지 자리. 이미지 에셋(이슈 #4)이 들어오면 종류별 이미지를 그려 넘긴다.
 *   넘긴 Modifier에는 자리 크기가 들어 있다.
 */
@Composable
fun CardVehicleStatus(
    state: CardVehicleStatusUiState,
    modifier: Modifier = Modifier,
    mode: CardVehicleStatusMode = CardVehicleStatusMode.LANDSCAPE,
    vehicleImage: @Composable (kind: VehicleImageKind, modifier: Modifier) -> Unit = { _, imageModifier ->
        VehicleImagePlaceholder(imageModifier)
    },
) {
    val colors = AlterTheme.colors
    val content = buildContent(state, mode)
    val shape = RoundedCornerShape(CardRadius)
    // 다크 모드는 그림자 대신 면 색으로 높이를 나타낸다 (AlterShadows 설명).
    // 세로 시안은 shadow/level_1 한 겹뿐이다(가로만 0 24 64 한 겹이 더 있다)
    val shadow = if (mode == CardVehicleStatusMode.PORTRAIT) AlterShadows.Level1 else CardShadow
    val shadowModifier = if (colors.isDark) Modifier else Modifier.alterShadow(shadow, CardRadius)

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .then(shadowModifier)
            .clip(shape)
            .background(colors.surface, shape)
            .padding(AlterMargin.XSmall),
    ) {
        when (mode) {
            CardVehicleStatusMode.LANDSCAPE -> LandscapeLayout(state, content, vehicleImage)
            CardVehicleStatusMode.PORTRAIT -> PortraitLayout(state, content, vehicleImage)
        }
    }
}

// ---------------------------------------------------------------------------
// 배치
// ---------------------------------------------------------------------------

@Composable
private fun LandscapeLayout(
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
            stage = {
                Stage(chip = state.chip) {
                    vehicleImage(content.landscapeImage, Modifier.fillMaxSize())
                }
            },
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
                        // 타일이 하나뿐이면(09 배터리 위험 · 미연결) 값은 왼쪽 반에 두고 구분선은 패널 폭 전체에 긋는다 (시안).
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
private fun PortraitLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AlterPadding.Large, start = PortraitHeadlinePadding, end = PortraitHeadlinePadding),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TODO 디자이너 확인: 칸이 카드 폭 절반이라 긴 이름 · 영문(07 등)은 줄이 많아진다.
            //   임시로 단어 단위로 줄을 바꾸고 3줄에서 말줄임한다. 긴 이름 · 영문 기준 시안이 나오면 맞춘다.
            Headline(
                text = content.portraitHeadline,
                maxLines = PORTRAIT_HEADLINE_MAX_LINES,
                lineBreak = PortraitHeadlineLineBreak,
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
            stage = {
                Stage(chip = state.chip) {
                    // Figma 세로 stage는 variant가 둘이다(상황 보드 455:58776 세로 01~12).
                    // 둘 다 패널이 겹친 부분까지 포함한 스테이지 기준이고, 차 자리는 세로 가운데 · 오른쪽 여백 24.
                    val stageSpec = if (content.landscapeImage == VehicleImageKind.CHARGING) {
                        PortraitStageCharging
                    } else {
                        PortraitStageUnplugged
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Box(
                            modifier = Modifier
                                .align(stageSpec.backdropAlignment)
                                .offset(x = stageSpec.backdropOffsetX, y = stageSpec.backdropOffsetY)
                                .size(stageSpec.backdropSize)
                                .background(colors.surfaceHigh, CircleShape),
                        )
                        vehicleImage(
                            VehicleImageKind.TOP_VIEW,
                            Modifier
                                .padding(end = AlterPadding.Large)
                                .size(width = stageSpec.imageWidth, height = stageSpec.imageHeight),
                        )
                    }
                }
            },
            panel = {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AlterPadding.Large,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.XXSmall)) {
                            Text(
                                text = stringResource(R.string.card_vehicle_status_battery),
                                style = typography.labelMedium,
                                color = colors.textSecondary,
                            )
                            Row {
                                Soc(
                                    state = state,
                                    content = content,
                                    valueStyle = portraitSocStyle(typography.numericHero),
                                    unitColor = portraitSocUnitColor(colors),
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(PortraitBatteryDetailGap),
                        ) {
                            // 세로 시안은 gauge/outline · gauge/cell_empty(#EDEEF2) 변수를 쓴다: 테두리가 한 단계 연하고 빈 칸에 2 테두리
                            BatteryGauge(
                                level = content.gaugeLevel,
                                fill = content.accent,
                                outline = colors.surfaceHigh,
                                emptyCellBorder = colors.surfaceHigh,
                            )
                            StatusText(
                                status = content.status,
                                numberStyle = typography.titleSmall.copy(
                                    fontWeight = AlterFontWeight.Normal,
                                    color = colors.textPrimary,
                                ),
                                unitStyle = typography.titleSmall.copy(
                                    fontWeight = AlterFontWeight.Normal,
                                    color = colors.textTertiary,
                                ),
                                plainStyle = typography.labelMedium.copy(color = colors.textTertiary),
                            )
                        }
                    }
                }
            },
        )
    }
}

/** 스테이지 아래쪽에 상태 패널을 겹쳐 올린다. 패널 높이를 먼저 재고 나머지를 스테이지에 준다. */
@Composable
private fun StageWithPanel(
    overlap: Dp,
    modifier: Modifier,
    stage: @Composable () -> Unit,
    panel: @Composable () -> Unit,
) {
    Layout(contents = listOf(stage, panel), modifier = modifier) { measurables, constraints ->
        val (stageMeasurables, panelMeasurables) = measurables
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val panelPlaceable = panelMeasurables.first()
            .measure(Constraints(minWidth = width, maxWidth = width, maxHeight = height))
        val stageHeight = (height - panelPlaceable.height + overlap.roundToPx()).coerceIn(0, height)
        val stagePlaceable = stageMeasurables.first().measure(Constraints.fixed(width, stageHeight))
        layout(width, height) {
            stagePlaceable.place(0, 0)
            panelPlaceable.place(0, height - panelPlaceable.height)
        }
    }
}

// ---------------------------------------------------------------------------
// 조각
// ---------------------------------------------------------------------------

@Composable
private fun Headline(
    text: String,
    maxLines: Int,
    modifier: Modifier = Modifier,
    lineBreak: LineBreak = LineBreak.Unspecified,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AlterTheme.typography.headlineLarge.copy(fontWeight = AlterFontWeight.Strong, lineBreak = lineBreak),
        color = AlterTheme.colors.textPrimary,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** 차량 이미지가 놓이는 면. 왼쪽 위에 연결 칩이 뜬다. */
@Composable
private fun Stage(chip: ConnectionChip, image: @Composable () -> Unit) {
    val shape = RoundedCornerShape(AlterRadius.XXLarge)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(AlterTheme.colors.background, shape),
    ) {
        image()
        ChipStatus(
            chip = chip,
            modifier = Modifier.padding(start = AlterPadding.Large, top = AlterPadding.Large),
        )
    }
}

/** 연결 칩 (chip_status). 바깥 고리 + 안쪽 알약. */
@Composable
private fun ChipStatus(chip: ConnectionChip, modifier: Modifier = Modifier) {
    val colors = AlterTheme.colors
    val (labelRes, dotColor) = when (chip) {
        ConnectionChip.CONNECTED -> R.string.card_vehicle_status_chip_connected to colors.blue500
        ConnectionChip.UNPLUGGED -> R.string.card_vehicle_status_chip_unplugged to colors.negative
        ConnectionChip.ERROR -> R.string.card_vehicle_status_chip_error to colors.negative
        // TODO 서희님: Figma 점 색은 basic_300(회색)인데 디자인 값에 없어 textDisabled로 대신한다.
        ConnectionChip.STANDBY -> R.string.card_vehicle_status_chip_standby to colors.textDisabled
        ConnectionChip.OFFLINE -> R.string.card_vehicle_status_chip_offline to colors.textDisabled
    }
    val pill = RoundedCornerShape(AlterRadius.Full)
    Box(
        modifier = modifier
            .background(colors.surfaceHigh, pill)
            .padding(ChipRingPadding),
    ) {
        Row(
            modifier = Modifier
                .background(colors.surface, pill)
                .padding(horizontal = AlterPadding.Normal, vertical = AlterPadding.XSmall),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(ChipDotSize)
                    .background(dotColor, CircleShape),
            )
            Text(
                text = stringResource(labelRes),
                style = AlterTheme.typography.labelMedium,
                color = colors.textPrimary,
                maxLines = 1,
            )
        }
    }
}

/** 상태 패널의 유리 면 (glass + glass_edge). */
@Composable
private fun GlassPanel(
    modifier: Modifier,
    contentPadding: Dp,
    content: @Composable () -> Unit,
) {
    val colors = AlterTheme.colors
    val shape = RoundedCornerShape(AlterRadius.XXLarge)
    // TODO 서희님: Figma는 뒤를 흐리게(backdrop blur 20) 하고 그림자(0 6 12, 6%)를 준다. Compose 기본으로는 뒤 흐림이 안 돼 면 색만 쓴다.
    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceHigh.copy(alpha = GlassAlpha), shape)
            .border(GlassEdgeWidth, colors.surface.copy(alpha = GlassEdgeAlpha), shape)
            .padding(contentPadding),
    ) {
        content()
    }
}

/** 배터리 % 큰 숫자와 단위. 바깥 Row의 글자 기준선에 맞춘다. */
@Composable
private fun RowScope.Soc(
    state: CardVehicleStatusUiState,
    content: CardContent,
    valueStyle: TextStyle,
    unitColor: Color,
) {
    val typography = AlterTheme.typography
    val known = state.socPercent != null
    Row(
        modifier = Modifier.alignByBaseline(),
        horizontalArrangement = Arrangement.spacedBy(SocGap),
    ) {
        Text(
            // 센서 값이 범위를 벗어나도 0~100으로 보여 준다.
            text = state.socPercent?.coerceIn(0, MAX_SOC)?.toString()
                ?: stringResource(R.string.card_vehicle_status_soc_unknown),
            modifier = Modifier.alignByBaseline(),
            style = if (known) {
                valueStyle.copy(shadow = Shadow(content.accent.copy(alpha = SocGlowAlpha), Offset.Zero, SocGlowBlur))
            } else {
                valueStyle
            },
            color = content.accent,
            maxLines = 1,
        )
        Text(
            text = stringResource(R.string.card_vehicle_status_percent_unit),
            modifier = Modifier.alignByBaseline(),
            style = typography.numericMedium,
            color = unitColor,
        )
    }
}

/** 배터리 게이지 (battery_gauge). level = 채운 칸 수 0~4. */
@Composable
private fun BatteryGauge(
    level: Int,
    fill: Color,
    outline: Color = AlterTheme.colors.surfaceAccent,
    emptyCellBorder: Color? = null,
) {
    val colors = AlterTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .border(GaugeOutlineWidth, outline, RoundedCornerShape(AlterRadius.Large))
                // Figma는 테두리 2가 자리를 차지한다(2 + 여백 8 → 159×60). Compose border는 자리를 안 차지해 따로 띄운다
                .padding(GaugeOutlineWidth)
                .padding(AlterPadding.XSmall),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.XXSmall),
        ) {
            repeat(GaugeCells) { index ->
                val cellShape = RoundedCornerShape(AlterRadius.Medium)
                val filled = index < level
                Box(
                    modifier = Modifier
                        .size(width = GaugeCellWidth, height = GaugeCellHeight)
                        .background(if (filled) fill else colors.surfaceAccent, cellShape)
                        .then(
                            if (!filled && emptyCellBorder != null) {
                                Modifier.border(GaugeOutlineWidth, emptyCellBorder, cellShape)
                            } else {
                                Modifier
                            },
                        ),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(width = GaugeTipWidth, height = GaugeTipHeight)
                .background(colors.surfaceAccent, RoundedCornerShape(topEnd = GaugeTipRadius, bottomEnd = GaugeTipRadius)),
        )
    }
}

/** SOC 옆 상태 문구. 남은 시간은 숫자만 강조한다. */
@Composable
private fun StatusText(
    status: StatusLine,
    numberStyle: TextStyle,
    unitStyle: TextStyle,
    plainStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    when (status) {
        is StatusLine.Plain -> Text(
            text = status.text,
            modifier = modifier,
            style = plainStyle,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
        is StatusLine.TimeLeft -> Text(
            text = emphasizeArgs(status.template, status.args, numberStyle.toSpanStyle(), unitStyle.toSpanStyle()),
            modifier = modifier,
            style = unitStyle,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

/** 타일 위 구분선. 위쪽 끝에 폭 전체로 긋는다. */
private fun Modifier.tileDivider(color: Color): Modifier = drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = TileDividerWidth.toPx(),
    )
}

/**
 * 정보 타일 (tile_stat): 위 구분선 / 라벨 / 값 + 단위.
 * @param drawDivider false면 구분선을 부모가 대신 긋는다 (타일 하나일 때 폭 전체).
 */
@Composable
private fun RowScope.InfoTile(
    tile: Tile,
    valueStyle: TextStyle,
    unitStyle: TextStyle,
    drawDivider: Boolean = true,
) {
    val colors = AlterTheme.colors
    Column(
        modifier = Modifier
            .weight(1f)
            .then(if (drawDivider) Modifier.tileDivider(colors.surfaceAccent) else Modifier)
            // Figma는 위 구분선 1이 자리를 차지한다(border-top 1 + 여백 16)
            .padding(top = TileDividerWidth + AlterPadding.Normal),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
    ) {
        Text(
            text = tile.label,
            style = AlterTheme.typography.labelMedium,
            color = colors.textTertiary,
            maxLines = 1,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.XXSmall)) {
            Text(
                text = tile.value,
                modifier = Modifier.alignByBaseline(),
                style = valueStyle,
                color = colors.textPrimary,
                maxLines = 1,
            )
            if (tile.unit != null) {
                Text(
                    text = tile.unit,
                    modifier = Modifier.alignByBaseline(),
                    style = unitStyle,
                    color = colors.textTertiary,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * 차량 이미지 자리 표시. 같은 크기의 회색 면이다.
 * TODO 석기님(이슈 #4): 차량 이미지 에셋이 들어오면 CardVehicleStatus의 vehicleImage로 종류별 이미지를 넘긴다.
 */
@Composable
fun VehicleImagePlaceholder(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.card_vehicle_status_vehicle_image)
    Box(
        modifier = modifier
            .semantics { contentDescription = description }
            .background(AlterTheme.colors.surfaceAccent, RoundedCornerShape(AlterRadius.Large)),
    )
}

// ---------------------------------------------------------------------------
// 상황별 내용
// ---------------------------------------------------------------------------

@Immutable
private data class Tile(val label: String, val value: String, val unit: String?)

private sealed interface StatusLine {
    data class Plain(val text: String) : StatusLine

    /** template의 %1$s, %2$s 자리에 args가 들어가고, 그 숫자만 강조된다. */
    data class TimeLeft(val template: String, val args: List<String>) : StatusLine
}

@Immutable
private data class CardContent(
    val headline: String,
    val portraitHeadline: String,
    val status: StatusLine,
    val accent: Color,
    val gaugeLevel: Int,
    val tiles: List<Tile>,
    val portraitTiles: List<Tile>,
    val landscapeImage: VehicleImageKind,
)

/** 상황별 문구 · 색 · 타일을 만든다. 리소스 문자열을 읽어야 해서 Composable이다. */
@Composable
private fun buildContent(state: CardVehicleStatusUiState, mode: CardVehicleStatusMode): CardContent {
    val colors = AlterTheme.colors
    val situation = state.situation
    val unknown = stringResource(R.string.card_vehicle_status_value_unknown)
    val km = stringResource(R.string.card_vehicle_status_unit_km)
    val expected = stringResource(R.string.card_vehicle_status_unit_expected)
    val done = stringResource(R.string.card_vehicle_status_unit_done)

    val rangeTile = Tile(
        label = stringResource(R.string.card_vehicle_status_tile_range),
        value = state.rangeKm?.toString() ?: unknown,
        unit = km,
    )
    val fullAtLabel = stringResource(R.string.card_vehicle_status_tile_full_at)
    val secondTile: Tile? = when (situation) {
        VehicleSituation.CHARGE_RECOMMENDED -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_nearest_station),
            value = state.nearestStationKm?.let { String.format(Locale.ROOT, "%.1f", it) } ?: unknown,
            unit = km,
        )
        VehicleSituation.READY -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_last_charge),
            value = state.lastChargedAt ?: unknown,
            unit = done,
        )
        VehicleSituation.CHARGING,
        VehicleSituation.CHARGING_LOW,
        VehicleSituation.CHARGING_CRITICAL,
        VehicleSituation.SCHEDULED,
        -> Tile(fullAtLabel, state.fullAt ?: unknown, expected)
        VehicleSituation.CHARGED -> Tile(fullAtLabel, state.fullAt ?: unknown, done)
        // TODO 디자이너 확인: 수동 재개(autoResumeAt 없음)일 때 타일 문구("수동 재개 필요") 영문 시안이 없어 "–"로 둔다.
        VehicleSituation.PAUSED -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_auto_resume),
            value = state.autoResumeAt ?: unknown,
            unit = if (state.autoResumeAt != null) expected else null,
        )
        VehicleSituation.CRITICAL_UNPLUGGED -> null
        VehicleSituation.DEPLETED -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_emergency),
            value = stringResource(R.string.card_vehicle_status_tile_emergency_value),
            unit = stringResource(R.string.card_vehicle_status_unit_24h),
        )
        VehicleSituation.CHARGE_ERROR -> null
        VehicleSituation.NO_DATA -> Tile(
            label = stringResource(R.string.card_vehicle_status_tile_retry),
            value = state.retryInSeconds?.toString() ?: unknown,
            unit = stringResource(R.string.card_vehicle_status_unit_seconds_later),
        )
    }
    val tiles = if (situation == VehicleSituation.CHARGE_ERROR) {
        // 11 충전 오류: 원인 · 조치 타일
        // TODO 디자이너 확인: 통신 오류 외 원인(고장 · 정전 · 상태 확인 불가)은 영문 시안이 없어 통신 오류 문구만 있다.
        listOf(
            Tile(
                label = stringResource(R.string.card_vehicle_status_tile_cause),
                value = stringResource(R.string.card_vehicle_status_tile_cause_communication),
                unit = null,
            ),
            Tile(
                label = stringResource(R.string.card_vehicle_status_tile_action),
                value = stringResource(R.string.card_vehicle_status_tile_action_reconnect),
                unit = stringResource(R.string.card_vehicle_status_unit_then_retry),
            ),
        )
    } else {
        listOfNotNull(rangeTile, secondTile)
    }
    // 세로 시안은 01 · 02 · 09 · 10 · 12에서 주행 가능 타일 하나만 보여준다.
    val portraitTiles = when (situation) {
        VehicleSituation.CHARGE_RECOMMENDED,
        VehicleSituation.READY,
        VehicleSituation.CRITICAL_UNPLUGGED,
        VehicleSituation.DEPLETED,
        VehicleSituation.NO_DATA,
        -> listOf(rangeTile)
        else -> tiles
    }

    val name = state.driverName
    val headline = when (situation) {
        VehicleSituation.CHARGE_RECOMMENDED -> stringResource(R.string.card_vehicle_status_headline_charge_recommended, name)
        VehicleSituation.READY -> stringResource(R.string.card_vehicle_status_headline_ready, name)
        VehicleSituation.CHARGING -> stringResource(R.string.card_vehicle_status_headline_charging, name)
        VehicleSituation.CHARGING_LOW -> stringResource(R.string.card_vehicle_status_headline_charging_low)
        VehicleSituation.CHARGING_CRITICAL -> stringResource(R.string.card_vehicle_status_headline_charging_critical)
        VehicleSituation.CHARGED -> stringResource(R.string.card_vehicle_status_headline_charged, name)
        VehicleSituation.PAUSED -> stringResource(R.string.card_vehicle_status_headline_paused)
        VehicleSituation.SCHEDULED -> stringResource(
            R.string.card_vehicle_status_headline_scheduled,
            state.scheduledAt ?: unknown,
        )
        VehicleSituation.CRITICAL_UNPLUGGED -> stringResource(R.string.card_vehicle_status_headline_critical_unplugged)
        VehicleSituation.DEPLETED -> stringResource(R.string.card_vehicle_status_headline_depleted)
        VehicleSituation.CHARGE_ERROR -> stringResource(R.string.card_vehicle_status_headline_charge_error)
        VehicleSituation.NO_DATA -> stringResource(R.string.card_vehicle_status_headline_no_data)
    }
    val portraitHeadline = if (mode == CardVehicleStatusMode.PORTRAIT) {
        portraitHeadlineRes(situation)?.let { stringResource(it) } ?: headline
    } else {
        headline
    }

    val status: StatusLine = when (situation) {
        VehicleSituation.CHARGE_RECOMMENDED ->
            StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_charge_recommended))
        VehicleSituation.READY -> StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_not_connected))
        VehicleSituation.CHARGING,
        VehicleSituation.CHARGING_LOW,
        VehicleSituation.CHARGING_CRITICAL,
        VehicleSituation.CRITICAL_UNPLUGGED,
        -> timeLeft(state.remainingMinutes, unknown)
        VehicleSituation.CHARGED -> StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_charged))
        VehicleSituation.PAUSED -> StatusLine.Plain(
            when (state.pauseReason) {
                PauseReason.GRID_DEMAND -> stringResource(R.string.card_vehicle_status_status_paused_grid_demand)
                // TODO 디자이너 확인: 사유를 모를 때 문구가 시안에 없다.
                null -> unknown
            },
        )
        VehicleSituation.SCHEDULED -> StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_scheduled))
        VehicleSituation.DEPLETED -> StatusLine.Plain(stringResource(R.string.card_vehicle_status_status_depleted))
        VehicleSituation.CHARGE_ERROR -> StatusLine.Plain(
            when (state.fault) {
                ChargeFault.COMMUNICATION -> stringResource(R.string.card_vehicle_status_status_fault_communication)
                // TODO 디자이너 확인: 오류 종류를 모를 때 문구가 시안에 없다.
                null -> unknown
            },
        )
        VehicleSituation.NO_DATA -> StatusLine.Plain(
            stringResource(R.string.card_vehicle_status_status_last_updated, state.lastUpdatedAt ?: unknown),
        )
    }

    val landscapeImage = when (state.chip) {
        ConnectionChip.CONNECTED, ConnectionChip.STANDBY, ConnectionChip.ERROR -> VehicleImageKind.CHARGING
        ConnectionChip.UNPLUGGED, ConnectionChip.OFFLINE -> VehicleImageKind.DRIVING
    }

    return CardContent(
        headline = headline,
        portraitHeadline = portraitHeadline,
        status = status,
        accent = accentColor(situation, colors),
        gaugeLevel = gaugeLevel(state.socPercent),
        tiles = tiles,
        portraitTiles = portraitTiles,
        landscapeImage = landscapeImage,
    )
}

@StringRes
private fun portraitHeadlineRes(situation: VehicleSituation): Int? = when (situation) {
    VehicleSituation.CHARGE_RECOMMENDED -> R.string.card_vehicle_status_portrait_headline_charge_recommended
    VehicleSituation.CHARGING_CRITICAL -> R.string.card_vehicle_status_portrait_headline_charging_critical
    VehicleSituation.PAUSED -> R.string.card_vehicle_status_portrait_headline_paused
    VehicleSituation.DEPLETED -> R.string.card_vehicle_status_portrait_headline_depleted
    VehicleSituation.CHARGE_ERROR -> R.string.card_vehicle_status_portrait_headline_charge_error
    VehicleSituation.NO_DATA -> R.string.card_vehicle_status_portrait_headline_no_data
    else -> null
}

@Composable
private fun timeLeft(minutes: Int?, unknown: String): StatusLine {
    if (minutes == null) {
        return StatusLine.TimeLeft(stringResource(R.string.card_vehicle_status_time_left_minutes), listOf(unknown))
    }
    // 음수(데이터 오류)는 0분으로 보여 준다.
    val safeMinutes = minutes.coerceAtLeast(0)
    val hours = safeMinutes / MINUTES_PER_HOUR
    val rest = safeMinutes % MINUTES_PER_HOUR
    return if (hours > 0) {
        StatusLine.TimeLeft(
            stringResource(R.string.card_vehicle_status_time_left_hours),
            listOf(hours.toString(), rest.toString()),
        )
    } else {
        StatusLine.TimeLeft(stringResource(R.string.card_vehicle_status_time_left_minutes), listOf(rest.toString()))
    }
}

/**
 * SOC 숫자 · 게이지 칸 색. 배터리 구간 표(377:11454)와 상황별 시안 기준.
 * - 기본 blue_600, 부족 · 일시정지 regulation_orange, 위험 · 방전 regulation_red, 완료 informative_active
 * - 오류 · 정보 없음은 값이 없어 비활성 색
 *
 * TODO 디자이너 확인: battery_gauge 설명은 칸마다 다른 색(text_dark_primary → blue_700 → blue_500 → blue_200)인데,
 * 상황별 시안은 모든 칸이 상태 색 하나라 시안을 따랐다.
 */
private fun accentColor(situation: VehicleSituation, colors: AlterColors): Color = when (situation) {
    VehicleSituation.CHARGE_RECOMMENDED,
    VehicleSituation.CHARGING_LOW,
    VehicleSituation.PAUSED,
    -> colors.warning
    VehicleSituation.CHARGING_CRITICAL,
    VehicleSituation.CRITICAL_UNPLUGGED,
    VehicleSituation.DEPLETED,
    -> colors.danger
    VehicleSituation.CHARGED -> colors.positive
    VehicleSituation.CHARGE_ERROR, VehicleSituation.NO_DATA -> colors.textDisabled
    VehicleSituation.READY, VehicleSituation.CHARGING, VehicleSituation.SCHEDULED -> colors.blue600
}

/** level = ceil(SOC ÷ 25), 0~4칸. 값을 모르면 0칸. */
private fun gaugeLevel(socPercent: Int?): Int {
    if (socPercent == null) return 0
    return ceil(socPercent.coerceIn(0, MAX_SOC) / SOC_PER_CELL).toInt().coerceIn(0, GaugeCells)
}

/**
 * 세로 카드 % 단위 색 (card_vehicle_status/soc/unit, 라이트 #8DABED = blue_300).
 * TODO 디자이너 확인: 다크 값이 시안에 없어 다크 팔레트에서 밝기가 비슷한 blue_700을 쓴다.
 */
private fun portraitSocUnitColor(colors: AlterColors): Color = if (colors.isDark) colors.blue700 else colors.blue300

/** template의 %n$s 자리를 args로 채우고, 채운 글자에만 emphasis를 준다. */
private fun emphasizeArgs(
    template: String,
    args: List<String>,
    emphasis: SpanStyle,
    normal: SpanStyle,
): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    ArgPattern.findAll(template).forEach { match ->
        withStyle(normal) { append(template.substring(cursor, match.range.first)) }
        val index = match.groupValues[1].toInt() - 1
        withStyle(emphasis) { append(args.getOrElse(index) { "" }) }
        cursor = match.range.last + 1
    }
    withStyle(normal) { append(template.substring(cursor)) }
}

// ---------------------------------------------------------------------------
// 카드 고유 치수 (Figma card_vehicle_status 변수). 디자인 값에 없는 것만 둔다.
// ---------------------------------------------------------------------------

private val ArgPattern = Regex("%(\\d+)\\$[sd]")
private const val MINUTES_PER_HOUR = 60
private const val SOC_PER_CELL = 25.0
private const val MAX_SOC = 100

/** 헤드라인 최대 줄 수. 넘치면 말줄임 */
private const val LANDSCAPE_HEADLINE_MAX_LINES = 2
private const val PORTRAIT_HEADLINE_MAX_LINES = 3

/** 세로 헤드라인: 단어(한국어는 어절) 단위 줄바꿈 */
private val PortraitHeadlineLineBreak = LineBreak(
    strategy = LineBreak.Strategy.HighQuality,
    strictness = LineBreak.Strictness.Normal,
    wordBreak = LineBreak.WordBreak.Phrase,
)

/** container/radius */
private val CardRadius = 32.dp

/** 카드 그림자: shadow/level_1 + 넓게 퍼지는 한 겹 (0 24 64, #131F40 7%) */
private val CardShadow: List<ShadowLayer> =
    AlterShadows.Level1 + ShadowLayer(offsetY = 24.dp, blur = 64.dp, color = Color(0x12131F40))

/** 상태 패널이 스테이지 위로 겹치는 높이 */
private val LandscapePanelOverlap = 56.dp
private val PortraitPanelOverlap = 24.dp

/** 세로 헤드라인 줄 좌우 여백 */
private val PortraitHeadlinePadding = 40.dp

/** 세로 스테이지: 원형 배경과 위에서 본 차 이미지 크기 */
private class PortraitStageSpec(
    val backdropSize: Dp,
    val backdropAlignment: Alignment,
    val backdropOffsetX: Dp,
    val backdropOffsetY: Dp,
    val imageWidth: Dp,
    val imageHeight: Dp,
)

/** 충전기 연결 · 대기 · 오류 (03 · 05 · 08 · 11): 원 253, 스테이지 오른쪽에서 19 안 · 위로 21 나감. 차 242×153 */
private val PortraitStageCharging = PortraitStageSpec(
    backdropSize = 253.dp,
    backdropAlignment = Alignment.TopEnd,
    backdropOffsetX = (-19).dp,
    backdropOffsetY = (-21).dp,
    imageWidth = 242.dp,
    imageHeight = 153.dp,
)

/** 미연결 · 오프라인 (01 · 02 · 09 · 12): 원 217.6, 세로 가운데 · 오른쪽 밖으로 15.17. 차 139×115(115×139를 -90도) */
private val PortraitStageUnplugged = PortraitStageSpec(
    backdropSize = 217.6.dp,
    backdropAlignment = Alignment.CenterEnd,
    backdropOffsetX = 15.17.dp,
    backdropOffsetY = 0.dp,
    imageWidth = 139.dp,
    imageHeight = 115.dp,
)

/** battery_tile/gap */
private val PortraitBatteryDetailGap = 12.dp

/** chip/ring_padding, 점 크기 */
private val ChipRingPadding = 8.dp
private val ChipDotSize = 12.dp

/** 유리 면 */
private const val GlassAlpha = 0.55f
private const val GlassEdgeAlpha = 0.8f
private val GlassEdgeWidth = 1.5.dp

/** soc/gap, SOC 숫자 빛번짐 (text-shadow 0 0 64, 22%) */
private val SocGap = 4.dp
private const val SocGlowAlpha = 0.22f
private const val SocGlowBlur = 64f

/** gauge: 칸 4개, 칸 30×40, 테두리 2, 꼭지 7×22 */
private const val GaugeCells = 4
private val GaugeCellWidth = 30.dp
private val GaugeCellHeight = 40.dp
private val GaugeOutlineWidth = 2.dp
private val GaugeTipWidth = 7.dp
private val GaugeTipHeight = 22.dp
private val GaugeTipRadius = 3.dp

/** 타일 위 구분선 */
private val TileDividerWidth = 1.dp

/** 가로 타일 단위 글자 18/27 (디자인 값에 없는 크기). 자간 · 줄 높이 처리는 디자인 값 글자와 같다 */
private val LandscapeTileUnitStyle = TextStyle(
    fontFamily = AstaSans,
    fontWeight = AlterFontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 27.sp,
    letterSpacing = AlterLetterSpacing,
    lineHeightStyle = AlterLineHeightStyle,
)

/** 가로 타일 값: numeric/medium 40에 줄 높이만 1.2배(48). Figma tile_range · tile_* 값 */
private fun landscapeTileValueStyle(base: TextStyle): TextStyle = base.copy(lineHeight = 48.sp)

/** 세로 SOC 숫자 80 (디자인 값에 없는 크기, 굵기 · 자간은 numericHero와 같다) */
private fun portraitSocStyle(base: TextStyle): TextStyle = base.copy(fontSize = 80.sp, lineHeight = 120.sp)

// ---------------------------------------------------------------------------
// 미리보기 (샘플: CardVehicleStatusSampleData)
// ---------------------------------------------------------------------------

@Composable
private fun LandscapePreview(state: CardVehicleStatusUiState, dark: Boolean = false) {
    AlterChargeTheme(darkTheme = dark) {
        Box(
            Modifier
                .background(AlterTheme.colors.background)
                .padding(AlterPadding.Normal),
        ) {
            CardVehicleStatus(
                state = state,
                modifier = Modifier.size(width = 790.dp, height = 1040.dp),
            )
        }
    }
}

@Composable
private fun PortraitPreview(state: CardVehicleStatusUiState, dark: Boolean = false) {
    AlterChargeTheme(darkTheme = dark) {
        Box(
            Modifier
                .background(AlterTheme.colors.background)
                .padding(AlterPadding.Normal),
        ) {
            CardVehicleStatus(
                state = state,
                mode = CardVehicleStatusMode.PORTRAIT,
                modifier = Modifier.size(width = 776.dp, height = 652.dp),
            )
        }
    }
}

@Preview(name = "01 충전 권장 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewChargeRecommended() = LandscapePreview(CardVehicleStatusSampleData.ChargeRecommended)

@Preview(name = "03 충전 중 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewCharging() = LandscapePreview(CardVehicleStatusSampleData.Charging)

@Preview(name = "03 충전 중 · 가로 · 다크", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewChargingDark() = LandscapePreview(CardVehicleStatusSampleData.Charging, dark = true)

@Preview(name = "03 충전 중 · 가로 · EN", widthDp = 822, heightDp = 1072, locale = "en")
@Composable
private fun PreviewChargingEn() = LandscapePreview(CardVehicleStatusSampleData.Charging)

@Preview(name = "05 배터리 위험 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewChargingCritical() = LandscapePreview(CardVehicleStatusSampleData.ChargingCritical)

@Preview(name = "06 충전 완료 · 가로 · 다크", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewCharged() = LandscapePreview(CardVehicleStatusSampleData.Charged, dark = true)

@Preview(name = "07 일시정지 · 가로 · EN", widthDp = 822, heightDp = 1072, locale = "en")
@Composable
private fun PreviewPaused() = LandscapePreview(CardVehicleStatusSampleData.Paused)

@Preview(name = "09 위험 · 미연결 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewCriticalUnplugged() = LandscapePreview(CardVehicleStatusSampleData.CriticalUnplugged)

@Preview(name = "10 방전 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewDepleted() = LandscapePreview(CardVehicleStatusSampleData.Depleted)

@Preview(name = "11 충전 오류 · 가로", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewChargeError() = LandscapePreview(CardVehicleStatusSampleData.ChargeError)

@Preview(name = "12 정보 없음 · 가로 · 다크", widthDp = 822, heightDp = 1072)
@Composable
private fun PreviewNoData() = LandscapePreview(CardVehicleStatusSampleData.NoData, dark = true)

@Preview(name = "01 충전 권장 · 세로", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitChargeRecommended() = PortraitPreview(CardVehicleStatusSampleData.ChargeRecommended)

@Preview(name = "03 충전 중 · 세로", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitCharging() = PortraitPreview(CardVehicleStatusSampleData.Charging)

@Preview(name = "03 충전 중 · 세로 · 다크 · EN", widthDp = 808, heightDp = 684, locale = "en")
@Composable
private fun PreviewPortraitChargingDarkEn() = PortraitPreview(CardVehicleStatusSampleData.Charging, dark = true)

@Preview(name = "05 배터리 위험 · 세로 · 다크", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitCritical() = PortraitPreview(CardVehicleStatusSampleData.ChargingCritical, dark = true)

@Preview(name = "11 충전 오류 · 세로", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitError() = PortraitPreview(CardVehicleStatusSampleData.ChargeError)

@Preview(name = "12 정보 없음 · 세로", widthDp = 808, heightDp = 684)
@Composable
private fun PreviewPortraitNoData() = PortraitPreview(CardVehicleStatusSampleData.NoData)

@Preview(name = "07 일시정지 · 세로 · EN (헤드라인 줄바꿈)", widthDp = 808, heightDp = 684, locale = "en")
@Composable
private fun PreviewPortraitPausedEn() = PortraitPreview(CardVehicleStatusSampleData.Paused)
