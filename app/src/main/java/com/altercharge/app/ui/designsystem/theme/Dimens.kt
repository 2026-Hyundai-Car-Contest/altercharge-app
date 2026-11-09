package com.altercharge.app.ui.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * 모서리 · 바깥 간격 · 안쪽 여백. Figma Size, space & Radius (103:46835, 변수 02 Dimensions).
 * 이름 규칙은 Figma와 같다: xxsmall < xsmall < small < normal < medium < large < xlarge < xxlarge
 */
object AlterRadius {
    val None = 0.dp
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val XLarge = 20.dp
    val XXLarge = 24.dp

    /** 캡슐 모양 */
    val Full = 999.dp
}

/** 요소 사이 간격 (Auto Layout Gap) */
object AlterMargin {
    val XXSmall = 4.dp
    val XSmall = 8.dp
    val Small = 12.dp
    val Normal = 16.dp
    val Medium = 20.dp
    val Large = 24.dp
    val XLarge = 32.dp
    val XXLarge = 48.dp
}

/** 컨테이너 안쪽 여백 (Auto Layout Padding) */
object AlterPadding {
    val XXSmall = 4.dp
    val XSmall = 8.dp

    /** 4px 단위 예외 */
    val Small = 10.dp
    val Normal = 16.dp
    val Medium = 20.dp
    val Large = 24.dp
    val XLarge = 32.dp
}
