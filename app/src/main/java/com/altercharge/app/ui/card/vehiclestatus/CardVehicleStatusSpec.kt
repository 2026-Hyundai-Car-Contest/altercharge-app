/**
 * 여러 파일이 같이 쓰는 C 카드 치수 · 색 · 변환 함수 (Figma card_vehicle_status 변수). 디자인 값에 없는 것만 둔다.
 * 한 파일에서만 쓰는 값은 그 파일 아래에 둔다.
 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.ui.designsystem.theme.AlterColors

/**
 * pleos basic/gray/basic_300 (라이트 #C7C9CE · 다크 #686A72). 디자인 값(AlterColors)에 없는 단계라 C 카드 안에서만 쓴다.
 * 칩 점(standby · offline)과 값 없는 SOC "--"가 이 색이다.
 * 공용 팔레트(AlterColors)에 gray300이 생기면 이 확장을 지운다 — 멤버가 확장보다 우선해 조용히 무시된다.
 */
internal val AlterColors.gray300: Color
    get() = if (isDark) Color(0xFF686A72) else Color(0xFFC7C9CE)

/**
 * Figma blur(= 가우스 표준편차 × 2)를 안드로이드 blur 반지름(px)으로 바꾼다.
 * 안드로이드는 반지름 r을 표준편차 0.57735 × r + 0.5로 쓴다.
 */
internal fun figmaBlurToAndroidRadius(blurPx: Float): Float =
    ((blurPx / 2f - 0.5f) / 0.57735f).coerceAtLeast(0f)

/** 상태 패널이 스테이지 위로 겹치는 높이 */
internal val LandscapePanelOverlap = 56.dp
internal val PortraitPanelOverlap = 24.dp

/** 세로 헤드라인 줄 · tall 헤드라인 · tall 타일 줄 좌우 여백 */
internal val WideSidePadding = 40.dp

/** 세로 SOC 숫자 80 (디자인 값에 없는 크기, 굵기 · 자간은 numericHero와 같다) */
internal fun portraitSocStyle(base: TextStyle): TextStyle = base.copy(fontSize = 80.sp, lineHeight = 120.sp)

/** battery_tile/gap. 패널 배터리 묶음(Battery)과 WF 레이아웃이 같이 쓴다 */
internal val BatteryTileGap = 12.dp

/** 배터리 게이지 칸 수. 게이지 그리기(Battery)와 칸 수 계산(Accent)이 같이 쓴다 */
internal const val GaugeCells = 4
