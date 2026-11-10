package com.altercharge.app.ui.card.station

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme

/**
 * R1 - 충전소 카드(Light/KO)
 * */

@Preview(name = "R1 - 충전소 카드(Light/KO)", widthDp = 383, heightDp = 368, locale = "ko")
@Composable
private fun CardStationPreviewLigthAvailableKo() {
    AlterChargeTheme() {
        CardStation(state = samplePreviewUiState, modifier = Modifier)
    }
}

@Preview(name = "R1 - 충전소 카드(Light/En)", widthDp = 383, heightDp = 368, locale = "en")
@Composable
private fun CardStationPreviewLigthAvailableEn() {
    AlterChargeTheme() {
        CardStation(state = samplePreviewUiState, modifier = Modifier)
    }
}

@Preview(name = "R1 - 충전소 카드(Dark/KO)", widthDp = 383, heightDp = 368, locale = "ko")
@Composable
private fun CardStationPreviewDarkAvailableKo() {
    AlterChargeTheme(darkTheme = true) {
        CardStation(state = samplePreviewUiState, modifier = Modifier)
    }
}

@Preview(name = "R1 - 충전소 카드(Dark/En)", widthDp = 383, heightDp = 368, locale = "ko")
@Composable
private fun CardStationPreviewDarkAvailableEn() {
    AlterChargeTheme(darkTheme = true) {
        CardStation(state = samplePreviewUiState, modifier = Modifier)
    }
}
