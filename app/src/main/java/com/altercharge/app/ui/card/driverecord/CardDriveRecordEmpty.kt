package com.altercharge.app.ui.card.driverecord

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.ShadowLayer
import com.altercharge.app.ui.designsystem.theme.alterShadow

/**
 * Figma state = empty, empty_overlay 648:58509. 요일 라벨 줄은 덮지 않는다.
 * 시안 설명은 "chart/background 40% + 배경 블러 6"이지만 그려진 인스턴스는 막 아래가 거의 순백이라 불투명하게 얹는다.
 * 시안 메모: 막 불투명도와 블러 값이 설명과 그림이 다르다.
 */
@Composable
internal fun ChartEmptyOverlay(mode: CardDriveRecordMode, modifier: Modifier = Modifier) {
    // WF 가로는 차트가 낮아 그림이 작다 (648:58515). 다른 모드는 648:58230 · 58325 · 58420 · 58610
    val illust = if (mode == CardDriveRecordMode.WF_LANDSCAPE) EmptyIllustCompact else EmptyIllustRegular
    Box(modifier = modifier.padding(bottom = EmptyOverlayBottomInset)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(EmptyOverlayRadius))
                .background(AlterTheme.colors.surface.copy(alpha = EmptyOverlayAlpha)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall, Alignment.CenterVertically),
        ) {
            Box(Modifier.size(width = illust.width, height = illust.height)) {
                Image(
                    painter = painterResource(R.drawable.img_vehicle_illust),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .offset(y = illust.carTop)
                        .size(width = illust.width, height = illust.carHeight),
                )
                AlertBadge(illust, Modifier.offset(x = illust.badgeLeft, y = illust.badgeTop))
            }
            Text(
                text = stringResource(R.string.card_drive_record_empty_message),
                style = AlterTheme.typography.labelSmall,
                color = AlterTheme.colors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** badge_alert. 왼쪽 아래 모서리만 4로 뾰족하다 */
@Composable
private fun AlertBadge(illust: EmptyIllustSpec, modifier: Modifier = Modifier) {
    val size = illust.badgeSize
    val shape = RoundedCornerShape(topStart = size / 2, topEnd = size / 2, bottomEnd = size / 2, bottomStart = EmptyBadgeTipRadius)
    Box(
        modifier = modifier
            .size(size)
            .alterShadow(EmptyBadgeShadow, size / 2)
            .clip(shape)
            .background(AlterTheme.colors.blue500),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "!",
            style = illust.badgeTextStyle().copy(fontWeight = AlterFontWeight.Strong),
            color = Color.White,
        )
    }
}

/** Figma illust_no_record */
private class EmptyIllustSpec(
    val width: Dp,
    val height: Dp,
    val carTop: Dp,
    val carHeight: Dp,
    val badgeLeft: Dp,
    val badgeTop: Dp,
    val badgeSize: Dp,
    val badgeTextStyle: @Composable () -> TextStyle,
)

/** 200×160: 차 200×134 (y 26), 느낌표 40 (150, 12) */
private val EmptyIllustRegular = EmptyIllustSpec(
    width = 200.dp, height = 160.dp, carTop = 26.dp, carHeight = 134.dp,
    badgeLeft = 150.dp, badgeTop = 12.dp, badgeSize = 40.dp,
    badgeTextStyle = { AlterTheme.typography.bodySmall },
)

/** WF 가로 85×68: 차 85×57 (y 11), 느낌표 28 (64, 5) */
private val EmptyIllustCompact = EmptyIllustSpec(
    width = 85.dp, height = 68.dp, carTop = 11.dp, carHeight = 57.dp,
    badgeLeft = 64.dp, badgeTop = 5.dp, badgeSize = 28.dp,
    badgeTextStyle = { AlterTheme.typography.labelXSmall },
)

private val EmptyOverlayBottomInset = 30.dp
private val EmptyOverlayRadius = 16.dp
private const val EmptyOverlayAlpha = 1f
private val EmptyBadgeTipRadius = 4.dp

/** 0 4 12, blue(59, 110, 224) 30% */
private val EmptyBadgeShadow = listOf(ShadowLayer(offsetY = 4.dp, blur = 12.dp, color = Color(0x4D3B6EE0)))
