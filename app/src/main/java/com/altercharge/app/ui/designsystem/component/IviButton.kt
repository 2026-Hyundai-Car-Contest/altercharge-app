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

/** 버튼 위계. 화면(카드)마다 Primary는 하나만 쓴다. */
enum class IviButtonHierarchy { Primary, Contrast, Secondary, Tonal, Ghost, Danger }

/** 버튼 크기. Figma button/{large, medium, small} */
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
}

/** 차량 화면 최소 터치 영역 (button/touch_target/min) */
private val TouchTargetMin = 76.dp

/**
 * 차량용 기본 버튼. Figma ivi_button (366:38848).
 *
 * - 누름: 한 단계 진한 색 / 포커스(로터리 · 키보드): 4dp 테두리를 4dp 바깥에 그린다.
 * - [restricted]: 주행 중 잠금. 아이콘이 자물쇠로 바뀌고 눌리지 않는다. 사유 문구는 버튼 밖에 따로 둔다.
 * - Small(64)도 누르는 범위는 76을 확보한다. 그래서 Small은 레이아웃 높이가 76이다.
 *
 * @param text null이면 아이콘만 있는 버튼이다. 이때 [contentDescription]을 꼭 넣는다.
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
    /** true면 버튼 배경을 받은 폭 전체로 채운다 (카드 하단 전폭 버튼 등) */
    fullWidth: Boolean = false,
) {
    val palette = iviButtonPalette(hierarchy, AlterTheme.colors)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    // 포커스 링은 로터리 · 스티어링 · 키보드로 조작할 때만 보인다(Figma Focused 상태).
    // 터치로 쓸 때는 포커스가 자동으로 가 있어도 링을 그리지 않는다.
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
            // 글자가 있으면 글자를 읽고, 아이콘만 있을 때만 설명 문구를 붙인다.
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
                    if (isGhost && !pressed) Modifier.border(GhostOutlineWidth, palette.outline, shape) else Modifier,
                )
                .padding(horizontal = if (iconOnly) 0.dp else size.paddingHorizontal),
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
                    Icon(it, contentDescription = null, tint = content, modifier = Modifier.size(size.iconSize))
                }
            }
        }
    }
}

/** Ghost 테두리: 1.5dp, 안쪽 정렬, basic_300. 누른 동안에는 테두리 없음 (디자이너 확정) */
private val GhostOutlineWidth = 1.5.dp

private fun IviButtonSize.textStyle(type: AlterTypography): TextStyle = when (this) {
    IviButtonSize.Large -> type.titleSmall
    IviButtonSize.Medium -> type.bodyMedium
    IviButtonSize.Small -> type.bodySmall
}

/**
 * 포커스 테두리: 두께 4, 버튼에서 4 바깥 (button/focus/ring_width, ring_offset)
 * 버튼 바깥에 그리므로 부모가 잘라내면(clip, 스크롤) 안 보일 수 있다. 카드 가장자리에 둘 때 여유를 둔다.
 */
private fun Modifier.focusRing(visible: Boolean, color: Color, radius: Dp): Modifier =
    if (!visible) {
        this
    } else {
        drawBehind {
            val stroke = 4.dp.toPx()
            val inset = 4.dp.toPx() + stroke / 2
            drawRoundRect(
                color = color,
                topLeft = Offset(-inset, -inset),
                size = Size(size.width + inset * 2, size.height + inset * 2),
                cornerRadius = CornerRadius(radius.toPx() + inset),
                style = Stroke(stroke),
            )
        }
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

/**
 * 버튼 색. Figma 05 Components › button 변수의 라이트(366:38848) · 다크(439:16558) 값.
 * Ghost 누름 배경은 basic_100 (디자이너 확정).
 */
private fun iviButtonPalette(hierarchy: IviButtonHierarchy, colors: AlterColors): IviButtonPalette {
    val isDark = colors.isDark
    // 디자인 값에 있는 색은 토큰으로 쓴다. 토큰에 없는 버튼 전용 색만 숫자로 둔다.
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
        // 테두리는 basic_300 (버튼 전용 변수, 다크 값 #686A72)
        IviButtonHierarchy.Ghost -> palette(
            Color.Transparent,
            colors.surfaceHigh,
            colors.blue700,
            outline = if (isDark) Color(0xFF686A72) else Color(0xFFC7C9CE),
        )
        IviButtonHierarchy.Danger -> palette(colors.negative, if (isDark) Color(0xFFDF4323) else Color(0xFFE73612), Color.White)
    }
}
