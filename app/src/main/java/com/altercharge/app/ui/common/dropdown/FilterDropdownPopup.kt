package com.altercharge.app.ui.common.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
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

/**
 * 필터 칩 아래에 여는 팝업 (Figma 필터 드롭다운: 칩 줄 바로 아래에서 목록을 덮으며 열린다).
 * Material DropdownMenu는 아래 공간이 조금만 모자라도 위로 뒤집혀 R1 카드를 덮어서 따로 둔다.
 * 아래가 모자라면 뒤집지 않고 화면 안으로 필요한 만큼만 올린다.
 */
@Composable
fun FilterDropdownPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (!expanded) return
    val gap = with(LocalDensity.current) { GAP.roundToPx() }
    Popup(
        popupPositionProvider = BelowAnchorPosition(gap),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AlterRadius.Large))
                .background(AlterTheme.colors.containerBackgroundSubtle),
        ) {
            content()
        }
    }
}

private class BelowAnchorPosition(private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = anchorBounds.left.coerceAtMost(windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val y = (anchorBounds.bottom + gap).coerceAtMost(windowSize.height - popupContentSize.height).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

private val GAP = 8.dp
