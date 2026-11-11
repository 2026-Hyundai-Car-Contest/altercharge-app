/**
 * 정보 타일(tile_stat) · 타일 위 구분선 · WF 요약 한 줄(info_summary).
 */
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

/** 타일 위 구분선. 위쪽 끝에 폭 전체로 긋는다. */
internal fun Modifier.tileDivider(color: Color): Modifier = drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = TileDividerWidth.toPx(),
    )
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

/** 타일 위 구분선 */
private val TileDividerWidth = 1.dp
