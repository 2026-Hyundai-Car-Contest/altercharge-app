/**
 * C 카드 SOC 강조색과 배터리 게이지 칸 수. 상황(VehicleSituation)으로 색을 고르고, SOC로 채울 칸 수를 정한다.
 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.ui.graphics.Color
import com.altercharge.app.ui.designsystem.theme.AlterColors
import kotlin.math.ceil

/**
 * SOC 숫자 · 게이지 칸 색. 배터리 구간 표(377:11454)와 상황별 시안 기준.
 * - 기본 blue_600, 부족 · 일시정지 regulation_orange, 위험 · 방전 regulation_red, 완료 informative_active
 * - 오류 · 정보 없음은 값이 없어 비활성 색
 *
 * TODO 디자이너 확인: battery_gauge 설명은 칸마다 다른 색(text_dark_primary → blue_700 → blue_500 → blue_200)인데,
 * 상황별 시안은 모든 칸이 상태 색 하나라 시안을 따랐다.
 */
internal fun accentColor(situation: VehicleSituation, colors: AlterColors): Color = when (situation) {
    // 07 일시정지는 SOC와 상관없이 주황이다 (상황 보드 07 가로 455:63201: 54%가 regulation_orange #FF8A00)
    VehicleSituation.CHARGE_RECOMMENDED,
    VehicleSituation.CHARGING_LOW,
    VehicleSituation.PAUSED,
    -> colors.warning
    VehicleSituation.CHARGING_CRITICAL,
    VehicleSituation.CRITICAL_UNPLUGGED,
    VehicleSituation.DEPLETED,
    -> colors.danger
    VehicleSituation.CHARGED -> colors.positive
    // Figma fault · unknown variant(371:9467 · 371:9645)의 soc_value "--"는 basic_300 (라이트 #C7C9CE · 다크 #686A72)
    VehicleSituation.CHARGE_ERROR, VehicleSituation.NO_DATA -> colors.gray300
    VehicleSituation.READY, VehicleSituation.CHARGING, VehicleSituation.SCHEDULED -> colors.blue600
}

/** level = ceil(SOC ÷ 25), 0~4칸. 값을 모르면 0칸. */
internal fun gaugeLevel(socPercent: Int?): Int {
    if (socPercent == null) return 0
    return ceil(socPercent.coerceIn(0, MAX_SOC) / SOC_PER_CELL).toInt().coerceIn(0, GaugeCells)
}

/**
 * 세로 카드 % 단위 색 (card_vehicle_status/soc/unit = blue_300: 라이트 #8DABED · 다크 #243F79, EN 다크 07 세로 457:78886).
 * 상태 색을 따라가지 않는다: 위험 · 빨강 숫자일 때도 시안 %는 이 색이다 (세로 05 455:61892, landscape_tall 미연결 366:37704).
 */
internal fun portraitSocUnitColor(colors: AlterColors): Color = colors.blue300

private const val SOC_PER_CELL = 25.0
internal const val MAX_SOC = 100
