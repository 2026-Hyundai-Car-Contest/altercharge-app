package com.altercharge.app.ui.card.stationlist

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.altercharge.app.ui.card.common.ChargerSpeed
import com.altercharge.app.ui.designsystem.component.PlaceholderBox
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme

/**
 * R2 충전소 목록 패널. 담당: 석기님
 *
 * 선택 동작(Figma card_station 스펙): 항목을 누르면 선택, 같은 항목을 다시 누르거나 정렬을 바꾸면 선택 해제.
 */
@Composable
fun PanelStationList(
    state: PanelStationListUiState,
    onQueryChange: (String) -> Unit,
    onSortChange: (StationSort) -> Unit,
    onSpeedFilterChange: (ChargerSpeed?) -> Unit,
    onStationClick: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO 석기님: Figma panel_station_list (366:38178) 구현
    PlaceholderBox("R2 panel_station_list · ${state.items.size}곳 · ${state.sort}", modifier)
}

@Preview(widthDp = 383, heightDp = 648)
@Composable
private fun PanelStationListPreview() {
    AlterChargeTheme {
        PanelStationList(
            state = PanelStationListUiState(
                query = "",
                sort = StationSort.DISTANCE,
                speedFilter = null,
                items = listOf(
                    StationItemUiState(
                        id = "1",
                        name = "연희동 주민센터",
                        distanceKm = 1.2,
                        address = "서울 서대문구 연희로 140",
                        speed = ChargerSpeed.FAST,
                        availableCount = 2,
                        totalCount = 4,
                        savedPlace = null,
                    ),
                ),
                selectedId = null,
            ),
            onQueryChange = {},
            onSortChange = {},
            onSpeedFilterChange = {},
            onStationClick = {},
        )
    }
}
