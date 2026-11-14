package com.altercharge.app.ui.common.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** 필터 줄이 화면에서 차지하는 자리(px). 드롭다운을 패널 폭에 맞추고 남은 높이를 재는 데 쓴다 */
data class FilterRowBounds(val left: Int, val width: Int, val bottom: Int)

/**
 * 필터 칩 아래 8에 여는 팝업 (Figma 필터 드롭다운 · 디자이너 QA 04 · 06).
 * Material DropdownMenu는 아래 공간이 모자라면 위로 뒤집혀 칩 줄과 R1을 덮어서 따로 둔다.
 * [panel]을 넘기면 패널 왼쪽 끝에 맞추고 폭도 패널 폭으로 잡아 패널 밖으로 넘치지 않는다.
 * 높이는 화면 아래까지 남은 만큼만 쓰고, 넘치는 내용은 안에서 스크롤한다([DropDownWrapper]).
 */
@Composable
fun FilterDropdownPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    panel: FilterRowBounds? = null,
    content: @Composable () -> Unit,
) {
    if (!expanded) return
    val density = LocalDensity.current
    val gap = with(density) { GAP.roundToPx() }
    val windowHeight = LocalView.current.rootView.height
    val sizeModifier = if (panel != null) {
        with(density) {
            val maxHeight = (windowHeight - panel.bottom - gap - BOTTOM_MARGIN.roundToPx()).coerceAtLeast(MIN_HEIGHT.roundToPx())
            Modifier.width(panel.width.toDp()).heightIn(max = maxHeight.toDp())
        }
    } else {
        Modifier
    }
    Popup(
        popupPositionProvider = BelowAnchorPosition(gap, panel?.left),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Box(
            modifier = sizeModifier
                .clip(RoundedCornerShape(AlterRadius.Large))
                .background(AlterTheme.colors.containerBackgroundSubtle),
        ) {
            content()
        }
    }
}

private class BelowAnchorPosition(private val gap: Int, private val left: Int?) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = (left ?: anchorBounds.left).coerceAtMost(windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val y = (anchorBounds.bottom + gap).coerceAtMost(windowSize.height - popupContentSize.height).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

private val GAP = 8.dp
private val BOTTOM_MARGIN = 16.dp
private val MIN_HEIGHT = 200.dp
