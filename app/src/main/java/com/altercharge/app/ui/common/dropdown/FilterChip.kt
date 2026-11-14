package com.altercharge.app.ui.common.dropdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * Figma chip_filter (panel_station_list filter_row). 좌 16 · 우 12 · 위아래 4, 글자 20, 아래 화살표 16.
 * 선택(또는 메뉴 열림): accent 배경 + 굵은 accent 글자. 미선택: 배경 없이 1.5 테두리(item/divider) + 보통 글자.
 */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AlterTheme.colors
    val active = selected || expanded
    val shape = RoundedCornerShape(AlterRadius.Full)
    val content = if (active) colors.blue500 else colors.textPrimary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(shape)
            .background(if (active) colors.blue50 else Color.Transparent)
            .then(if (active) Modifier else Modifier.border(1.5.dp, colors.surfaceAccent, shape))
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
    ) {
        Text(
            text = label,
            style = if (active) AlterTheme.typography.bodyXXSmall else AlterTheme.typography.labelSmall,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Image(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(content),
            modifier = Modifier
                .size(16.dp)
                .rotate(if (expanded) -90F else 90F),
        )
    }
}
