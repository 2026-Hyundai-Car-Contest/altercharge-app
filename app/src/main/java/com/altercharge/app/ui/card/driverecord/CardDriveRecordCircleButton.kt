/** L2 원형 아이콘 버튼 (button_period_nav · button_expand) */
package com.altercharge.app.ui.card.driverecord

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.offset
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.alterShadow

/**
 * 터치 영역 76을 레이아웃에서 그대로 차지하고 원만 48로 그린다. 시안 자리에 맞추려면 [bleedTouchInset]을 쓴다.
 *
 * @param touchPadding 버튼이 나란히 있으면 맞닿는 쪽을 간격 절반으로 줄여 겹치지 않게 한다.
 */
@Composable
internal fun CircleIconButton(
    contentDescription: String,
    iconRotation: Float,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = AlterTheme.colors.textPrimary,
    touchPadding: PaddingValues = PaddingValues(CircleButtonTouchInset),
) {
    val colors = AlterTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clickable(
                interactionSource = interaction,
                // 터치 영역이 치우쳐도 물결이 원 가운데에서 퍼지게 원에 그린다
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription }
            .padding(touchPadding),
        contentAlignment = Alignment.Center,
    ) {
        val circle = if (enabled) {
            Modifier
                .alterShadow(CircleButtonShadow, CircleButtonSize / 2)
                .background(colors.surface, CircleShape)
        } else {
            Modifier
        }
        Box(
            modifier = Modifier
                .size(CircleButtonSize)
                .then(circle)
                .indication(interaction, ripple(bounded = false, radius = CircleButtonSize / 2)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ChevronRight,
                contentDescription = null,
                tint = if (enabled) iconTint else colors.textDisabled,
                modifier = Modifier
                    .size(ButtonIconSize)
                    .rotate(iconRotation),
            )
        }
    }
}

/**
 * 터치 여백만큼 작게 보고하고 바깥으로 옮겨 그려, 원 위치와 줄 높이를 시안(48 기준)에 맞춘다.
 * 번진 14는 카드 안쪽 여백 위에 놓여 잘리지 않고, Compose는 clip되지 않은 자식에 부모 밖 터치도 전달한다.
 */
internal fun Modifier.bleedTouchInset(): Modifier = layout { measurable, constraints ->
    val inset = CircleButtonTouchInset.roundToPx()
    val placeable = measurable.measure(constraints.offset(horizontal = inset * 2, vertical = inset * 2))
    val width = (placeable.width - inset * 2).coerceAtLeast(0)
    val height = (placeable.height - inset * 2).coerceAtLeast(0)
    layout(width, height) {
        placeable.place(-inset, -inset)
    }
}
