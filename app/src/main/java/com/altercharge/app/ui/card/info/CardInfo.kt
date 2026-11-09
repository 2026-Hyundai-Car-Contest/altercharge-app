package com.altercharge.app.ui.card.info

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.card.common.ChargeVerdict
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.ShadowLayer
import com.altercharge.app.ui.designsystem.theme.alterShadow

/**
 * 카드가 놓이는 화면 모드. Figma card_info mode.
 * 카드 높이는 모드마다 고정이고(가로 368 · 세로 358 · WF 가로 300 · WF 세로 260) 부르는 쪽이 정한다.
 * 가로는 컬럼에 맞춰 늘어나고 번개 그래픽은 오른쪽에 붙는다.
 */
enum class CardInfoMode { LANDSCAPE, PORTRAIT, WF_LANDSCAPE, WF_PORTRAIT }

/**
 * L1 오늘 충전 판정 카드. Figma card_info (366:38742). 담당: 서희님
 * 보기 전용이다. 라이트/다크 모두 어두운 카드 배경을 쓰고, 제목과 번개 채움만 blue_500을 따른다.
 *
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
            .alterShadow(CardShadow, CardRadius)
            .clip(shape)
            .background(CardBackground)
            .semantics(mergeDescendants = true) {},
    ) {
        BoltGraphic(
            status = status,
            rightOverflow = spec.boltRightOverflow,
            fillColor = AlterTheme.colors.blue500,
            modifier = Modifier.matchParentSize(),
        )
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

@get:StringRes
private val CardInfoStatus.titleRes: Int
    get() = when (this) {
        CardInfoStatus.TODAY -> R.string.card_info_title_today
        CardInfoStatus.TOMORROW -> R.string.card_info_title_tomorrow
        CardInfoStatus.WITHIN_THREE_DAYS -> R.string.card_info_title_within_three_days
    }

// 카드 고유 값 (Figma card_info/* 변수, 라이트/다크 같은 값)
private val CardBackground = Color(0xFF020417)
private val CardRadius = 32.dp
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
    /** 번개가 카드 오른쪽 밖으로 나가는 비율 (번개 너비 기준) */
    val boltRightOverflow: Float,
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

private val LandscapeSpec = ModeSpec(AlterPadding.Large, 252.dp, 279.dp, isWf = false, boltRightOverflow = 0.218f)
private val PortraitSpec = ModeSpec(AlterPadding.Normal, 212.dp, 279.dp, isWf = false, boltRightOverflow = 0.342f)
private val WfLandscapeSpec = ModeSpec(AlterPadding.Normal, 217.dp, 262.dp, isWf = true, boltRightOverflow = 0.220f)
private val WfPortraitSpec = ModeSpec(AlterPadding.Normal, 290.dp, 262.dp, isWf = true, boltRightOverflow = 0.088f)

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

/** 번개 높이 = 카드 높이 × 1.055, 위로 2.8% 넘친다 (네 모드 평균) */
private const val BOLT_HEIGHT_RATIO = 1.055f
private const val BOLT_TOP_RATIO = -0.028f

/**
 * 판정별 채움 수위. 번개 높이 기준 왼쪽 · 가운데 · 오른쪽 수면 높이(위에서부터)와,
 * 시안에서 판정별로 번개가 오른쪽으로 밀린 정도(번개 너비 기준).
 */
private class BoltLevel(val left: Float, val middle: Float, val right: Float, val shift: Float)

private fun CardInfoStatus.boltLevel(): BoltLevel = when (this) {
    CardInfoStatus.TODAY -> BoltLevel(left = 0.75f, middle = 0.705f, right = 0.66f, shift = 0f)
    CardInfoStatus.TOMORROW -> BoltLevel(left = 0.43f, middle = 0.48f, right = 0.50f, shift = 0.029f)
    CardInfoStatus.WITHIN_THREE_DAYS -> BoltLevel(left = 0.29f, middle = 0.275f, right = 0.27f, shift = 0.18f)
}

@Composable
private fun BoltGraphic(
    status: CardInfoStatus,
    rightOverflow: Float,
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
        val scale = size.height * BOLT_HEIGHT_RATIO / BOLT_HEIGHT
        val boltWidth = BOLT_WIDTH * scale
        val left = size.width - boltWidth + boltWidth * (rightOverflow + level.shift)
        val top = size.height * BOLT_TOP_RATIO
        withTransform({
            translate(left, top)
            scale(scale, scale, pivot = Offset.Zero)
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

// 미리보기: 오늘 · 내일 · 3일내 × 라이트 · 다크 × 가로 · 세로 (+ WF)

@Composable
private fun CardInfoPreviewColumn(mode: CardInfoMode, width: Dp, height: Dp, darkTheme: Boolean) {
    AlterChargeTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .background(AlterTheme.colors.background)
                .padding(AlterPadding.Large),
            verticalArrangement = Arrangement.spacedBy(AlterMargin.Large),
        ) {
            listOf(
                CardInfoUiState(ChargeVerdict.TODAY, daysUntilCharge = 0, placeName = "예시 아파트"),
                CardInfoUiState(ChargeVerdict.WITHIN_TWO_DAYS, daysUntilCharge = 1, placeName = "예시 아파트 지하주차장"),
                CardInfoUiState(
                    ChargeVerdict.WITHIN_TWO_DAYS,
                    daysUntilCharge = 2,
                    placeName = "아주 긴 예시 장소 이름이 들어가면 한 줄로 말줄임",
                ),
            ).forEach { state ->
                CardInfo(
                    state = state,
                    mode = mode,
                    modifier = Modifier.size(width, height),
                )
            }
        }
    }
}

@Preview(name = "가로 · 라이트", widthDp = 498, heightDp = 1200)
@Composable
private fun CardInfoLandscapeLightPreview() =
    CardInfoPreviewColumn(CardInfoMode.LANDSCAPE, 450.dp, 368.dp, darkTheme = false)

@Preview(name = "가로 · 다크", widthDp = 498, heightDp = 1200)
@Composable
private fun CardInfoLandscapeDarkPreview() =
    CardInfoPreviewColumn(CardInfoMode.LANDSCAPE, 450.dp, 368.dp, darkTheme = true)

@Preview(name = "세로 · 라이트", widthDp = 426, heightDp = 1170)
@Composable
private fun CardInfoPortraitLightPreview() =
    CardInfoPreviewColumn(CardInfoMode.PORTRAIT, 378.dp, 358.dp, darkTheme = false)

@Preview(name = "세로 · 다크", widthDp = 426, heightDp = 1170)
@Composable
private fun CardInfoPortraitDarkPreview() =
    CardInfoPreviewColumn(CardInfoMode.PORTRAIT, 378.dp, 358.dp, darkTheme = true)

@Preview(name = "WF 가로 · 라이트", widthDp = 435, heightDp = 1000)
@Composable
private fun CardInfoWfLandscapePreview() =
    CardInfoPreviewColumn(CardInfoMode.WF_LANDSCAPE, 387.dp, 300.dp, darkTheme = false)

@Preview(name = "WF 세로 · 라이트", widthDp = 628, heightDp = 900)
@Composable
private fun CardInfoWfPortraitPreview() =
    CardInfoPreviewColumn(CardInfoMode.WF_PORTRAIT, 580.dp, 260.dp, darkTheme = false)

@Preview(name = "가로 · 영문", widthDp = 498, heightDp = 1200, locale = "en")
@Composable
private fun CardInfoLandscapeEnPreview() =
    CardInfoPreviewColumn(CardInfoMode.LANDSCAPE, 450.dp, 368.dp, darkTheme = false)
