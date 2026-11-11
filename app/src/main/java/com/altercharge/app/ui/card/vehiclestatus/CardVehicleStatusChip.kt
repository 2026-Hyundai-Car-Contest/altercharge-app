/**
 * 스테이지 왼쪽 위 연결 칩(chip_status)과 그 치수 · 점 빛번짐 값.
 */
package com.altercharge.app.ui.card.vehiclestatus

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** 연결 칩 (chip_status). 바깥 고리 + 안쪽 알약. */
@Composable
internal fun ChipStatus(chip: ConnectionChip, modifier: Modifier = Modifier) {
    val colors = AlterTheme.colors
    val (labelRes, dotColor) = when (chip) {
        ConnectionChip.CONNECTED -> R.string.card_vehicle_status_chip_connected to colors.blue500
        ConnectionChip.UNPLUGGED -> R.string.card_vehicle_status_chip_unplugged to colors.negative
        ConnectionChip.ERROR -> R.string.card_vehicle_status_chip_error to colors.negative
        // Figma chip/dot/standby → basic/gray/basic_300. 오프라인 variant(371:9645)의 점도 같은 색
        ConnectionChip.STANDBY -> R.string.card_vehicle_status_chip_standby to colors.gray300
        ConnectionChip.OFFLINE -> R.string.card_vehicle_status_chip_offline to colors.gray300
    }
    val pill = RoundedCornerShape(AlterRadius.Full)
    Box(
        modifier = modifier
            .background(colors.surfaceHigh, pill)
            .padding(ChipRingPadding),
    ) {
        Row(
            modifier = Modifier
                .background(colors.surface, pill)
                .padding(horizontal = AlterPadding.Normal, vertical = AlterPadding.XSmall),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(ChipDotSize)
                    .drawBehind {
                        val glowRadius = size.minDimension / 2 + ChipDotGlowSpread.toPx()
                        val blur = figmaBlurToAndroidRadius(ChipDotGlowBlur.toPx())
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = (if (chip == ConnectionChip.UNPLUGGED) dotColor.copy(alpha = ChipDotGlowAlpha) else ChipDotGlowColor).toArgb()
                            if (blur > 0f) maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
                        }
                        drawIntoCanvas { it.nativeCanvas.drawCircle(center.x, center.y, glowRadius, paint) }
                    }
                    .background(dotColor, CircleShape),
            )
            Text(
                text = stringResource(labelRes),
                style = AlterTheme.typography.labelMedium,
                color = colors.textPrimary,
                maxLines = 1,
            )
        }
    }
}

/** chip/ring_padding, 점 크기 */
private val ChipRingPadding = 8.dp
private val ChipDotSize = 12.dp

/** 칩 점 빛번짐 (dot drop shadow 0 0 blur 6 spread 2, 연결됨 · 대기는 #99C6CF 35%. 라이트 · 다크 같음) */
private val ChipDotGlowSpread = 2.dp
private val ChipDotGlowBlur = 6.dp
private val ChipDotGlowColor = Color(0x5999C6CF)

/** 연결 안 됨(빨강 점)은 빛번짐도 점과 같은 색 35% (Figma 칩 점 SVG) */
private const val ChipDotGlowAlpha = 0.35f
