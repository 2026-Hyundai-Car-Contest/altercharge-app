/** C 카드 여러 파일이 같이 쓰는 값 (Figma card_vehicle_status 변수 중 디자인 값에 없는 것) */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Figma blur = 표준편차 × 2. 안드로이드는 반지름 r을 표준편차 0.57735 × r + 0.5로 쓴다 */
internal fun figmaBlurToAndroidRadius(blurPx: Float): Float =
    ((blurPx / 2f - 0.5f) / 0.57735f).coerceAtLeast(0f)

internal val LandscapePanelOverlap = 56.dp
internal val PortraitPanelOverlap = 24.dp

/** 세로 헤드라인 줄 · tall 헤드라인 · tall 타일 줄 좌우 여백 */
internal val WideSidePadding = 40.dp

/** 80은 디자인 값에 없는 크기 */
internal fun portraitSocStyle(base: TextStyle): TextStyle = base.copy(fontSize = 80.sp, lineHeight = 120.sp)

/** battery_tile/gap */
internal val BatteryTileGap = 12.dp

internal const val GaugeCells = 4
