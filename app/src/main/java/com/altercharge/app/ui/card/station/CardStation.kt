package com.altercharge.app.ui.card.station

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.altercharge.app.ui.card.common.ChargerSpeed
import com.altercharge.app.ui.designsystem.component.PlaceholderBox
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme

/**
 * R1 추천 충전소 카드. 담당: 석기님
 *
 * @param onNavigate "여기로 안내"를 누르면 호출한다. 내비 경로 요청은 화면 쪽에서 연결한다.
 */
@Composable
fun CardStation(
    state: CardStationUiState,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO 석기님: Figma card_station (366:38369) 구현
    PlaceholderBox("R1 card_station · ${state.mode} · ${state.name}", modifier)
}

@Preview(widthDp = 383, heightDp = 368)
@Composable
private fun CardStationPreview() {
    AlterChargeTheme {
        CardStation(
            state = CardStationUiState(
                mode = StationCardMode.RECOMMENDED,
                name = "고덕 그라시움 급속",
                distanceKm = 1.8,
                driveMinutes = 5,
                speed = ChargerSpeed.FAST,
                powerKw = 100,
                availableCount = 2,
                totalCount = 4,
                visitCount = 3,
                lastVisitDate = null,
                pricePerKwh = null,
                unavailableReason = null,
            ),
            onNavigate = {},
        )
    }
}
