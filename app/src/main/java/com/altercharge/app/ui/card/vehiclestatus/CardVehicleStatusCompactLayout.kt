/** C 카드 W-Standard 세로 배치. 패널이 스테이지 옆에 있어 StageWithPanel을 쓰지 않는다 */
package com.altercharge.app.ui.card.vehiclestatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** 598:57477 충전 중 · 598:57512 미연결 */
@Composable
internal fun CompactLayout(
    state: CardVehicleStatusUiState,
    content: CardContent,
    vehicleImage: @Composable (VehicleImageKind, Modifier) -> Unit,
) {
    val typography = AlterTheme.typography
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(AlterMargin.Small),
        ) {
            AutoShrinkHeadline(
                text = content.portraitHeadline,
                styles = listOf(typography.headlineSmall, typography.titleLarge),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AlterPadding.Large, end = AlterPadding.XSmall, top = AlterMargin.Small),
            )
            GlassPanel(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = AlterPadding.Medium,
                verticalPadding = AlterPadding.Normal,
                glassAlpha = FlatStageGlassAlpha,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    BatteryValue(
                        state = state,
                        content = content,
                        labelStyle = typography.labelMedium,
                        socStyle = compactSocStyle(typography.numericHero),
                    )
                    // 미연결 variant(598:57512)는 range 문구를 숨기고 게이지만 SOC 줄 아래에 맞춘다
                    BatteryDetail(
                        content = content,
                        statusStyle = typography.titleSmall,
                        horizontalAlignment = Alignment.End,
                        gap = AlterMargin.XSmall,
                        showStatus = state.charging != ChargingState.UNPLUGGED,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .width(CompactStageWidth)
                .fillMaxHeight(),
        ) {
            Stage(chip = state.chip) {
                StageVehicle(spec = CompactStage, vehicleImage = vehicleImage)
            }
        }
    }
}

private val CompactStageWidth = 300.dp

/** portrait_compact (두 variant 같음): 원 240 세로 가운데 · 오른쪽 여백 30, 차 242×153(-90도) 가운데 */
private val CompactStage = StageImageSpec(
    backdropSize = 240.dp,
    backdropAlignment = Alignment.CenterEnd,
    backdropOffsetX = (-30).dp,
    imageWidth = 242.dp,
    imageHeight = 153.dp,
)

/** 56은 디자인 값에 없는 크기 */
private fun compactSocStyle(base: TextStyle): TextStyle = base.copy(fontSize = 56.sp, lineHeight = 84.sp)
