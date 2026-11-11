/**
 * L2 원형 아이콘 버튼(button_period_nav · button_expand)과 터치 여백만큼 바깥으로 번지게 놓는 Modifier.
 */
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
 * 48 원형 버튼. 레이아웃에서 누르는 영역(기본 76 = 원 + 사방 14)을 그대로 차지하고, 보이는 원만 48로 그린다.
 * 시안의 원 자리에 맞추려면 부르는 쪽에서 [bleedTouchInset]으로 터치 여백만큼 바깥으로 번지게 놓는다.
 * 비활성이면 바탕과 그림자 없이 흐린 화살표만 보인다.
 *
 * @param touchPadding 원 바깥 터치 여백. 버튼이 나란히 있으면 맞닿는 쪽을 간격 절반으로 줄여 겹치지 않게 한다.
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
                // 물결은 원에 그린다 (터치 영역이 한쪽으로 치우쳐도 원 가운데에서 퍼지게)
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
 * 버튼(묶음)의 터치 여백만큼 사방으로 작게 보고하고 그만큼 바깥으로 옮겨 그린다.
 * 그래서 원의 화면 위치와 줄 높이는 시안(48 기준) 그대로다.
 * 번진 14는 카드 안쪽 여백(그래프 패널 20, 요약 오른쪽 16 + 카드 8, 요약 위 간격 20) 위에 놓여
 * 잘리지 않고 터치도 받는다. Compose는 부모 영역 밖이라도 잘리지(clip) 않은 자식에 터치를 전달한다.
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
