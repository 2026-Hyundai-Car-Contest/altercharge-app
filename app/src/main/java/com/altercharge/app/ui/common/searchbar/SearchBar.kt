package com.altercharge.app.ui.common.searchbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * Figma search_field (820:62965). 높이 61, 왼쪽 20 · 오른쪽 16, 아이콘 · 글자 · 마이크 간격 12, 모서리 16.
 * Material TextField는 안쪽 여백 · 최소 높이(56)가 있어 시안 높이가 안 나와 BasicTextField로 그린다.
 *
 * @param offline 오프라인이면 입력할 수 없고 안내 문구를 보여 준다 (REQ_SL_002 · REQ_SL_003 예외).
 *   마이크는 눌리게 두고, 누르면 쓰는 쪽이 "오프라인에서는 검색할 수 없어요"를 알린다.
 */
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit,
    onMicClick: () -> Unit,
    offline: Boolean = false,
) {
    val colors = AlterTheme.colors
    val shape = RoundedCornerShape(AlterRadius.Large)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.fieldBackground)
            .border(1.5.dp, colors.surfaceBasic, shape)
            .height(61.dp)
            .padding(start = 20.dp, end = 16.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.contentNeutral),
            // Figma CM_006 · V2-3: 오프라인이면 검색 아이콘도 35%
            modifier = Modifier.size(24.dp).alpha(if (offline) 0.35f else 1f),
        )

        val textStyle = AlterTheme.typography.labelSmall
        BasicTextField(
            value = if (offline) "" else value,
            onValueChange = onValueChange,
            enabled = !offline,
            singleLine = true,
            textStyle = textStyle.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.blue500),
            modifier = Modifier.weight(1f),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (offline || value.isEmpty()) {
                        Text(
                            text = stringResource(
                                if (offline) R.string.search_bar_placeholder_offline else R.string.search_bar_placeholder,
                            ),
                            style = textStyle,
                            color = if (offline) colors.textDisabled else colors.textTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    field()
                }
            },
        )

        Image(
            painter = painterResource(R.drawable.ic_mic),
            contentDescription = stringResource(R.string.search_bar_voice),
            colorFilter = ColorFilter.tint(colors.contentNeutral),
            // Figma v2 가이드 V2-3: 오프라인이면 음성 버튼도 비활성(35%)
            modifier = Modifier
                .size(24.dp)
                .alpha(if (offline) 0.35f else 1f)
                .clickable(enabled = !offline) { onMicClick() },
        )
    }
}

@Preview(name = "SearchBar(Light/KO)", locale = "ko")
@Composable
private fun SearchBarPreviewLightKo() {
    AlterChargeTheme() {
        SearchBar(
            onValueChange = {},
            onMicClick = {}
        )
    }
}

@Preview(name = "SearchBar(Light/KO)", locale = "ko")
@Composable
private fun SearchBarPreviewDarkKo() {
    AlterChargeTheme(darkTheme = true) {
        SearchBar(
            onValueChange = {},
            onMicClick = {}
        )
    }
}

@Preview(name = "SearchBar(Light/EN)", locale = "en")
@Composable
private fun SearchBarPreviewLightEn() {
    AlterChargeTheme() {
        SearchBar(
            onValueChange = {},
            onMicClick = {}
        )
    }
}

@Preview(name = "SearchBar(Dark/EN)", locale = "en")
@Composable
private fun SearchBarPreviewDarkEn() {
    AlterChargeTheme(darkTheme = true) {
        SearchBar(
            onValueChange = {},
            onMicClick = {}
        )
    }
}
