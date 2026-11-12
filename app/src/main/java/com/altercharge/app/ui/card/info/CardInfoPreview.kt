/** L1 카드 미리보기 */
package com.altercharge.app.ui.card.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

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
