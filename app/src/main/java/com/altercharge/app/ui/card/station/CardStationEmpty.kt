package com.altercharge.app.ui.card.station

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.component.IviButton
import com.altercharge.app.ui.designsystem.component.IviButtonHierarchy
import com.altercharge.app.ui.designsystem.component.IviButtonSize
import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import com.altercharge.app.ui.designsystem.layout.LocalAppLayout
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * R1 추천 충전소 빈 상태 (Figma card_station availability = empty, 807:57151).
 * 오프라인이고 받아 둔 충전소 정보가 없을 때: 그림 · 제목 · 안내 + 우측 상단 새로고침 (REQ_CM_006).
 *
 * R1 칸은 내용 높이만큼만 차지하므로 카드 높이를 시안 값으로 고정한다 ([cardStationHeightFor]). 채우면 R2가 밀려난다.
 *
 * @param offline 연결이 끊겨 못 받았을 때만 "불러오지 못했어요 / 연결되면…"이다. 연결돼 있는데 추천이 없으면
 *   (3km 안 충전소 없음 등) 오프라인 안내 대신 "추천할 충전소가 없어요"만 보여 준다 (시안에 없는 경우라 문구만 바꿈)
 * @param refreshing 요청 중이면 새로고침 버튼을 비활성으로 둔다 (시안 규칙). 30초 자동 재요청은 ViewModel이 계속한다
 */
@Composable
fun CardStationEmpty(
    onRefresh: () -> Unit,
    refreshing: Boolean,
    offline: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardStationHeightFor(LocalAppLayout.current))
            .clip(RoundedCornerShape(AlterRadius.XXLarge))
            .background(AlterTheme.colors.containerBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AlterMargin.Normal),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.img_empty_station_offline),
                contentDescription = null,
                modifier = Modifier.size(width = 200.dp, height = 160.dp),
            )
            Text(
                text = stringResource(if (offline) R.string.card_station_empty_title else R.string.card_station_empty_online_title),
                style = AlterTheme.typography.bodyXSmall,
                color = AlterTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            if (offline) {
                Text(
                    text = stringResource(R.string.card_station_empty_description),
                    style = AlterTheme.typography.labelSmall,
                    color = AlterTheme.colors.textTertiary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // IviButton은 터치 영역 76 안에 64 버튼을 가운데 둔다: 바깥 10 + 안쪽 6 = 시안의 카드 안쪽 여백 16
        IviButton(
            text = null,
            onClick = onRefresh,
            hierarchy = IviButtonHierarchy.Tonal,
            size = IviButtonSize.Small,
            enabled = !refreshing,
            leadingIcon = painterResource(R.drawable.ic_refresh),
            contentDescription = stringResource(R.string.card_station_refresh),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp),
        )
    }
}

/** Figma card_station 모드별 높이: 기본 368, W-Extended(_tall) 420, WF 가로 360 */
fun cardStationHeightFor(info: AppLayoutInfo): Dp = when {
    info.formFactor == FormFactor.W_EXTENDED -> 420.dp
    info.formFactor == FormFactor.WF && info.isLandscape -> 360.dp
    else -> 368.dp
}

@Preview(widthDp = 383, heightDp = 368)
@Composable
private fun CardStationEmptyPreview() {
    AlterChargeTheme { CardStationEmpty(onRefresh = {}, refreshing = false) }
}

@Preview(widthDp = 383, heightDp = 368)
@Composable
private fun CardStationEmptyDarkPreview() {
    AlterChargeTheme(darkTheme = true) { CardStationEmpty(onRefresh = {}, refreshing = true) }
}
