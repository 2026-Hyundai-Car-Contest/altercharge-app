/** 상태 패널 안의 유리 면 · SOC 숫자 · 배터리 게이지 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** glass + glass_edge */
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
    // 뒤 흐림(backdrop blur 20)은 Compose에 없어서 무대 쪽 PanelBackdrop이 흐린 복사본을 깔아 대신한다
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

/**
 * battery_value.
 * @param glow 세로 시안에만 있고 tall · WF 시안(366:37686 · 366:37763 · 366:37801 · 366:37835 등)에는 없다.
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
 * battery_detail. statusStyle의 굵기는 보통으로 바꿔 쓴다.
 * 세로 · tall · WF 시안은 gauge/outline · gauge/cell_empty 변수라 테두리가 한 단계 연하고 빈 칸에 2 테두리가 있다.
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
        val socText = state.socPercent?.coerceIn(0, MAX_SOC)?.toString()
            ?: stringResource(R.string.card_vehicle_status_soc_unknown)
        if (known && glow) {
            // Compose 글자 그림자는 한 겹이라 같은 글자를 두 번 겹쳐 두 겹을 낸다
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

/** battery_gauge. level = 채운 칸 수 0~4 */
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
                // Figma는 테두리 2가 자리를 차지한다(→ 159×60). Compose border는 자리를 안 차지해 따로 띄운다
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

/** 남은 시간은 숫자만 강조한다 */
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

private const val GlassAlpha = 0.55f

/**
 * 민무늬 스테이지(세로 · tall · WF) 위 유리 면.
 * Figma 렌더는 뒤 흐림 덕에 스테이지 위든 흰 카드 위든 #EFF0F3 근처로 고르다 (455:60518 캡처 실측).
 * 55%만 쓰면 스테이지 끝 줄에 이음새가 보여 라이트는 90%로 둔다. 다크는 55% 그대로와 같다 (457:78886 실측).
 */
internal val FlatStageGlassAlpha: Float
    @Composable get() = if (AlterTheme.colors.isDark) GlassAlpha else FLAT_STAGE_GLASS_ALPHA_LIGHT

private const val FLAT_STAGE_GLASS_ALPHA_LIGHT = 0.9f
private const val GlassEdgeAlpha = 0.8f
private val GlassEdgeWidth = 1.5.dp

/** soc/gap, soc_value text-shadow (457:78886) */
private val SocGap = 4.dp
private const val SocGlowWideAlpha = 0.22f
private val SocGlowWideBlur = 64.dp
private const val SocGlowNearAlpha = 0.10f
private val SocGlowNearBlur = 24.dp

private val GaugeCellWidth = 30.dp
private val GaugeCellHeight = 40.dp
private val GaugeOutlineWidth = 2.dp
private val GaugeTipWidth = 7.dp
private val GaugeTipHeight = 22.dp
private val GaugeTipRadius = 3.dp
