package com.altercharge.app.ui.designsystem.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 앱 영역 한 방향(가로 또는 세로)의 그리드 값.
 * Figma: Wireflows · 01 주차 · Layout Rules (103:63202), Form factors 표 (420:288492)
 */
data class AreaSpec(
    val width: Dp,
    val height: Dp,
    /** 좌우 바깥 여백 */
    val margin: Dp,
    /** 칸 사이 간격 */
    val gutter: Dp,
    /** 위아래 여백 */
    val vertical: Dp,
    val columns: Int,
)

/** 가로 화면에서 L · C · R 열이 차지하는 컬럼 수. */
data class ColumnSpans(val left: Int, val center: Int, val right: Int)

private val Spans363 = ColumnSpans(left = 3, center = 6, right = 3)

/**
 * 하이파이 시안이 있는 폼팩터.
 * 가로 블록 배치는 S-17 · W-Extended가 L 3 · C 6 · R 3, WF가 L 4 · C 4 · R 4 컬럼이다.
 */
enum class FormFactor(
    val landscape: AreaSpec,
    val portrait: AreaSpec,
    val landscapeSpans: ColumnSpans = Spans363,
) {
    S17(
        landscape = AreaSpec(1700.dp, 1184.dp, margin = 48.dp, gutter = 24.dp, vertical = 32.dp, columns = 12),
        portrait = AreaSpec(840.dp, 1184.dp, margin = 32.dp, gutter = 20.dp, vertical = 32.dp, columns = 4),
    ),

    // TODO 디자이너 확인: Layout Rules에 W-Standard 여백 값이 없어서 W-Extended 값을 임시로 쓴다.
    W_STANDARD(
        landscape = AreaSpec(1640.dp, 810.dp, margin = 40.dp, gutter = 20.dp, vertical = 32.dp, columns = 12),
        portrait = AreaSpec(800.dp, 810.dp, margin = 32.dp, gutter = 20.dp, vertical = 32.dp, columns = 4),
    ),
    W_EXTENDED(
        landscape = AreaSpec(1640.dp, 1400.dp, margin = 40.dp, gutter = 20.dp, vertical = 32.dp, columns = 12),
        portrait = AreaSpec(800.dp, 1400.dp, margin = 32.dp, gutter = 20.dp, vertical = 32.dp, columns = 4),
    ),
    WF(
        landscape = AreaSpec(1256.dp, 800.dp, margin = 32.dp, gutter = 16.dp, vertical = 24.dp, columns = 12),
        // 세로는 시안에 "세로 없음 · 참고"로만 있다.
        portrait = AreaSpec(628.dp, 800.dp, margin = 24.dp, gutter = 16.dp, vertical = 24.dp, columns = 4),
        // TODO 서희님: WF 가로는 스크롤 없음 · L2 미니 카드 제외 · 충전소 2개 (Layout Rules)
        landscapeSpans = ColumnSpans(left = 4, center = 4, right = 4),
    ),
    ;

    fun spec(isLandscape: Boolean): AreaSpec = if (isLandscape) landscape else portrait

    companion object {
        /** 지금 앱 영역 크기와 가장 가까운 폼팩터를 고른다. */
        fun closestTo(width: Dp, height: Dp): FormFactor {
            val isLandscape = width >= height
            return entries.minBy { formFactor ->
                val spec = formFactor.spec(isLandscape)
                val dw = (spec.width - width).value
                val dh = (spec.height - height).value
                dw * dw + dh * dh
            }
        }
    }
}
