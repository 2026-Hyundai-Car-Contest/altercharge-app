/** L1 오늘 충전 판정 카드. 번개는 CardInfoBolt.kt, 모드별 치수는 CardInfoModeSpec.kt */
package com.altercharge.app.ui.card.info

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.ShadowLayer
import com.altercharge.app.ui.designsystem.theme.alterShadow

/** Figma card_info mode. 높이는 모드마다 고정 */
enum class CardInfoMode(val height: Dp) {
    LANDSCAPE(368.dp),
    PORTRAIT(358.dp),
    WF_LANDSCAPE(300.dp),
    WF_PORTRAIT(260.dp),
}

/**
 * card_info 시안은 S-17 · WF 모드만 있다. W-Standard 세로(452:53649)는 PORTRAIT(378×358)를 쓴다.
 * TODO 디자이너 확인: W-Standard 가로 · W-Extended는 card_info 시안이 없어 S-17 모드(LANDSCAPE · PORTRAIT)로 대신한다.
 */
fun cardInfoModeFor(info: AppLayoutInfo): CardInfoMode = when {
    info.formFactor == FormFactor.WF ->
        if (info.isLandscape) CardInfoMode.WF_LANDSCAPE else CardInfoMode.WF_PORTRAIT
    info.isLandscape -> CardInfoMode.LANDSCAPE
    else -> CardInfoMode.PORTRAIT
}

/**
 * Figma card_info (366:38742). 라이트/다크 모두 어두운 카드 배경이고, 제목과 번개 채움만 blue_500을 따른다.
 *
 * @param mode 레이아웃 틀 안에서는 cardInfoModeFor(LocalAppLayout.current)로 고른다.
 * @param showIcon Figma Show icons
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
                    text = titleText(status, state.daysUntilCharge),
                    style = spec.titleStyle(),
                    color = AlterTheme.colors.blue500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (state.recordedDays != null) {
                CollectingContent(recordedDays = state.recordedDays, spec = spec)
            } else if (state.placeName.isNotBlank()) {
                // 받아 둔 충전소가 없으면(오프라인 · REQ_CM_006 예외) 숨긴다 (Figma show_place = false)
                PlaceContent(state = state, spec = spec, showIcon = showIcon)
            }
        }
    }
}

/** Figma container_text. 오프라인이면 장소 아래 "N분 전 기준" 줄이 붙는다 (Figma show_last_checked) */
@Composable
private fun PlaceContent(state: CardInfoUiState, spec: ModeSpec, showIcon: Boolean) {
    Column(
        modifier = Modifier.padding(start = AlterPadding.XSmall),
        verticalArrangement = Arrangement.spacedBy(PlaceRowGap),
    ) {
        Row(
            modifier = Modifier.widthIn(max = spec.placeMaxWidth),
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
        state.placeLastChecked?.let { lastChecked ->
            Text(
                text = lastCheckedText(lastChecked),
                style = spec.eyebrowStyle(),
                color = EyebrowColor,
                maxLines = 1,
                // 아이콘을 숨겨도 시안 값 그대로 둔다
                modifier = Modifier.padding(start = LastCheckedInset),
            )
        }
    }
}

@Composable
private fun titleText(status: CardInfoStatus, daysUntilCharge: Int): String = when (status) {
    CardInfoStatus.TODAY -> stringResource(R.string.card_info_title_today)
    CardInfoStatus.TOMORROW -> stringResource(R.string.card_info_title_tomorrow)
    CardInfoStatus.WITHIN_N_DAYS -> stringResource(R.string.card_info_title_within_days, daysUntilCharge)
    CardInfoStatus.COLLECTING -> stringResource(R.string.card_info_title_collecting)
}

/**
 * Figma status = collecting (647:58207). 13일차는 "내일부터"로 바뀐다.
 * TODO 디자이너 확인: 시안이 세로(378×358)뿐이라 다른 모드도 같은 구성에 모드별 글자 크기를 쓴다.
 */
@Composable
private fun CollectingContent(recordedDays: Int, spec: ModeSpec) {
    Row(
        // Figma progress는 13일차에 폭 333으로 안쪽 폭 330을 넘어도 눌리지 않게 넘치도록 둔다.
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
/** card_info/chip/border */
private val ProgressTrackColor = Color.White.copy(alpha = 0.24f)

// Figma card_info/* 변수, 라이트/다크 같은 값
private val CardBackground = Color(0xFF020417)

/** card_info/container/radius. 카드 변수라 AlterRadius에 없다 */
private val CardRadius = 32.dp

/** card_info/container/shadow. AlterShadows와 값이 달라 카드에 둔다 */
private val CardShadow = listOf(ShadowLayer(offsetY = 12.dp, blur = 32.dp, color = Color(0x52000000)))

/** card_info/eyebrow와 basic_static_light_300(마지막 확인 시각)이 같은 흰색 64%라 하나로 쓴다 */
private val EyebrowColor = Color.White.copy(alpha = 0.64f)
private val PlaceColor = Color.White
private val PlaceIconSize = 24.dp

/** card_info/text/gap */
private val PlaceRowGap = 10.dp

/** row_last_checked 왼쪽 40 = 아이콘 24 + card_info/text/icon_gap 16 */
private val LastCheckedInset = PlaceIconSize + AlterMargin.Normal
