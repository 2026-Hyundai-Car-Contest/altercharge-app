package com.altercharge.app.ui.card.info

import android.graphics.BlurMaskFilter
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.ShadowLayer
import com.altercharge.app.ui.designsystem.theme.alterShadow

/**
 * 카드가 놓이는 화면 모드. Figma card_info mode.
 * 카드 높이는 모드마다 고정이고 카드가 스스로 적용한다. 너비는 부모 폭을 채운다.
 * 번개 그래픽은 오른쪽에 붙고, 제목과 겹치지 않게 제목 오른쪽으로 비켜 선다.
 */
enum class CardInfoMode(val height: Dp) {
    LANDSCAPE(368.dp),
    PORTRAIT(358.dp),
    WF_LANDSCAPE(300.dp),
    WF_PORTRAIT(260.dp),
}

/**
 * 레이아웃 정보로 mode를 고른다. card_info 시안은 S-17 · WF 모드만 있다.
 * W-Standard 세로 화면(452:53649)은 PORTRAIT(378×358)를 쓴다.
 * TODO 디자이너 확인: W-Standard 가로 · W-Extended는 card_info 시안이 없어 S-17 모드(LANDSCAPE · PORTRAIT)로 대신한다.
 */
fun cardInfoModeFor(info: AppLayoutInfo): CardInfoMode = when {
    info.formFactor == FormFactor.WF ->
        if (info.isLandscape) CardInfoMode.WF_LANDSCAPE else CardInfoMode.WF_PORTRAIT
    info.isLandscape -> CardInfoMode.LANDSCAPE
    else -> CardInfoMode.PORTRAIT
}

/**
 * L1 오늘 충전 판정 카드. Figma card_info (366:38742). 담당: 서희님
 * 보기 전용이다. 라이트/다크 모두 어두운 카드 배경을 쓰고, 제목과 번개 채움만 blue_500을 따른다.
 *
 * 기본으로 부모 폭을 채우고(fillMaxWidth) 높이는 [CardInfoMode.height]를 쓴다.
 * modifier에 크기를 주면 그 값이 이긴다 (예: Modifier.size(450.dp, 368.dp)).
 *
 * @param mode 레이아웃 틀 안에서는 cardInfoModeFor(LocalAppLayout.current)로 고른다.
 * @param showIcon Figma Show icons. false면 장소 줄에서 아이콘만 뺀다.
 */
@Composable
fun CardInfo(
    state: CardInfoUiState,
    modifier: Modifier = Modifier,
    mode: CardInfoMode = CardInfoMode.LANDSCAPE,
    showIcon: Boolean = true,
) {
    val status = state.toStatus()
    val spec = mode.spec()
    val shape = RoundedCornerShape(CardRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(mode.height)
            .alterShadow(CardShadow, CardRadius)
            .clip(shape)
            .background(CardBackground)
            .semantics(mergeDescendants = true) {},
    ) {
        if (status == CardInfoStatus.COLLECTING) {
            CollectingBoltGraphic(spec = spec, fillColor = AlterTheme.colors.blue500, modifier = Modifier.matchParentSize())
        } else {
            BoltGraphic(
                status = status,
                spec = spec,
                fillColor = AlterTheme.colors.blue500,
                modifier = Modifier.matchParentSize(),
            )
        }
        Column(
            modifier = Modifier
                .padding(AlterPadding.XSmall)
                .padding(spec.contentPadding),
        ) {
            Column(
                modifier = Modifier.widthIn(max = spec.headerMaxWidth),
                verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
            ) {
                Text(
                    text = stringResource(R.string.card_info_eyebrow),
                    style = spec.eyebrowStyle(),
                    color = EyebrowColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(status.titleRes),
                    style = spec.titleStyle(),
                    color = AlterTheme.colors.blue500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (state.recordedDays != null) {
                CollectingContent(recordedDays = state.recordedDays, spec = spec)
            } else {
                Row(
                    modifier = Modifier
                        .padding(start = AlterPadding.XSmall)
                        .widthIn(max = spec.placeMaxWidth),
                    horizontalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showIcon) {
                        // TODO 디자이너 확인: 시안이 장소 아이콘 자리에 Loading 아이콘을 쓰고 있다. 확정 아이콘이 오면 바꾼다.
                        Image(
                            painter = painterResource(R.drawable.card_info_ic_place),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(PlaceColor),
                            modifier = Modifier.size(PlaceIconSize),
                        )
                    }
                    Text(
                        text = state.placeName,
                        style = spec.placeStyle(),
                        color = PlaceColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@get:StringRes
private val CardInfoStatus.titleRes: Int
    get() = when (this) {
        CardInfoStatus.TODAY -> R.string.card_info_title_today
        CardInfoStatus.TOMORROW -> R.string.card_info_title_tomorrow
        CardInfoStatus.WITHIN_THREE_DAYS -> R.string.card_info_title_within_three_days
        CardInfoStatus.COLLECTING -> R.string.card_info_title_collecting
    }

/**
 * 기록 수집 중 본문: 진행 막대 "N / 14일"과 안내 문구. Figma status = collecting (647:58207).
 * 막대 트랙 220 × 8, 라벨은 막대 오른쪽 16 간격. 13일차는 "내일부터"로 바뀐다.
 * TODO 디자이너 확인: 시안이 세로(378×358)뿐이라 다른 모드도 같은 구성에 모드별 글자 크기를 쓴다.
 */
@Composable
private fun CollectingContent(recordedDays: Int, spec: ModeSpec) {
    Row(
        // Figma progress 프레임은 13일차에 폭 333으로 안쪽 폭 330을 3 넘어 오른쪽 여백까지 간다. 눌러서 글자가 잘리지 않게 한다.
        modifier = Modifier
            .wrapContentWidth(align = Alignment.Start, unbounded = true)
            .padding(start = AlterPadding.XSmall),
        horizontalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(ProgressTrackWidth)
                .height(ProgressHeight)
                .clip(RoundedCornerShape(ProgressRadius))
                .background(ProgressTrackColor),
        ) {
            Box(
                modifier = Modifier
                    .width(collectingFillWidthDp(recordedDays, ProgressTrackWidth.value.toInt()).dp)
                    .height(ProgressHeight)
                    .clip(RoundedCornerShape(ProgressRadius))
                    .background(AlterTheme.colors.blue500),
            )
        }
        Text(
            text = stringResource(R.string.card_info_collecting_progress, recordedDays, COLLECTING_TOTAL_DAYS),
            style = spec.eyebrowStyle(),
            color = EyebrowColor,
            maxLines = 1,
        )
    }
    val daysLeft = COLLECTING_TOTAL_DAYS - recordedDays
    Text(
        text = if (daysLeft <= 1) {
            stringResource(R.string.card_info_collecting_message_tomorrow)
        } else {
            stringResource(R.string.card_info_collecting_message, daysLeft)
        },
        style = spec.placeStyle().copy(fontWeight = AlterFontWeight.Strong),
        color = PlaceColor,
        modifier = Modifier.padding(start = AlterPadding.XSmall),
    )
}

private val ProgressTrackWidth = 220.dp
private val ProgressHeight = 8.dp
private val ProgressRadius = 4.dp
/** card_info/chip/border (흰색 24%) */
private val ProgressTrackColor = Color.White.copy(alpha = 0.24f)

// 카드 고유 값 (Figma card_info/* 변수, 라이트/다크 같은 값)
private val CardBackground = Color(0xFF020417)

/** card_info/container/radius. 카드 변수라 AlterRadius에 없다 (C 카드의 32도 카드별 변수) */
private val CardRadius = 32.dp

/** card_info/container/shadow (0 12 32, #000000 32%). 다른 카드 · AlterShadows와 값이 달라 카드에 둔다 */
private val CardShadow = listOf(ShadowLayer(offsetY = 12.dp, blur = 32.dp, color = Color(0x52000000)))

private val EyebrowColor = Color.White.copy(alpha = 0.64f)
private val PlaceColor = Color.White
private val PlaceIconSize = 24.dp
private val BoltTrackColor = Color.White.copy(alpha = 0.32f)
private val BoltGlowColor = Color(0x8C3B6FE0)

/** Figma glow 효과는 stdDeviation 33. 안드로이드 shadow radius로 바꾸면 약 56. */
private val BoltGlowRadius = 56.dp

/** 모드별 크기 · 글자. 너비 값은 Figma 인스턴스의 고정 너비를 최대 너비로 쓴다. */
private class ModeSpec(
    val contentPadding: Dp,
    val headerMaxWidth: Dp,
    val placeMaxWidth: Dp,
    val isWf: Boolean,
    /**
     * 번개(bolt_track) 영역. Figma 인스턴스 값 그대로(오늘·내일 기준).
     * 번개는 왼쪽 x가 고정이다([boltLeft]). 카드 폭이 달라도 같은 자리에 있고, 좁으면 오른쪽이 더 잘린다
     * (가로 450 · 383 인스턴스 모두 bolt_track x 130, WF 세로 580 · 584 모두 x 319).
     */
    val boltLeft: Dp,
    val boltTop: Dp,
    val boltWidth: Dp,
    val boltHeight: Dp,
    /** 판정별로 번개가 오늘보다 오른쪽으로 밀린 거리. Figma 판정별 variant의 bolt_track x 차이. */
    val tomorrowShift: Dp,
    val threeDaysShift: Dp,
    /** Figma bolt_track 상자 그대로(x · y · 폭 · 높이). 수집 중 번개를 모드 크기에 맞출 때 쓴다. */
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

// 번개 값은 Figma card_info mode별 인스턴스(366:38751 · 38755 · 38759 · 38763)의 bolt_track에서
// 그림이 실제로 차지하는 영역(상자 안쪽 위아래 8.87% · 좌우 4.95%)이다.
// boltLeft = bolt_track x + 상자 폭 × 4.95%
private val LandscapeSpec = ModeSpec(
    AlterPadding.Large, 252.dp, 279.dp, isWf = false,
    boltLeft = 151.0.dp, boltTop = (-9.7).dp, boltWidth = 381.9.dp, boltHeight = 387.4.dp,
    tomorrowShift = 11.0.dp, threeDaysShift = 68.0.dp,
    trackLeft = 130.0.dp, trackTop = (-51.5).dp, trackWidth = 423.9.dp, trackHeight = 471.0.dp,
)
// 세로 제목 영역 232 (개발자 Q&A Q5: 212 → 232, 영문 "Tomorrow" 213이 잘리지 않게)
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
// WF 세로는 카드 폭이 584(Q&A Q4, 화면 인스턴스 452:54383)이고 번개는 왼쪽 x 319 그대로라
// bolt_track x 319 + 14.8
private val WfPortraitSpec = ModeSpec(
    AlterPadding.Normal, 290.dp, 262.dp, isWf = true,
    boltLeft = 333.8.dp, boltTop = (-6.9).dp, boltWidth = 269.8.dp, boltHeight = 273.8.dp,
    tomorrowShift = 0.dp, threeDaysShift = 0.dp,
    trackLeft = 319.0.dp, trackTop = (-36.4).dp, trackWidth = 299.5.dp, trackHeight = 332.8.dp,
)

private fun CardInfoMode.spec(): ModeSpec = when (this) {
    CardInfoMode.LANDSCAPE -> LandscapeSpec
    CardInfoMode.PORTRAIT -> PortraitSpec
    CardInfoMode.WF_LANDSCAPE -> WfLandscapeSpec
    CardInfoMode.WF_PORTRAIT -> WfPortraitSpec
}

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
    CardInfoStatus.WITHIN_THREE_DAYS -> BoltLevel(left = 0.29f, middle = 0.275f, right = 0.27f)
    // 수집 중은 채움 수위를 쓰지 않는다 (CollectingBoltGraphic). 자리 채움 값.
    CardInfoStatus.COLLECTING -> BoltLevel(left = 1f, middle = 1f, right = 1f)
}

/** 판정별로 번개가 오늘 자리보다 오른쪽으로 밀린 거리 (모드마다 Figma 값이 다르다). */
private fun ModeSpec.boltShift(status: CardInfoStatus): Dp = when (status) {
    CardInfoStatus.TOMORROW -> tomorrowShift
    CardInfoStatus.WITHIN_THREE_DAYS -> threeDaysShift
    CardInfoStatus.TODAY, CardInfoStatus.COLLECTING -> 0.dp
}

@Composable
private fun BoltGraphic(
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
private fun CollectingBoltGraphic(spec: ModeSpec, fillColor: Color, modifier: Modifier = Modifier) {
    val track = remember { PathParser().parsePathString(COLLECTING_BOLT_PATH).toPath() }
    val nub = remember { PathParser().parsePathString(COLLECTING_FILL_PATH).toPath() }
    val density = LocalDensity.current.density
    // 블러 필터는 density(와 색)로만 정해지므로 그릴 때마다 만들지 않고 한 번 만든다.
    val trackPaint = remember(density) { blurPaint(BoltTrackColor, COLLECTING_BLUR_SIGMA, density) }
    // 꼭지 바깥 빛(표준편차 32, 55%)과 꼭지 자체(표준편차 10). 합쳐서 퍼지는 정도는 √(32² + 10²) ≈ 33.5
    val glowPaint = remember(density) {
        blurPaint(BoltGlowColor, kotlin.math.hypot(COLLECTING_GLOW_SIGMA, COLLECTING_BLUR_SIGMA), density)
    }
    val nubPaint = remember(density, fillColor) { blurPaint(fillColor, COLLECTING_BLUR_SIGMA, density) }
    // 모드별 bolt_track 상자에 맞춘 배율 (세로 = 1)
    val s = spec.trackWidth.value / COLLECTING_REF_TRACK_WIDTH
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

// 미리보기: 오늘 · 내일 · 3일내 × 라이트 · 다크 × 가로 · 세로 (+ WF). 높이는 카드가 mode로 정한다.

@Composable
private fun CardInfoPreviewColumn(mode: CardInfoMode, width: Dp, darkTheme: Boolean) {
    AlterChargeTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .background(AlterTheme.colors.background)
                .padding(AlterPadding.Large),
            verticalArrangement = Arrangement.spacedBy(AlterMargin.Large),
        ) {
            CardInfoSampleData.all.forEach { state ->
                CardInfo(state = state, mode = mode, modifier = Modifier.width(width))
            }
        }
    }
}

@Preview(name = "가로 383 · 라이트", widthDp = 431, heightDp = 1200)
@Composable
private fun CardInfoLandscapeLightPreview() =
    CardInfoPreviewColumn(CardInfoMode.LANDSCAPE, 383.dp, darkTheme = false)

@Preview(name = "가로 450 · 다크", widthDp = 498, heightDp = 1200)
@Composable
private fun CardInfoLandscapeDarkPreview() =
    CardInfoPreviewColumn(CardInfoMode.LANDSCAPE, 450.dp, darkTheme = true)

@Preview(name = "세로 · 라이트", widthDp = 426, heightDp = 1170)
@Composable
private fun CardInfoPortraitLightPreview() =
    CardInfoPreviewColumn(CardInfoMode.PORTRAIT, 378.dp, darkTheme = false)

@Preview(name = "세로 · 다크", widthDp = 426, heightDp = 1170)
@Composable
private fun CardInfoPortraitDarkPreview() =
    CardInfoPreviewColumn(CardInfoMode.PORTRAIT, 378.dp, darkTheme = true)

@Preview(name = "WF 가로 · 라이트", widthDp = 435, heightDp = 1000)
@Composable
private fun CardInfoWfLandscapePreview() =
    CardInfoPreviewColumn(CardInfoMode.WF_LANDSCAPE, 387.dp, darkTheme = false)

@Preview(name = "WF 세로 · 라이트", widthDp = 632, heightDp = 900)
@Composable
private fun CardInfoWfPortraitPreview() =
    CardInfoPreviewColumn(CardInfoMode.WF_PORTRAIT, 584.dp, darkTheme = false)

@Preview(name = "가로 383 · 영문", widthDp = 431, heightDp = 1200, locale = "en")
@Composable
private fun CardInfoLandscapeEnPreview() =
    CardInfoPreviewColumn(CardInfoMode.LANDSCAPE, 383.dp, darkTheme = false)
