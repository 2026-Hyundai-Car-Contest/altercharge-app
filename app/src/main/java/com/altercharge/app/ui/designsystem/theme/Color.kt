package com.altercharge.app.ui.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 앱 색. Figma 변수 값을 라이트/다크 그대로 옮겼다.
 * - 파란색 팔레트: altercharge / Colors (103:46834, 변수 01 Colors)
 * - 나머지: 15상황 보드의 라이트(455:58776) · 다크(457:78743)에서 카드가 실제로 쓰는 변수
 *
 * 카드 전용 색(예: L1 배경 #020417)은 여기 두지 않고 각 카드에서 정한다.
 * 필요한 값이 없으면 직접 만들지 말고 서희님에게 알린다.
 */
@Immutable
data class AlterColors(
    /** 다크 모드 여부. 컴포넌트가 모드별 전용 색을 고를 때 쓴다. */
    val isDark: Boolean,

    // 파란색 팔레트 (basic/blue/blue_50~900). 500이 기준색
    val blue50: Color,
    val blue100: Color,
    val blue200: Color,
    val blue300: Color,
    val blue400: Color,
    val blue500: Color,
    val blue600: Color,
    val blue700: Color,
    val blue800: Color,
    val blue900: Color,

    // 글자 · 아이콘
    /** 제목, 이름, 값 */
    val textPrimary: Color,
    /** 보조 라벨 */
    val textSecondary: Color,
    /** 설명, 단위, 메타 정보 */
    val textTertiary: Color,
    /** 비활성, 아직 안 온 값 */
    val textDisabled: Color,
    /** 중립 칩 글자, 목록 아이콘 */
    val contentNeutral: Color,
    /** pleos basic/gray/basic_300. C 카드 칩 점(대기 · 오프라인) · 값 없는 SOC "--", 고스트 버튼 테두리 */
    val gray300: Color,

    // 면
    /** 앱 화면 전체 배경. 15상황 보드 화면 프레임 (라이트 basic_100, 다크 surface_low) */
    val screenBackground: Color,
    /** 패널 배경 (주간 주행 기록, 충전소 목록) */
    val background: Color,
    /** 흰 카드 (차량 상태, 충전소 카드) */
    val surface: Color,
    /** 칩 바탕, 빈 게이지 칸, 차트 테두리 */
    val surfaceHigh: Color,
    /** 구분선, 강조 면 */
    val surfaceAccent: Color,
    /** 입력 필드 바탕 */
    val fieldBackground: Color,
    /**
     * 화면 위 카드 면 (05 Components common/container/background → surface_basic).
     * C 차량 상태, 충전소 카드 등. 다크에서 화면 배경보다 한 톤 밝다 (개발자 Q&A Q2)
     */
    val containerBackground: Color,
    /**
     * 화면 위 옅은 카드 면 (common/container/background_subtle → basic_50).
     * L2 주간 주행 기록, 충전소 목록 패널
     */
    val containerBackgroundSubtle: Color,

    // 버튼 (button/contrast, button/common/disabled)
    val buttonContrastContainer: Color,
    val buttonContrastLabel: Color,
    val buttonDisabledContainer: Color,
    val buttonDisabledLabel: Color,

    // 상태 색
    /** 연결됨, 정상 (informative_active) */
    val positive: Color,
    /** 연결 안 됨, 오류 (informative_negative) */
    val negative: Color,
    /** 배터리 위험 (regulation_red) */
    val danger: Color,
    /** 배터리 부족 (regulation_orange) */
    val warning: Color,
)

val LightAlterColors = AlterColors(
    isDark = false,
    blue50 = Color(0xFFF1F5FD),
    blue100 = Color(0xFFDCE5F9),
    blue200 = Color(0xFFB8CBF4),
    blue300 = Color(0xFF8DABED),
    blue400 = Color(0xFF628CE6),
    blue500 = Color(0xFF3B6FE0),
    blue600 = Color(0xFF325FC0),
    blue700 = Color(0xFF2A4FA1),
    blue800 = Color(0xFF213E81),
    blue900 = Color(0xFF182E61),

    textPrimary = Color(0xFF131417),
    textSecondary = Color(0xD6131417),
    textTertiary = Color(0xA3131417),
    textDisabled = Color(0x52131417),
    contentNeutral = Color(0xFF686A72),
    gray300 = Color(0xFFC7C9CE),

    screenBackground = Color(0xFFEDEEF2),
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFEDEEF2),
    surfaceAccent = Color(0xFFE0E1E6),
    fieldBackground = Color(0xFFFFFFFF),
    containerBackground = Color(0xFFFFFFFF),
    containerBackgroundSubtle = Color(0xFFF7F8FA),

    buttonContrastContainer = Color(0xFF131417),
    buttonContrastLabel = Color(0xFFFFFFFF),
    buttonDisabledContainer = Color(0xFFE0E1E6),
    buttonDisabledLabel = Color(0x52131417),

    positive = Color(0xFF02C265),
    negative = Color(0xFFFE3D16),
    danger = Color(0xFFF62E24),
    warning = Color(0xFFFF8A00),
)

val DarkAlterColors = AlterColors(
    isDark = true,
    blue50 = Color(0xFF162036),
    blue100 = Color(0xFF1A294A),
    blue200 = Color(0xFF1E325C),
    blue300 = Color(0xFF243F79),
    blue400 = Color(0xFF2D519E),
    blue500 = Color(0xFF3562C4),
    blue600 = Color(0xFF557DD3),
    blue700 = Color(0xFF799AE3),
    blue800 = Color(0xFF99B3EB),
    blue900 = Color(0xFFB9CBF3),

    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xD6FFFFFF),
    textTertiary = Color(0xA3FFFFFF),
    textDisabled = Color(0x52FFFFFF),
    contentNeutral = Color(0xFFC7C9CE),
    gray300 = Color(0xFF686A72),

    screenBackground = Color(0xFF131417),
    background = Color(0xFF131417),
    surface = Color(0xFF131417),
    surfaceHigh = Color(0xFF313236),
    surfaceAccent = Color(0xFF44464E),
    fieldBackground = Color(0xFF131417),
    containerBackground = Color(0xFF1D1E21),
    containerBackgroundSubtle = Color(0xFF1D1E21),

    buttonContrastContainer = Color(0xFFFFFFFF),
    buttonContrastLabel = Color(0xFF131417),
    buttonDisabledContainer = Color(0xFF44464E),
    buttonDisabledLabel = Color(0x52FFFFFF),

    positive = Color(0xFF00E074),
    negative = Color(0xFFFF4C28),
    danger = Color(0xFFFF4339),
    warning = Color(0xFFFF8A00),
)
