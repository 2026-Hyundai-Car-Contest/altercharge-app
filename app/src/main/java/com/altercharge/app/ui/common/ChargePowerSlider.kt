package com.altercharge.app.ui.common

import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
fun ChargePowerSlider(
    minPower: Int,
    maxPower: Int,
    onRangeChange: (min: Int, max: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val powerValues = listOf(3, 7, 11, 50, 100, 200, 350)
    val powerLabels = listOf("3", "7", "11", "50", "100", "200", "350+")

    val rawMinIndex = powerValues.indexOf(minPower).coerceAtLeast(0)
    val rawMaxIndex = powerValues.indexOf(maxPower).coerceAtLeast(0)

    val minIndex = minOf(rawMinIndex, rawMaxIndex)
    val maxIndex = maxOf(rawMinIndex, rawMaxIndex)

    val blue = AlterTheme.colors.blue500
    val inactive = AlterTheme.colors.surfaceAccent
    val textColor = AlterTheme.colors.textSecondary.copy(0.84f)

    val latestMinIndex by rememberUpdatedState(minIndex)
    val latestMaxIndex by rememberUpdatedState(maxIndex)
    val latestOnRangeChange by rememberUpdatedState(onRangeChange)

    val density = LocalDensity.current
    val sidePadding = with(density) { 16.dp.toPx() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AlterTheme.colors.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val circleInnerColor = AlterTheme.colors.containerBackground

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .pointerInput(Unit) {
                        var activeThumb = 0

                        fun updateIndex(x: Float) {
                            val availableWidth = size.width - sidePadding * 2
                            val stepWidth = availableWidth / 6f

                            val index = (
                                (x - sidePadding) / stepWidth
                                ).roundToInt().coerceIn(0, 6)

                            val currentMin = latestMinIndex
                            val currentMax = latestMaxIndex

                            if (activeThumb == 0) {
                                val newMin = index.coerceAtMost(currentMax)

                                latestOnRangeChange(
                                    powerValues[newMin],
                                    powerValues[currentMax]
                                )
                            } else {
                                val newMax = index.coerceAtLeast(currentMin)

                                latestOnRangeChange(
                                    powerValues[currentMin],
                                    powerValues[newMax]
                                )
                            }
                        }

                        detectDragGestures(
                            onDragStart = { offset ->
                                val availableWidth = size.width - sidePadding * 2
                                val stepWidth = availableWidth / 6f

                                val minX = sidePadding +
                                    latestMinIndex * stepWidth

                                val maxX = sidePadding +
                                    latestMaxIndex * stepWidth

                                activeThumb = if (
                                    abs(offset.x - minX) <= abs(offset.x - maxX)
                                ) 0 else 1

                                updateIndex(offset.x)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                updateIndex(change.position.x)
                            }
                        )
                    }
            ) {
                val centerY = size.height / 2
                val startX = sidePadding
                val endX = size.width - sidePadding
                val stepWidth = (endX - startX) / 6f

                val minX = startX + minIndex * stepWidth
                val maxX = startX + maxIndex * stepWidth

                drawLine(
                    color = inactive,
                    start = androidx.compose.ui.geometry.Offset(startX, centerY),
                    end = androidx.compose.ui.geometry.Offset(endX, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = blue,
                    start = androidx.compose.ui.geometry.Offset(minX, centerY),
                    end = androidx.compose.ui.geometry.Offset(maxX, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                for (index in 0..6) {
                    val x = startX + index * stepWidth

                    if (index != minIndex && index != maxIndex) {
                        drawCircle(
                            color = if (index in minIndex..maxIndex) {
                                Color.White
                            } else {
                                Color.Gray
                            },
                            radius = 2.5.dp.toPx(),
                            center = Offset(x, centerY)
                        )
                    }
                }

                listOf(minX, maxX).forEach { x ->
                    drawCircle(
                        color = blue,
                        radius = 16.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(x, centerY)
                    )

                    drawCircle(
                        color = circleInnerColor,
                        radius = 13.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(x, centerY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                powerLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 18.sp,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    stringResource(R.string.charge_status_ac) to 3f,
                    stringResource(R.string.charge_status_dc) to 3f,
                    stringResource(R.string.charge_status_ultra_fast) to 0.6f
                ).forEach { (label, weight) ->
                    Column(
                        modifier = Modifier.weight(weight),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalDivider(
                            color = Color(0xFFDADCE0),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 초급속 구간은 폭이 좁아(비율 0.6) 줄바꿈되면 한 글자씩 세로로 쪼개진다: 한 줄로 두고 양옆으로 넘치게 한다
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            color = AlterTheme.colors.textTertiary.copy(0.64f),
                            softWrap = false,
                            maxLines = 1,
                            modifier = Modifier.wrapContentWidth(unbounded = true)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "ChargePowerSlider(Light/KO)", locale = "ko")
@Composable
fun ChargePowerSliderLightKoPreview() {
    AlterChargeTheme() {
        var minPower by remember { mutableIntStateOf(50) }
        var maxPower by remember { mutableIntStateOf(350) }

        ChargePowerSlider(
            minPower = minPower,
            maxPower = maxPower,
            onRangeChange = { min, max ->
                minPower = min
                maxPower = max
            }
        )
    }
}

@Preview(name = "ChargePowerSlider(Dark/KO)", locale = "ko")
@Composable
fun ChargePowerSliderDarkKoPreview() {
    AlterChargeTheme(darkTheme = true) {
        var minPower by remember { mutableIntStateOf(50) }
        var maxPower by remember { mutableIntStateOf(350) }

        ChargePowerSlider(
            minPower = minPower,
            maxPower = maxPower,
            onRangeChange = { min, max ->
                minPower = min
                maxPower = max
            }
        )
    }
}

@Preview(name = "ChargePowerSlider(Light/En)", locale = "en")
@Composable
fun ChargePowerSliderLightEnPreview() {
    AlterChargeTheme() {
        var minPower by remember { mutableIntStateOf(50) }
        var maxPower by remember { mutableIntStateOf(350) }

        ChargePowerSlider(
            minPower = minPower,
            maxPower = maxPower,
            onRangeChange = { min, max ->
                minPower = min
                maxPower = max
            }
        )
    }
}

@Preview(name = "ChargePowerSlider(Dark/KO)", locale = "en")
@Composable
fun ChargePowerSliderDarkEnPreview() {
    AlterChargeTheme(darkTheme = true) {
        var minPower by remember { mutableIntStateOf(50) }
        var maxPower by remember { mutableIntStateOf(350) }

        ChargePowerSlider(
            minPower = minPower,
            maxPower = maxPower,
            onRangeChange = { min, max ->
                minPower = min
                maxPower = max
            }
        )
    }
}


