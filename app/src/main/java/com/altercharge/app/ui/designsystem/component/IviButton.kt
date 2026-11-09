package com.altercharge.app.ui.designsystem.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class IviButtonStyle { Primary, Secondary }

/**
 * 차량용 기본 버튼. Figma ivi_button (366:38848).
 *
 * 지금은 뼈대다. 모양 · 색 · 누름 · 비활성 · 주행 중 잠금은 디자인 값 작업 때 채운다.
 * 함수 이름과 파라미터는 그대로 두니 카드에서 먼저 가져다 써도 된다.
 */
@Composable
fun IviButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IviButtonStyle = IviButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val colors = when (style) {
        IviButtonStyle.Primary -> ButtonDefaults.buttonColors()
        IviButtonStyle.Secondary -> ButtonDefaults.filledTonalButtonColors()
    }
    Button(
        onClick = onClick,
        // 차량 화면 최소 터치 영역 76dp (Figma 기준)
        modifier = modifier.heightIn(min = 76.dp),
        enabled = enabled,
        colors = colors,
    ) {
        Text(text)
    }
}
