/**
 * L1 카드 오른쪽 번개 그리기: 판정 상태(BoltGraphic, 수위만큼 채움 + 바깥 빛)와 기록 수집 중(CollectingBoltGraphic, 전체 흐림).
 * Figma 경로 · 배치 값과 번개 색을 함께 둔다. 모드별 번개 자리는 CardInfoModeSpec.kt의 ModeSpec에 있다.
 */
package com.altercharge.app.ui.card.info

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 카드 고유 값 (Figma card_info/* 변수, 라이트/다크 같은 값)
private val BoltTrackColor = Color.White.copy(alpha = 0.32f)
private val BoltGlowColor = Color(0x8C3B6FE0)

/** Figma glow 효과는 stdDeviation 33. 안드로이드 shadow radius로 바꾸면 약 56. */
private val BoltGlowRadius = 56.dp

// 번개 그래픽 (bolt_track · bolt_fill · glow). Figma 경로를 그대로 쓰고 카드 높이에 맞춰 키운다.
private const val BOLT_PATH =
    "M207.508 20.2862C209.733 2.45304 187.324 -7.27108 175.819 6.5349L4.20642 212.469" +
        "C-5.56348 224.193 2.77329 241.993 18.0344 241.993H171.208C182.037 241.993 190.415 251.485 189.069 262.23" +
        "L175.638 369.445C173.404 387.281 195.819 397.015 207.327 383.205L378.901 177.316" +
        "C388.671 165.592 380.334 147.793 365.073 147.793H211.988C201.163 147.793 192.787 138.307 194.127 127.565" +
        "L207.508 20.2862Z"
private const val BOLT_WIDTH = 383.107f
private const val BOLT_HEIGHT = 389.74f

/** 판정별 채움 수위. 번개 높이 기준 왼쪽 · 가운데 · 오른쪽 수면 높이(위에서부터). */
private class BoltLevel(val left: Float, val middle: Float, val right: Float)

private fun CardInfoStatus.boltLevel(): BoltLevel = when (this) {
    CardInfoStatus.TODAY -> BoltLevel(left = 0.75f, middle = 0.705f, right = 0.66f)
    CardInfoStatus.TOMORROW -> BoltLevel(left = 0.43f, middle = 0.48f, right = 0.50f)
    CardInfoStatus.WITHIN_N_DAYS -> BoltLevel(left = 0.29f, middle = 0.275f, right = 0.27f)
    // 수집 중은 채움 수위를 쓰지 않는다 (CollectingBoltGraphic). 자리 채움 값.
    CardInfoStatus.COLLECTING -> BoltLevel(left = 1f, middle = 1f, right = 1f)
}

/** 판정별로 번개가 오늘 자리보다 오른쪽으로 밀린 거리 (모드마다 Figma 값이 다르다). */
private fun ModeSpec.boltShift(status: CardInfoStatus): Dp = when (status) {
    CardInfoStatus.TOMORROW -> tomorrowShift
    CardInfoStatus.WITHIN_N_DAYS -> threeDaysShift
    CardInfoStatus.TODAY, CardInfoStatus.COLLECTING -> 0.dp
}

@Composable
internal fun BoltGraphic(
    status: CardInfoStatus,
    spec: ModeSpec,
    fillColor: Color,
    modifier: Modifier = Modifier,
) {
    val track = remember { PathParser().parsePathString(BOLT_PATH).toPath() }
    val level = status.boltLevel()
    val fill = remember(status) {
        val surface = Path().apply {
            val y0 = BOLT_HEIGHT * level.left
            val y1 = BOLT_HEIGHT * level.middle
            val y2 = BOLT_HEIGHT * level.right
            // 가운데 점을 지나도록 조절점을 잡는다
            val controlY = 2 * y1 - (y0 + y2) / 2
            moveTo(0f, y0)
            quadraticTo(BOLT_WIDTH / 2, controlY, BOLT_WIDTH, y2)
            lineTo(BOLT_WIDTH, BOLT_HEIGHT * 2)
            lineTo(0f, BOLT_HEIGHT * 2)
            close()
        }
        Path().apply { op(track, surface, PathOperation.Intersect) }
    }

    Canvas(modifier) {
        // Figma 규칙: 왼쪽 x 고정, 글자와 겹쳐도 비키지 않는다.
        val boltWidth = spec.boltWidth.toPx()
        val scaleX = boltWidth / BOLT_WIDTH
        val scaleY = spec.boltHeight.toPx() / BOLT_HEIGHT
        val scale = minOf(scaleX, scaleY)
        val left = (spec.boltLeft + spec.boltShift(status)).toPx()
        val top = spec.boltTop.toPx()
        withTransform({
            translate(left, top)
            scale(scaleX, scaleY, pivot = Offset.Zero)
        }) {
            drawPath(track, BoltTrackColor)
            // 채움과 바깥 빛을 한 번에 그린다
            drawIntoCanvas { canvas ->
                val paint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = fillColor.toArgb()
                    setShadowLayer(BoltGlowRadius.toPx() / scale, 0f, 0f, BoltGlowColor.toArgb())
                }
                canvas.nativeCanvas.drawPath(fill.asAndroidPath(), paint)
            }
        }
    }
}

// 기록 수집 중 번개. Figma status = collecting (647:58207)의 bolt_track · bolt_fill SVG 값 그대로.
// 판정 상태와 달리 번개 전체에 가우시안 블러(표준편차 10)가 걸려 있고, 크기와 자리도 다르다.
// 모양: 세로에서 svg(411.6 × 417)를 카드 안 x 113, y -29.4(= -50 + 458.2 × 4.5%)에 놓는다. 왼쪽 x 고정은 판정 상태와 같다.
private const val COLLECTING_BOLT_PATH =
    "M221.092 40.2897C223.32 22.4555 200.91 12.7278 189.404 26.5345L24.2068 224.756" +
        "C14.4363 236.48 22.773 254.28 38.0344 254.28H185.413C196.24 254.28 204.616 263.768 203.274 274.511" +
        "L190.511 376.67C188.283 394.504 210.694 404.232 222.2 390.425L387.397 192.204" +
        "C397.167 180.48 388.831 162.68 373.569 162.68H226.191C215.364 162.68 206.987 153.191 208.329 142.449" +
        "L221.092 40.2897Z"

/** 아래 끝 파란 꼭지 (bolt_fill). svg(160.4 × 138.2)를 카드 안 x 242.48, y 293.41에 놓는다. */
private const val COLLECTING_FILL_PATH =
    "M93.3584 67.6357C84.6971 78.0284 69.8588 75.0853 64 65.3887C74.5418 65.3805 85.3377 64.9178 96.3877 64L93.3584 67.6357Z"

// 위 SVG 위치는 세로(portrait) variant 기준이다. bolt_track 상자(113, -50, 412.4 × 458.2) 안에서의 자리로 바꿔 두고,
// 다른 모드는 그 모드의 bolt_track 상자 크기 비율(s = 상자 폭 / 412.4)로 키우거나 줄인다.
// Figma collecting variant의 bolt_track 상자는 모드마다 오늘 variant와 같다.
private const val COLLECTING_REF_TRACK_WIDTH = 412.4f
private val CollectingBoltInTrack = Offset(0f, 20.62f) // (113 - 113, -29.38 - -50)
private val CollectingFillInTrack = Offset(129.48f, 343.41f) // (242.48 - 113, 293.41 - -50)
private const val COLLECTING_BOLT_SCALE_X = 412.4f / 411.604f
private const val COLLECTING_BLUR_SIGMA = 10f
private const val COLLECTING_GLOW_SIGMA = 32f

@Composable
internal fun CollectingBoltGraphic(spec: ModeSpec, fillColor: Color, modifier: Modifier = Modifier) {
    val track = remember { PathParser().parsePathString(COLLECTING_BOLT_PATH).toPath() }
    val nub = remember { PathParser().parsePathString(COLLECTING_FILL_PATH).toPath() }
    val density = LocalDensity.current.density
    // 모드별 bolt_track 상자에 맞춘 배율 (세로 = 1)
    val s = spec.trackWidth.value / COLLECTING_REF_TRACK_WIDTH
    // 블러 필터는 density(와 색)로만 정해지므로 그릴 때마다 만들지 않고 한 번 만든다.
    // 번개·꼭지의 LAYER_BLUR(σ10)는 Figma 모드 4개가 같아 s로 키우지 않는다.
    val trackPaint = remember(density) { blurPaint(BoltTrackColor, COLLECTING_BLUR_SIGMA, density) }
    // 꼭지 바깥 빛(DROP_SHADOW)은 모드 크기에 비례한다: 세로 64 · 가로 66 · WF 가로 54 · WF 세로 47 → 표준편차 32 × s.
    // 꼭지 자체(표준편차 10)와 합쳐서 퍼지는 정도는 √((32s)² + 10²)
    val glowPaint = remember(density, s) {
        blurPaint(BoltGlowColor, kotlin.math.hypot(COLLECTING_GLOW_SIGMA * s, COLLECTING_BLUR_SIGMA), density)
    }
    val nubPaint = remember(density, fillColor) { blurPaint(fillColor, COLLECTING_BLUR_SIGMA, density) }
    val trackLeft = spec.trackLeft.value
    val trackTop = spec.trackTop.value
    Canvas(modifier) {
        fun draw(path: Path, inTrack: Offset, scaleX: Float, paint: android.graphics.Paint) {
            withTransform({
                translate((trackLeft + inTrack.x * s) * density, (trackTop + inTrack.y * s) * density)
                scale(scaleX * s * density, s * density, pivot = Offset.Zero)
            }) {
                drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint) }
            }
        }
        draw(track, CollectingBoltInTrack, COLLECTING_BOLT_SCALE_X, trackPaint)
        draw(nub, CollectingFillInTrack, 1f, glowPaint)
        draw(nub, CollectingFillInTrack, 1f, nubPaint)
    }
}

/** 블러 페인트. Figma 블러 표준편차(σ, dp) → 안드로이드 BlurMaskFilter 반지름: r = (σ × density − 0.5) / 0.57735 */
private fun blurPaint(color: Color, sigmaDp: Float, density: Float): android.graphics.Paint =
    android.graphics.Paint().apply {
        isAntiAlias = true
        this.color = color.toArgb()
        val radius = ((sigmaDp * density - 0.5f) / 0.57735f).coerceAtLeast(0.1f)
        maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
    }
