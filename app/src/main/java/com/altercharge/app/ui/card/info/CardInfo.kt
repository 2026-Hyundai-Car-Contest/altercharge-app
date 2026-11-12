/**
 * L1 오늘 충전 판정 카드의 공개 진입점: CardInfoMode와 고르는 함수, CardInfo(글자 · 장소 줄 · 수집 중 진행 막대)와 카드 고유 색 · 치수.
 * 번개 그리기는 CardInfoBolt.kt, 모드별 치수는 CardInfoModeSpec.kt, 미리보기는 CardInfoPreview.kt에 있다.
 */
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
                // 받아 둔 충전소 정보가 없으면(오프라인 · REQ_CM_006 예외) 추천 충전소 줄을 숨긴다 (Figma show_place = false)
                PlaceContent(state = state, spec = spec, showIcon = showIcon)
            }
        }
    }
}

/**
 * 추천 충전소 칸: 장소 줄과, 오프라인이면 그 아래 "N분 전 기준" 줄. Figma container_text (세로, 간격 10, 왼쪽 8).
 * 연결되어 있으면([CardInfoUiState.placeLastChecked] = null) 장소 줄만 보인다 (Figma show_last_checked).
 */
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
                // 글자 크기는 eyebrow와 같은 label (모드별 medium · small), 색은 basic_static_light_300 (흰색 64%)
                style = spec.eyebrowStyle(),
                color = LastCheckedColor,
                maxLines = 1,
                // Figma row_last_checked 왼쪽 40 = 아이콘 24 + 간격 16. 아이콘을 숨겨도 시안 값 그대로 둔다.
                modifier = Modifier.padding(start = LastCheckedInset),
            )
        }
    }
}

/** 판정 제목. N일내는 버틸 일수를 넣는다 (2 → "2일내 충전", 5 → "5일내 충전") */
@Composable
private fun titleText(status: CardInfoStatus, daysUntilCharge: Int): String = when (status) {
    CardInfoStatus.TODAY -> stringResource(R.string.card_info_title_today)
    CardInfoStatus.TOMORROW -> stringResource(R.string.card_info_title_tomorrow)
    CardInfoStatus.WITHIN_N_DAYS -> stringResource(R.string.card_info_title_within_days, daysUntilCharge)
    CardInfoStatus.COLLECTING -> stringResource(R.string.card_info_title_collecting)
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

/** card_info/text/gap: 장소 줄과 "N분 전 기준" 줄 사이 */
private val PlaceRowGap = 10.dp

/** row_last_checked 왼쪽 여백 = 장소 아이콘 24 + card_info/text/icon_gap 16. 장소 이름 글자와 줄을 맞춘다 */
private val LastCheckedInset = PlaceIconSize + AlterMargin.Normal

/** basic/static_light/basic_static_light_300 (흰색 64%) */
private val LastCheckedColor = Color.White.copy(alpha = 0.64f)
