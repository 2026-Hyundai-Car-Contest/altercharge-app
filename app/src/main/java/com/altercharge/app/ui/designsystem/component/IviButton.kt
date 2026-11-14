package com.altercharge.app.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterColors
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.AlterTypography

/** 화면(카드)마다 Primary는 하나만 쓴다 */
enum class IviButtonHierarchy { Primary, Contrast, Secondary, Tonal, Ghost, Danger }

/** Figma button/{large, medium, small} */
enum class IviButtonSize(
    val height: Dp,
    val paddingHorizontal: Dp,
    val gap: Dp,
    val radius: Dp,
    val iconSize: Dp,
) {
    Large(height = 100.dp, paddingHorizontal = 32.dp, gap = 12.dp, radius = 24.dp, iconSize = 36.dp),
    Medium(height = 80.dp, paddingHorizontal = 24.dp, gap = 12.dp, radius = 20.dp, iconSize = 32.dp),
    Small(height = 64.dp, paddingHorizontal = 16.dp, gap = 8.dp, radius = 16.dp, iconSize = 28.dp),

    /** 카드 안 버튼. 시안이 Small을 52 높이 · 좌우 20으로 줄여 쓴다 (card_station button_navigate) */
    Compact(height = 52.dp, paddingHorizontal = 20.dp, gap = 8.dp, radius = 16.dp, iconSize = 24.dp),
}

/** button/touch_target/min */
private val TouchTargetMin = 76.dp

/**
 * Figma ivi_button (366:38848). Small(64)도 터치 영역 76을 확보해 레이아웃 높이가 76이다.
 *
 * @param text null이면 아이콘만 있는 버튼이다. 이때 [contentDescription]을 꼭 넣는다.
 * @param restricted 주행 중 잠금. 사유 문구는 버튼 밖에 따로 둔다.
 */
@Composable
fun IviButton(
    text: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hierarchy: IviButtonHierarchy = IviButtonHierarchy.Primary,
    size: IviButtonSize = IviButtonSize.Medium,
    enabled: Boolean = true,
    restricted: Boolean = false,
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
    contentDescription: String? = text,
    fullWidth: Boolean = false,
) {
    val palette = iviButtonPalette(hierarchy, AlterTheme.colors)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    // 포커스 링은 로터리 · 키보드로 조작할 때만. 터치 중에는 포커스가 가 있어도 그리지 않는다
    val keyboardInput = LocalInputModeManager.current.inputMode == InputMode.Keyboard
    val iconOnly = text == null
    val isGhost = hierarchy == IviButtonHierarchy.Ghost

    val container = when {
        restricted -> if (isGhost) Color.Transparent else palette.restrictedContainer
        !enabled -> if (isGhost) Color.Transparent else palette.disabledContainer
        pressed -> palette.pressedContainer
        else -> palette.container
    }
    val content = when {
        restricted -> palette.restrictedLabel
        !enabled -> palette.disabledLabel
        else -> palette.label
    }
    val shape = RoundedCornerShape(size.radius)
    val lockPainter = rememberVectorPainter(Icons.Filled.Lock)
    val leading = if (restricted) lockPainter else leadingIcon

    Box(
        modifier = modifier
            .heightIn(min = TouchTargetMin)
            .then(if (iconOnly) Modifier.widthIn(min = TouchTargetMin) else Modifier)
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .then(
                if (iconOnly && contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled && !restricted,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(size.height)
                .then(if (iconOnly) Modifier.width(size.height) else Modifier)
                .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                .focusRing(visible = focused && keyboardInput, color = palette.focusRing, radius = size.radius)
                .background(container, shape)
                .then(
                    // Disabled · Restricted Ghost는 배경도 테두리도 없다 (Figma 366:36970)
                    if (isGhost && !pressed && enabled && !restricted) {
                        Modifier.border(GhostOutlineWidth, palette.outline, shape)
                    } else {
                        Modifier
                    },
                )
                // Ghost는 외곽선 두께만큼 폭이 더 넓다 (Figma 인스턴스 폭 253 → 256)
                .padding(
                    horizontal = if (iconOnly) 0.dp else size.paddingHorizontal + (if (isGhost) GhostOutlineWidth else 0.dp),
                ),
            horizontalArrangement = Arrangement.spacedBy(size.gap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.let { Icon(it, contentDescription = null, tint = content, modifier = Modifier.size(size.iconSize)) }
            text?.let {
                Text(
                    text = it,
                    style = size.textStyle(AlterTheme.typography),
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!restricted) {
                trailingIcon?.let {
                    Icon(it, contentDescription = null, tint = content, modifier = Modifier.size(size.iconSize - TrailingIconInset))
                }
            }
        }
    }
}

/** 누른 동안에는 테두리 없음 (디자이너 확정) */
private val GhostOutlineWidth = 1.5.dp

/** button/focus/ring_width, ring_offset */
private val FocusRingWidth = 4.dp
private val FocusRingOffset = 4.dp

/** Figma trailing_icon: 32 · 28 · 24 */
private val TrailingIconInset = 4.dp

/**
 * 디자이너 확정(2026-11-11). 파란 배경에서 안쪽 테두리는 안 보이고 라벨 공간도 줄어 바깥에 그린다.
 * 그래서 부모가 잘라내면(clip, 스크롤) 안 보일 수 있다.
 */
private fun Modifier.focusRing(visible: Boolean, color: Color, radius: Dp): Modifier =
    if (!visible) {
        this
    } else {
        drawBehind {
            val stroke = FocusRingWidth.toPx()
            val inset = FocusRingOffset.toPx() + stroke / 2
            drawRoundRect(
                color = color,
                topLeft = Offset(-inset, -inset),
                size = Size(size.width + inset * 2, size.height + inset * 2),
                cornerRadius = CornerRadius(radius.toPx() + inset),
                style = Stroke(stroke),
            )
        }
    }

private fun IviButtonSize.textStyle(type: AlterTypography): TextStyle = when (this) {
    IviButtonSize.Large -> type.titleSmall
    IviButtonSize.Medium -> type.bodyMedium
    IviButtonSize.Small, IviButtonSize.Compact -> type.bodySmall
}

private data class IviButtonPalette(
    val container: Color,
    val pressedContainer: Color,
    val label: Color,
    val outline: Color = Color.Transparent,
    val disabledContainer: Color,
    val disabledLabel: Color,
    val restrictedContainer: Color,
    val restrictedLabel: Color,
    val focusRing: Color,
)

/** Figma 05 Components › button 변수 라이트(366:38848) · 다크(439:16558). Ghost 누름 배경은 디자이너 확정 */
private fun iviButtonPalette(hierarchy: IviButtonHierarchy, colors: AlterColors): IviButtonPalette {
    val isDark = colors.isDark
    // 토큰에 없는 버튼 전용 색만 숫자로 둔다
    val disabledContainer = colors.buttonDisabledContainer
    fun palette(container: Color, pressed: Color, label: Color, outline: Color = Color.Transparent) = IviButtonPalette(
        container = container,
        pressedContainer = pressed,
        label = label,
        outline = outline,
        disabledContainer = disabledContainer,
        disabledLabel = colors.buttonDisabledLabel,
        restrictedContainer = disabledContainer,
        restrictedLabel = colors.textTertiary,
        focusRing = colors.blue700,
    )
    return when (hierarchy) {
        IviButtonHierarchy.Primary -> palette(colors.blue500, colors.blue600, Color.White)
        // 눌림은 반대 모드의 surfaceHigh (버튼 전용)
        IviButtonHierarchy.Contrast -> palette(
            colors.buttonContrastContainer,
            if (isDark) Color(0xFFEDEEF2) else Color(0xFF313236),
            colors.buttonContrastLabel,
        )
        IviButtonHierarchy.Secondary -> palette(colors.surfaceHigh, colors.surfaceAccent, colors.textPrimary)
        IviButtonHierarchy.Tonal -> palette(colors.blue50, colors.blue100, colors.textPrimary)
        IviButtonHierarchy.Ghost -> palette(
            Color.Transparent,
            colors.surfaceHigh,
            colors.blue700,
            outline = colors.gray300,
        )
        IviButtonHierarchy.Danger -> palette(colors.negative, if (isDark) Color(0xFFDF4323) else Color(0xFFE73612), Color.White)
    }
}
