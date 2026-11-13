package com.altercharge.app.ui.designsystem.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** x 위치와 퍼짐은 Figma에서 모두 0이다 (shadow/common) */
data class ShadowLayer(val offsetY: Dp, val blur: Dp, val color: Color)

/**
 * Figma Effects (103:46836). strong은 시스템 오버레이 전용이라 넣지 않았다.
 * 다크 모드에서는 그림자 대신 면 색 단계로 높이를 표현한다.
 */
object AlterShadows {
    /** 슬라이더, 드롭다운, 카드 · 컨트롤 */
    val Level1 = listOf(ShadowLayer(offsetY = 6.dp, blur = 24.dp, color = Color(0x14000000)))

    /** 토글 On, 앱 아이콘, 떠 있는 요소 */
    val Level2 = listOf(
        ShadowLayer(offsetY = 8.dp, blur = 32.dp, color = Color(0x29000000)),
        ShadowLayer(offsetY = 2.dp, blur = 2.dp, color = Color(0x14000000)),
    )
}

/** Figma blur는 안드로이드 shadow radius의 약 2배라 절반으로 넣는다 */
fun Modifier.alterShadow(layers: List<ShadowLayer>, cornerRadius: Dp): Modifier = drawBehind {
    val radius = cornerRadius.toPx()
    layers.forEach { layer ->
        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(layer.blur.toPx() / 2f, 0f, layer.offsetY.toPx(), layer.color.toArgb())
            }
            canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, radius, radius, paint)
        }
    }
}
