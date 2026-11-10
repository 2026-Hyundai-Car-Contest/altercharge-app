package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
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
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(BatteryTileGap),
    ) {
        BatteryGauge(
            level = content.gaugeLevel,
            fill = content.accent,
            outline = colors.surfaceHigh,
            emptyCellBorder = colors.surfaceHigh,
        )
        StatusText(
            status = content.status,
            numberStyle = statusStyle.copy(fontWeight = AlterFontWeight.Normal, color = colors.textPrimary),
            unitStyle = statusStyle.copy(fontWeight = AlterFontWeight.Normal, color = colors.textTertiary),
            plainStyle = typography.labelMedium.copy(color = colors.textTertiary),
        )
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
internal fun GlassPanel(
    modifier: Modifier,
    contentPadding: Dp,
    glassAlpha: Float = GlassAlpha,
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
            .padding(contentPadding),
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
        Text(
            // 센서 값이 범위를 벗어나도 0~100으로 보여 준다.
            text = state.socPercent?.coerceIn(0, MAX_SOC)?.toString()
                ?: stringResource(R.string.card_vehicle_status_soc_unknown),
            modifier = Modifier.alignByBaseline(),
            style = if (known && glow) {
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

/** 세로 충전기 연결 · 대기 · 오류 (03 · 05 · 08 · 11): 원 253, 스테이지 오른쪽에서 19 안 · 위로 21 나감. 차 242×153 */
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
 */
internal const val FlatStageGlassAlpha = 0.9f
private const val GlassEdgeAlpha = 0.8f
private val GlassEdgeWidth = 1.5.dp

/** soc/gap, SOC 숫자 빛번짐 (text-shadow 0 0 64, 22%) */
private val SocGap = 4.dp
private const val SocGlowAlpha = 0.22f
private const val SocGlowBlur = 64f

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
