package com.altercharge.app.ui.designsystem.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Figma Layout Rules (103:63202), Form factors 표 (420:288492) */
data class AreaSpec(
    val width: Dp,
    val height: Dp,
    val margin: Dp,
    val gutter: Dp,
    val vertical: Dp,
    val columns: Int,
)

data class ColumnSpans(val left: Int, val center: Int, val right: Int)

private val Spans363 = ColumnSpans(left = 3, center = 6, right = 3)

/** 하이파이 시안이 있는 폼팩터 */
enum class FormFactor(
    val landscape: AreaSpec,
    val portrait: AreaSpec,
    val landscapeSpans: ColumnSpans = Spans363,
    /** S-17은 상황 15에서 왼쪽 열만 스크롤한다. WF는 Layout Rules가 "스크롤 없음"이다 */
    val landscapeLeftScrollable: Boolean = true,
) {
    S17(
        landscape = AreaSpec(1700.dp, 1184.dp, margin = 48.dp, gutter = 24.dp, vertical = 32.dp, columns = 12),
        portrait = AreaSpec(840.dp, 1184.dp, margin = 32.dp, gutter = 20.dp, vertical = 32.dp, columns = 4),
    ),

    // 디자이너 확정: pleos Layout › layout/connect_w_standard
    W_STANDARD(
        landscape = AreaSpec(1640.dp, 810.dp, margin = 40.dp, gutter = 20.dp, vertical = 24.dp, columns = 12),
        // 세로 홈 배치는 하이파이 화면(452:53649) 값을 따른다 (PortraitSpec)
        portrait = AreaSpec(800.dp, 810.dp, margin = 32.dp, gutter = 20.dp, vertical = 24.dp, columns = 4),
    ),
    W_EXTENDED(
        landscape = AreaSpec(1640.dp, 1400.dp, margin = 40.dp, gutter = 20.dp, vertical = 32.dp, columns = 12),
        portrait = AreaSpec(800.dp, 1400.dp, margin = 32.dp, gutter = 20.dp, vertical = 32.dp, columns = 4),
    ),
    WF(
        landscape = AreaSpec(1256.dp, 800.dp, margin = 32.dp, gutter = 16.dp, vertical = 24.dp, columns = 12),
        // 디자이너 확정(Q&A Q4): pleos layout/connect_wf/portrait. 폭 648은 화면 452:54321.
        // 세로 홈 배치는 하이파이 화면 값을 따른다 (PortraitSpec)
        portrait = AreaSpec(648.dp, 800.dp, margin = 32.dp, gutter = 16.dp, vertical = 24.dp, columns = 4),
        // TODO 카드 작업: WF 가로는 L2 미니 카드 제외 · 충전소 목록 2개. 틀은 슬롯만 두고 카드 쪽에서 처리한다.
        landscapeSpans = ColumnSpans(left = 4, center = 4, right = 4),
        landscapeLeftScrollable = false,
    ),
    ;

    fun spec(isLandscape: Boolean): AreaSpec = if (isLandscape) landscape else portrait

    companion object {
        /** width · height는 기기 화면이 아니라 앱이 받은 영역 크기다 (시스템 바 · 분할 화면을 뺀 값) */
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
