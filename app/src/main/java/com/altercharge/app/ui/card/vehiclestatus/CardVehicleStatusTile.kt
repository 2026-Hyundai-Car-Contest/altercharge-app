/** C 카드 정보 타일과 WF 요약 줄 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * tile_stat.
 * @param drawDivider false면 부모가 대신 긋는다 (타일 하나일 때 폭 전체)
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

internal fun Modifier.tileDivider(color: Color): Modifier = drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = TileDividerWidth.toPx(),
    )
}

/** info_summary */
@Composable
internal fun InfoSummary(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        // 한국어는 기본이 글자 단위 줄바꿈이라 어절 단위로 바꾼다
        style = AlterTheme.typography.labelMedium.copy(lineBreak = PortraitHeadlineLineBreak),
        color = AlterTheme.colors.textSecondary,
    )
}

private val TileDividerWidth = 1.dp
