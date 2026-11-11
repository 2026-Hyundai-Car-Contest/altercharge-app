package com.altercharge.app.ui.card.vehiclestatus

import android.graphics.BlurMaskFilter
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterColors
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// 조각
// ---------------------------------------------------------------------------

@Composable
internal fun Headline(
    text: String,
    maxLines: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = AlterTheme.typography.headlineLarge,
    lineBreak: LineBreak = LineBreak.Unspecified,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontWeight = AlterFontWeight.Strong, lineBreak = lineBreak),
        color = AlterTheme.colors.textPrimary,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * 줄 수를 넘으면 글자를 한 단계씩 줄이는 헤드라인 (개발자 Q&A Q1).
 * [styles]를 큰 것부터 차례로 재서 [maxLines] 안에 들어오는 첫 크기를 쓴다.
 * 가장 작은 크기에서도 넘치면 [maxLines]까지 보여주고 말줄임한다.
 */
@Composable
internal fun AutoShrinkHeadline(
    text: String,
    styles: List<TextStyle>,
    modifier: Modifier = Modifier,
    maxLines: Int = AUTO_SHRINK_HEADLINE_MAX_LINES,
    lineBreak: LineBreak = PortraitHeadlineLineBreak,
) {
    val measurer = rememberTextMeasurer()
    // 글자 크기 설정(fontScale)이 바뀌면 다시 재야 한다
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        val maxWidth = constraints.maxWidth
        val candidates = remember(styles, lineBreak) {
            styles.map { it.copy(fontWeight = AlterFontWeight.Strong, lineBreak = lineBreak) }
        }
        val style = remember(text, candidates, maxWidth, maxLines, density) {
            candidates.firstOrNull { candidate ->
                !measurer.measure(
                    text = text,
                    style = candidate,
                    maxLines = maxLines,
                    constraints = Constraints(maxWidth = maxWidth),
                ).hasVisualOverflow
            } ?: candidates.last()
        }
        Text(
            text = text,
            style = style,
            color = AlterTheme.colors.textPrimary,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 자동 축소 헤드라인 줄 수. Figma portrait · portrait_tall · portrait_compact 헤드라인 2줄 말줄임 */
private const val AUTO_SHRINK_HEADLINE_MAX_LINES = 2

/** 차량 이미지가 놓이는 면. 왼쪽 위에 연결 칩이 뜬다 (WF는 16, 나머지는 24 안쪽). */
@Composable
internal fun Stage(
    chip: ConnectionChip,
    chipInset: Dp = AlterPadding.Large,
    background: Color = AlterTheme.colors.background,
    image: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(AlterRadius.XXLarge)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(background, shape),
    ) {
        image()
        ChipStatus(
            chip = chip,
            modifier = Modifier.padding(start = chipInset, top = chipInset),
        )
    }
}

/**
 * 상태 패널(status_panel) 뒤에 깔리는 "뒤 흐림" 대용. 스테이지와 패널 사이 층에 놓는다.
 *
 * Figma 패널은 BACKGROUND_BLUR 40이라 뒤에 있는 스테이지 아래쪽과 그 밑의 카드 배경이 흐리게 비친다.
 * Compose에는 뒤를 흐리는 기능이 없어서, 패널 뒤에 있을 내용(스테이지 배경 + 이미지 + 카드 배경)을 한 번 더 그려
 * 흐린 뒤 패널이 겹치는 띠 모양으로 잘라낸다. 흐림은 API 31 이상에서만 걸린다. 그 아래(minSdk 28)에서는 복사본이
 * 흐려지지 않은 채 원본 위에 겹치므로 띠 구간만 아래 모서리가 각지고 카드 배경이 비친다(크래시 없음, 차량은 API 34).
 *
 * 이 composable은 스테이지와 같은 폭, 스테이지 높이 + [extendBelow] 높이로 놓인다(StageWithPanel이 정한다).
 * - 보이는 띠: 스테이지 아래 [bandHeight](패널이 겹치는 높이)부터 아래 끝([extendBelow]까지)이다.
 *   스테이지 아래 끝도 흐려져야 시안처럼 경계가 사라지므로 스테이지보다 아래로 더 내려간다.
 * - 흐림 복사본은 스테이지보다 사방 [PanelBackdropMargin]만큼 크게 만든다. 가장자리에 여유가 있어야
 *   흐림이 끝에서 어두워지지 않고, 스테이지 밑 카드 배경(단색)이 섞여 띠가 아래로 갈수록 밝아진다.
 * - 큰 상자는 부모보다 커서 Compose가 넘친 만큼 가운데로 민다. 그래서 상자 높이를 "부모 높이 + 여유 × 2"로 두고
 *   안쪽 스테이지 복사본을 위에서 여유만큼 내리면, 복사본이 원본 스테이지와 정확히 같은 자리에 온다.
 */
@Composable
internal fun PanelBackdrop(
    bandHeight: Dp,
    extendBelow: Dp,
    stageBackground: Color = AlterTheme.colors.background,
    cardBackground: Color = AlterTheme.colors.containerBackground,
    image: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // 안쪽 Box 안에서는 BoxWithConstraints 값에 바로 닿을 수 없어(DslMarker) 여기서 미리 꺼낸다
        val stageWidth = maxWidth
        val stageHeight = maxHeight - extendBelow
        val backdropHeight = maxHeight
        // 보이는 띠: 스테이지 아래 bandHeight부터 이 상자 아래 끝까지. 위 두 모서리는 패널 모서리(24)와 같다
        val bandShape = remember(stageHeight, bandHeight, density) {
            GenericShape { size, _ ->
                val top = with(density) { (stageHeight - bandHeight).toPx() }.coerceAtLeast(0f)
                val radius = with(density) { AlterRadius.XXLarge.toPx() }
                addRoundRect(
                    RoundRect(
                        left = 0f,
                        top = top,
                        right = size.width,
                        bottom = size.height,
                        topLeftCornerRadius = CornerRadius(radius),
                        topRightCornerRadius = CornerRadius(radius),
                    ),
                )
            }
        }
        // 복사본은 장식이라 접근성 트리에서 뺀다 (안 그러면 차량 이미지 설명이 두 번 읽힌다)
        Box(Modifier.matchParentSize().clip(bandShape).clearAndSetSemantics {}) {
            Box(
                modifier = Modifier
                    .requiredSize(
                        width = stageWidth + PanelBackdropMargin * 2,
                        height = backdropHeight + PanelBackdropMargin * 2,
                    )
                    .background(cardBackground)
                    .blur(PanelBackdropBlurRadius, BlurredEdgeTreatment.Unbounded),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = PanelBackdropMargin)
                        .size(width = stageWidth, height = stageHeight)
                        .background(stageBackground),
                ) {
                    image()
                }
            }
        }
    }
}

/**
 * status_panel BACKGROUND_BLUR 40 = 가우스 표준편차 20. Modifier.blur(RenderEffect)의 radius는 표준편차로 쓰인다.
 * (BlurMaskFilter용 반지름 변환을 쓰면 띠가 시안보다 넓게 번진다 — 에뮬레이터 캡처로 확인)
 */
internal val PanelBackdropBlurRadius = 20.dp

/** 흐림 복사본을 스테이지보다 사방으로 더 키우는 여유. 표준편차 20의 3배 */
private val PanelBackdropMargin = 60.dp

/**
 * 위에서 본 차 + 원형 배경 (세로 · tall · WF 스테이지). 둘 다 패널이 겹친 부분까지 포함한 스테이지 기준이다.
 * 원은 스테이지보다 클 수 있어(예: 290.7 원 · 277 스테이지) 자리를 잡지 않고 뒤에 그린다.
 */
@Composable
internal fun StageVehicle(
    spec: StageImageSpec,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val backdropColor = AlterTheme.colors.surfaceHigh
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val diameter = spec.backdropSize?.toPx() ?: return@drawBehind
                val topLeft = spec.backdropAlignment.align(
                    IntSize(diameter.roundToInt(), diameter.roundToInt()),
                    IntSize(size.width.roundToInt(), size.height.roundToInt()),
                    layoutDirection,
                )
                drawCircle(
                    color = backdropColor,
                    radius = diameter / 2,
                    center = Offset(
                        x = topLeft.x + spec.backdropOffsetX.toPx() + diameter / 2,
                        y = topLeft.y + spec.backdropOffsetY.toPx() + diameter / 2,
                    ),
                )
            },
    ) {
        vehicleImage(
            VehicleImageKind.TOP_VIEW,
            Modifier
                .align(spec.imageAlignment)
                .padding(end = spec.imageEndPadding)
                .size(width = spec.imageWidth, height = spec.imageHeight),
        )
    }
}

/**
 * 배터리 라벨 + SOC 큰 숫자 (battery_value).
 * @param glow SOC 숫자 빛번짐. 세로 시안에만 있고 tall · WF 시안(366:37686 · 366:37763 · 366:37801 · 366:37835 등)에는 없다.
 */
@Composable
internal fun BatteryValue(
    state: CardVehicleStatusUiState,
    content: CardContent,
    labelStyle: TextStyle,
    socStyle: TextStyle,
    glow: Boolean = false,
) {
    val colors = AlterTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.XXSmall)) {
        Text(
            text = stringResource(R.string.card_vehicle_status_battery),
            style = labelStyle,
            color = colors.textSecondary,
        )
        Row {
            Soc(
                state = state,
                content = content,
                valueStyle = socStyle,
                unitColor = portraitSocUnitColor(colors),
                glow = glow,
            )
        }
    }
}

/**
 * 게이지 + 남은 시간 (battery_detail). statusStyle은 숫자 · 단위 글자 크기(굵기는 보통으로 바꾼다).
 * 세로 · tall · WF 시안은 gauge/outline · gauge/cell_empty(#EDEEF2) 변수를 쓴다: 테두리가 한 단계 연하고 빈 칸에 2 테두리
 */
@Composable
internal fun BatteryDetail(
    content: CardContent,
    statusStyle: TextStyle,
    horizontalAlignment: Alignment.Horizontal,
    gap: Dp = BatteryTileGap,
    showStatus: Boolean = true,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        BatteryGauge(
            level = content.gaugeLevel,
            fill = content.accent,
            outline = colors.surfaceHigh,
            emptyCellBorder = colors.surfaceHigh,
        )
        if (showStatus && content.status != StatusLine.None) {
            StatusText(
                status = content.status,
                numberStyle = statusStyle.copy(fontWeight = AlterFontWeight.Normal, color = colors.textPrimary),
                unitStyle = statusStyle.copy(fontWeight = AlterFontWeight.Normal, color = colors.textTertiary),
                plainStyle = typography.labelMedium.copy(color = colors.textTertiary),
            )
        }
    }
}

/** WF 요약 한 줄 (info_summary): 타일 내용을 " · "로 이어 쓴다. */
@Composable
internal fun InfoSummary(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        // 한국어는 기본이 글자 단위 줄바꿈이라 어절 단위로 바꾼다 (항목 안은 줄바꿈 없는 공백)
        style = AlterTheme.typography.labelMedium.copy(lineBreak = PortraitHeadlineLineBreak),
        color = AlterTheme.colors.textSecondary,
    )
}

/** 연결 칩 (chip_status). 바깥 고리 + 안쪽 알약. */
@Composable
private fun ChipStatus(chip: ConnectionChip, modifier: Modifier = Modifier) {
    val colors = AlterTheme.colors
    val (labelRes, dotColor) = when (chip) {
        ConnectionChip.CONNECTED -> R.string.card_vehicle_status_chip_connected to colors.blue500
        ConnectionChip.UNPLUGGED -> R.string.card_vehicle_status_chip_unplugged to colors.negative
        ConnectionChip.ERROR -> R.string.card_vehicle_status_chip_error to colors.negative
        // Figma chip/dot/standby → basic/gray/basic_300. 오프라인 variant(371:9645)의 점도 같은 색
        ConnectionChip.STANDBY -> R.string.card_vehicle_status_chip_standby to colors.gray300
        ConnectionChip.OFFLINE -> R.string.card_vehicle_status_chip_offline to colors.gray300
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
                    .drawBehind {
                        val glowRadius = size.minDimension / 2 + ChipDotGlowSpread.toPx()
                        val blur = figmaBlurToAndroidRadius(ChipDotGlowBlur.toPx())
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = (if (chip == ConnectionChip.UNPLUGGED) dotColor.copy(alpha = ChipDotGlowAlpha) else ChipDotGlowColor).toArgb()
                            if (blur > 0f) maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
                        }
                        drawIntoCanvas { it.nativeCanvas.drawCircle(center.x, center.y, glowRadius, paint) }
                    }
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

/**
 * pleos basic/gray/basic_300 (라이트 #C7C9CE · 다크 #686A72). 디자인 값(AlterColors)에 없는 단계라 C 카드 안에서만 쓴다.
 * 칩 점(standby · offline)과 값 없는 SOC "--"가 이 색이다.
 * 공용 팔레트(AlterColors)에 gray300이 생기면 이 확장을 지운다 — 멤버가 확장보다 우선해 조용히 무시된다.
 */
internal val AlterColors.gray300: Color
    get() = if (isDark) Color(0xFF686A72) else Color(0xFFC7C9CE)

/** 상태 패널의 유리 면 (glass + glass_edge). */
@Composable
internal fun GlassPanel(
    modifier: Modifier,
    contentPadding: Dp,
    glassAlpha: Float = GlassAlpha,
    verticalPadding: Dp = contentPadding,
    content: @Composable () -> Unit,
) {
    val colors = AlterTheme.colors
    val shape = RoundedCornerShape(AlterRadius.XXLarge)
    // TODO 서희님: Figma는 뒤를 흐리게(backdrop blur 20) 하고 그림자(0 6 12, 6%)를 준다. Compose 기본으로는 뒤 흐림이 안 돼 면 색만 쓴다.
    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceHigh.copy(alpha = glassAlpha), shape)
            .border(GlassEdgeWidth, colors.surface.copy(alpha = GlassEdgeAlpha), shape)
            .padding(horizontal = contentPadding, vertical = verticalPadding),
    ) {
        content()
    }
}

/** 배터리 % 큰 숫자와 단위. 바깥 Row의 글자 기준선에 맞춘다. */
@Composable
internal fun RowScope.Soc(
    state: CardVehicleStatusUiState,
    content: CardContent,
    valueStyle: TextStyle,
    unitColor: Color,
    glow: Boolean,
) {
    val typography = AlterTheme.typography
    val known = state.socPercent != null
    Row(
        modifier = Modifier.alignByBaseline(),
        horizontalArrangement = Arrangement.spacedBy(SocGap),
    ) {
        // 센서 값이 범위를 벗어나도 0~100으로 보여 준다.
        val socText = state.socPercent?.coerceIn(0, MAX_SOC)?.toString()
            ?: stringResource(R.string.card_vehicle_status_soc_unknown)
        if (known && glow) {
            // Compose 글자 그림자는 한 겹이라 같은 글자를 두 번 겹쳐 그림자 두 겹을 낸다 (글자 색이 불투명이라 겹쳐도 같다)
            val density = LocalDensity.current
            val wide = with(density) { figmaBlurToAndroidRadius(SocGlowWideBlur.toPx()) }
            val near = with(density) { figmaBlurToAndroidRadius(SocGlowNearBlur.toPx()) }
            Box(modifier = Modifier.alignByBaseline()) {
                Text(
                    text = socText,
                    style = valueStyle.copy(shadow = Shadow(content.accent.copy(alpha = SocGlowWideAlpha), Offset.Zero, wide)),
                    color = content.accent,
                    maxLines = 1,
                )
                Text(
                    text = socText,
                    style = valueStyle.copy(shadow = Shadow(content.accent.copy(alpha = SocGlowNearAlpha), Offset.Zero, near)),
                    color = content.accent,
                    maxLines = 1,
                )
            }
        } else {
            Text(
                text = socText,
                modifier = Modifier.alignByBaseline(),
                style = valueStyle,
                color = content.accent,
                maxLines = 1,
            )
        }
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
internal fun BatteryGauge(
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
internal fun StatusText(
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
        StatusLine.None -> Unit
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
internal fun Modifier.tileDivider(color: Color): Modifier = drawBehind {
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
internal fun RowScope.InfoTile(
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
 * 차량 이미지. 종류별 에셋(이슈 #4)을 주어진 자리 크기에 맞춰 그린다.
 * 자리 크기는 시안에서 회전한 뒤 화면에 차지하는 크기라 이미지를 돌리지 않고 가운데에 맞춘다.
 * DRIVING은 자리 비율이 Figma 슬롯(774×411)과 같다는 전제로 자른다. 자리 비율을 바꾸면 이미지가 늘어난다.
 */
@Composable
fun VehicleImage(kind: VehicleImageKind, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.card_vehicle_status_vehicle_image)
    if (kind == VehicleImageKind.DRIVING) {
        // Figma vehicle_driving(371:9513)은 같은 1440×444 파일을 CROP으로 쓴다. 자리의 (u, v) → 이미지의
        // (0.2138 + 0.5723u, -0.2144 + v): 가로는 21.4%~78.6%만 보이고, 세로는 자리 위 21.4%가 비고 아래가 잘린다.
        val bitmap = ImageBitmap.imageResource(kind.drawableRes)
        Canvas(modifier.clipToBounds().semantics { contentDescription = description }) {
            val dstWidth = size.width / DRIVING_CROP_SCALE_X
            val dstHeight = size.height / DRIVING_CROP_SCALE_Y
            drawImage(
                image = bitmap,
                dstOffset = IntOffset((-DRIVING_CROP_OFFSET_X * dstWidth).roundToInt(), (-DRIVING_CROP_OFFSET_Y * dstHeight).roundToInt()),
                dstSize = IntSize(dstWidth.roundToInt(), dstHeight.roundToInt()),
                filterQuality = FilterQuality.High,
            )
        }
        return
    }
    Image(
        painter = painterResource(kind.drawableRes),
        contentDescription = description,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/** Figma vehicle_driving imageTransform [[0.5723, 0, 0.2138], [0, 1, -0.2144]] */
private const val DRIVING_CROP_SCALE_X = 0.5723f
private const val DRIVING_CROP_OFFSET_X = 0.2138f
private const val DRIVING_CROP_SCALE_Y = 1f
private const val DRIVING_CROP_OFFSET_Y = -0.2144f

@get:DrawableRes
private val VehicleImageKind.drawableRes: Int
    get() = when (this) {
        VehicleImageKind.CHARGING -> R.drawable.img_vehicle_charging
        VehicleImageKind.DRIVING -> R.drawable.img_vehicle_driving
        VehicleImageKind.TOP_VIEW -> R.drawable.img_vehicle_top_view
    }

// ---------------------------------------------------------------------------
// 조각 치수 (Figma card_vehicle_status 변수). 디자인 값에 없는 것만 둔다.
// ---------------------------------------------------------------------------

/**
 * 스테이지 안 원형 배경과 위에서 본 차 자리. 위치는 패널이 겹친 부분까지 포함한 스테이지 기준이다.
 * 차 크기는 회전한 뒤 화면에 차지하는 크기다(Figma는 세로 이미지를 -90도 돌려 둔다).
 * @param backdropSize null이면 원이 없다
 */
internal class StageImageSpec(
    val backdropSize: Dp?,
    val imageWidth: Dp,
    val imageHeight: Dp,
    val backdropAlignment: Alignment = Alignment.Center,
    val backdropOffsetX: Dp = 0.dp,
    val backdropOffsetY: Dp = 0.dp,
    val imageAlignment: Alignment = Alignment.Center,
    val imageEndPadding: Dp = 0.dp,
)

/** battery_tile/gap */
internal val BatteryTileGap = 12.dp

/** chip/ring_padding, 점 크기 */
private val ChipRingPadding = 8.dp
private val ChipDotSize = 12.dp

/** 유리 면 */
private const val GlassAlpha = 0.55f

/**
 * 민무늬 스테이지(세로 · tall · WF) 위 유리 면 불투명도.
 * Figma는 surface_high 55% + 뒤 흐림 20 + 그림자라, 렌더 결과가 스테이지(#F7F8FA) 위든 흰 카드 위든 #EFF0F3 근처로 고르다
 * (366:37686 · 366:37763 · 366:37801 · 366:37835, 세로 455:60518 캡처 실측). 55%만 쓰면 스테이지가 끝나는 줄에 이음새가 보여
 * 렌더 색에 맞춰 90%로 둔다(흰 위 #EFF0F3, #F7F8FA 위 #EEEFF2). 가로는 뒤에 차 이미지가 있어 55% 그대로.
 * 다크는 Figma 렌더가 55% 그대로와 같다(EN 다크 07 세로 457:78886 실측 #28292D = #313236 55% · 카드 #1D1E21 45%).
 */
internal val FlatStageGlassAlpha: Float
    @Composable get() = if (AlterTheme.colors.isDark) GlassAlpha else FLAT_STAGE_GLASS_ALPHA_LIGHT

private const val FLAT_STAGE_GLASS_ALPHA_LIGHT = 0.9f
private const val GlassEdgeAlpha = 0.8f
private val GlassEdgeWidth = 1.5.dp

/** soc/gap, SOC 숫자 빛번짐 두 겹 (soc_value text-shadow 0 0 64 22% · 0 0 24 10%, 457:78886) */
private val SocGap = 4.dp
private const val SocGlowWideAlpha = 0.22f
private val SocGlowWideBlur = 64.dp
private const val SocGlowNearAlpha = 0.10f
private val SocGlowNearBlur = 24.dp

/** 칩 점 빛번짐 (dot drop shadow 0 0 blur 6 spread 2, 연결됨 · 대기는 #99C6CF 35%. 라이트 · 다크 같음) */
private val ChipDotGlowSpread = 2.dp
private val ChipDotGlowBlur = 6.dp
private val ChipDotGlowColor = Color(0x5999C6CF)

/** 연결 안 됨(빨강 점)은 빛번짐도 점과 같은 색 35% (Figma 칩 점 SVG) */
private const val ChipDotGlowAlpha = 0.35f

/**
 * Figma blur(= 가우스 표준편차 × 2)를 안드로이드 blur 반지름(px)으로 바꾼다.
 * 안드로이드는 반지름 r을 표준편차 0.57735 × r + 0.5로 쓴다.
 */
private fun figmaBlurToAndroidRadius(blurPx: Float): Float =
    ((blurPx / 2f - 0.5f) / 0.57735f).coerceAtLeast(0f)

/** gauge: 칸 4개, 칸 30×40, 테두리 2, 꼭지 7×22 */
internal const val GaugeCells = 4
private val GaugeCellWidth = 30.dp
private val GaugeCellHeight = 40.dp
private val GaugeOutlineWidth = 2.dp
private val GaugeTipWidth = 7.dp
private val GaugeTipHeight = 22.dp
private val GaugeTipRadius = 3.dp

/** 타일 위 구분선 */
private val TileDividerWidth = 1.dp
