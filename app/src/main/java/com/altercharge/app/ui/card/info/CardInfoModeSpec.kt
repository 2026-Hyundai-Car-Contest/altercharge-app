/** L1 카드 모드별 크기 · 글자 · 번개 자리 */
package com.altercharge.app.ui.card.info

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** 너비 값은 Figma 인스턴스의 고정 너비를 최대 너비로 쓴다 */
internal class ModeSpec(
    val contentPadding: Dp,
    val headerMaxWidth: Dp,
    val placeMaxWidth: Dp,
    val isWf: Boolean,
    /**
     * 왼쪽 x 고정이라 카드 폭이 좁으면 오른쪽이 더 잘린다
     * (가로 450 · 383 인스턴스 모두 bolt_track x 130, WF 세로 580 · 584 모두 x 319).
     */
    val boltLeft: Dp,
    val boltTop: Dp,
    val boltWidth: Dp,
    val boltHeight: Dp,
    /** Figma 판정별 variant의 bolt_track x 차이 */
    val tomorrowShift: Dp,
    /** Figma status=3days variant */
    val threeDaysShift: Dp,
    /** Figma bolt_track 상자 그대로. 수집 중 번개를 모드 크기에 맞출 때 쓴다 */
    val trackLeft: Dp,
    val trackTop: Dp,
    val trackWidth: Dp,
    val trackHeight: Dp,
) {
    @Composable
    fun eyebrowStyle(): TextStyle =
        if (isWf) AlterTheme.typography.labelSmall else AlterTheme.typography.labelMedium

    @Composable
    fun titleStyle(): TextStyle =
        (if (isWf) AlterTheme.typography.titleLarge else AlterTheme.typography.headlineSmall)
            .copy(fontWeight = AlterFontWeight.Strong)

    @Composable
    fun placeStyle(): TextStyle =
        if (isWf) AlterTheme.typography.bodySmall else AlterTheme.typography.bodyMedium
}

// 번개 값은 Figma mode별 인스턴스(366:38751 · 38755 · 38759 · 38763) bolt_track에서
// 그림이 차지하는 영역(상자 안쪽 위아래 8.87% · 좌우 4.95%)이다. boltLeft = bolt_track x + 상자 폭 × 4.95%
private val LandscapeSpec = ModeSpec(
    AlterPadding.Large, 252.dp, 279.dp, isWf = false,
    boltLeft = 151.0.dp, boltTop = (-9.7).dp, boltWidth = 381.9.dp, boltHeight = 387.4.dp,
    tomorrowShift = 11.0.dp, threeDaysShift = 68.0.dp,
    trackLeft = 130.0.dp, trackTop = (-51.5).dp, trackWidth = 423.9.dp, trackHeight = 471.0.dp,
)
// 제목 영역 232 (Q&A Q5: 212 → 232, 영문 "Tomorrow" 213이 잘리지 않게)
private val PortraitSpec = ModeSpec(
    AlterPadding.Normal, 232.dp, 279.dp, isWf = false,
    boltLeft = 133.4.dp, boltTop = (-9.4).dp, boltWidth = 371.6.dp, boltHeight = 376.9.dp,
    tomorrowShift = 0.dp, threeDaysShift = 53.3.dp,
    trackLeft = 113.0.dp, trackTop = (-50.0).dp, trackWidth = 412.4.dp, trackHeight = 458.2.dp,
)
private val WfLandscapeSpec = ModeSpec(
    AlterPadding.Normal, 217.dp, 262.dp, isWf = true,
    boltLeft = 144.1.dp, boltTop = (-7.9).dp, boltWidth = 311.4.dp, boltHeight = 315.9.dp,
    tomorrowShift = 10.0.dp, threeDaysShift = 43.3.dp,
    trackLeft = 127.0.dp, trackTop = (-42.0).dp, trackWidth = 345.6.dp, trackHeight = 384.0.dp,
)
// 카드 폭 584(Q&A Q4, 452:54383)여도 bolt_track x 319 그대로
private val WfPortraitSpec = ModeSpec(
    AlterPadding.Normal, 290.dp, 262.dp, isWf = true,
    boltLeft = 333.8.dp, boltTop = (-6.9).dp, boltWidth = 269.8.dp, boltHeight = 273.8.dp,
    tomorrowShift = 0.dp, threeDaysShift = 0.dp,
    trackLeft = 319.0.dp, trackTop = (-36.4).dp, trackWidth = 299.5.dp, trackHeight = 332.8.dp,
)

internal fun CardInfoMode.spec(): ModeSpec = when (this) {
    CardInfoMode.LANDSCAPE -> LandscapeSpec
    CardInfoMode.PORTRAIT -> PortraitSpec
    CardInfoMode.WF_LANDSCAPE -> WfLandscapeSpec
    CardInfoMode.WF_PORTRAIT -> WfPortraitSpec
}
