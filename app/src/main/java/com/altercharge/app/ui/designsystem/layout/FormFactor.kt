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
    /**
     * 가로에서 L2를 펼쳤을 때 왼쪽 열 스크롤을 허용하는지.
     * S-17은 하이파이 상황 15가 왼쪽 열만 스크롤한다. WF는 Layout Rules가 "스크롤 없음"이다.
     */
    val landscapeLeftScrollable: Boolean = true,
) {
    S17(
        landscape = AreaSpec(1700.dp, 1184.dp, margin = 48.dp, gutter = 24.dp, vertical = 32.dp, columns = 12),
        portrait = AreaSpec(840.dp, 1184.dp, margin = 32.dp, gutter = 20.dp, vertical = 32.dp, columns = 4),
    ),

    // 디자이너 확정: pleos Layout › layout/connect_w_standard. W-Extended와 app_padding(위아래 여백)만 다르다.
    W_STANDARD(
        landscape = AreaSpec(1640.dp, 810.dp, margin = 40.dp, gutter = 20.dp, vertical = 24.dp, columns = 12),
        portrait = AreaSpec(800.dp, 810.dp, margin = 32.dp, gutter = 20.dp, vertical = 24.dp, columns = 4),
    ),
    W_EXTENDED(
        landscape = AreaSpec(1640.dp, 1400.dp, margin = 40.dp, gutter = 20.dp, vertical = 32.dp, columns = 12),
        portrait = AreaSpec(800.dp, 1400.dp, margin = 32.dp, gutter = 20.dp, vertical = 32.dp, columns = 4),
    ),
    WF(
        landscape = AreaSpec(1256.dp, 800.dp, margin = 32.dp, gutter = 16.dp, vertical = 24.dp, columns = 12),
        // 디자이너 확정(Q&A Q4): pleos Layout › layout/connect_wf/portrait/* columns 4 · margin 32 · gutter 16 · app_padding 24.
        // 폭 648은 화면 altercharge/home/18_5_waterfall/portrait (452:54321). 카드 폭 648 - 32 × 2 = 584.
        portrait = AreaSpec(648.dp, 800.dp, margin = 32.dp, gutter = 16.dp, vertical = 24.dp, columns = 4),
        // WF 가로 (Layout Rules): L 4 · C 4 · R 4, 스크롤 없음.
        // TODO 카드 작업: WF 가로는 L2 미니 카드 제외 · 충전소 목록 2개. 틀은 슬롯만 두고 카드 쪽에서 처리한다.
        landscapeSpans = ColumnSpans(left = 4, center = 4, right = 4),
        landscapeLeftScrollable = false,
    ),
    ;

    fun spec(isLandscape: Boolean): AreaSpec = if (isLandscape) landscape else portrait

    companion object {
        /**
         * 지금 앱 영역 크기와 가장 가까운 폼팩터를 고른다.
         * width · height는 기기 화면 크기가 아니라 앱이 실제로 받은 영역 크기다
         * (차량 시스템 바 · 분할 화면을 뺀 값, 예: BoxWithConstraints의 maxWidth · maxHeight).
         */
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
