/** C 카드 SOC 강조색과 배터리 게이지 칸 수 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.ui.graphics.Color
import com.altercharge.app.ui.designsystem.theme.AlterColors
import kotlin.math.ceil

/**
 * 배터리 구간 표(377:11454)와 상황별 시안 기준.
 *
 * 시안 메모: battery_gauge 설명은 칸마다 다른 색(text_dark_primary → blue_700 → blue_500 → blue_200)인데,
 * 상황별 시안은 모든 칸이 상태 색 하나라 시안을 따랐다.
 */
internal fun accentColor(situation: VehicleSituation, colors: AlterColors): Color = when (situation) {
    // 07 일시정지는 SOC와 상관없이 주황이다 (455:63201)
    VehicleSituation.CHARGE_RECOMMENDED,
    VehicleSituation.CHARGING_LOW,
    VehicleSituation.PAUSED,
    -> colors.warning
    VehicleSituation.CHARGING_CRITICAL,
    VehicleSituation.CRITICAL_UNPLUGGED,
    VehicleSituation.DEPLETED,
    -> colors.danger
    VehicleSituation.CHARGED -> colors.positive
    // Figma fault · unknown variant(371:9467 · 371:9645)의 soc_value "--"는 basic_300
    VehicleSituation.CHARGE_ERROR, VehicleSituation.NO_DATA -> colors.gray300
    VehicleSituation.READY, VehicleSituation.CHARGING, VehicleSituation.SCHEDULED -> colors.blue600
}

internal fun gaugeLevel(socPercent: Int?): Int {
    if (socPercent == null) return 0
    return ceil(socPercent.coerceIn(0, MAX_SOC) / SOC_PER_CELL).toInt().coerceIn(0, GaugeCells)
}

/**
 * card_vehicle_status/soc/unit = blue_300 (457:78886).
 * 위험 · 빨강 숫자일 때도 시안 %는 이 색이다 (455:61892, 366:37704).
 */
internal fun portraitSocUnitColor(colors: AlterColors): Color = colors.blue300

private const val SOC_PER_CELL = 25.0
internal const val MAX_SOC = 100
