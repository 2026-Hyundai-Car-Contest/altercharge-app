package com.altercharge.app.ui.card.info

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.card.common.ChargeVerdict
import com.altercharge.app.ui.designsystem.component.PlaceholderBox
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme

/** L1 오늘 충전 판정 카드. 담당: 서희님 */
@Composable
fun CardInfo(
    state: CardInfoUiState,
    modifier: Modifier = Modifier,
) {
    // TODO 서희님: Figma card_info (366:38742) 구현
    PlaceholderBox("L1 card_info · ${state.verdict} · D-${state.daysUntilCharge}", modifier)
}

@Preview(widthDp = 383, heightDp = 368)
@Composable
private fun CardInfoPreview() {
    AlterChargeTheme {
        CardInfo(
            state = CardInfoUiState(
                verdict = ChargeVerdict.TODAY,
                daysUntilCharge = 0,
                placeName = "명일 신동아 아파트",
            ),
            modifier = Modifier.height(368.dp),
        )
    }
}
