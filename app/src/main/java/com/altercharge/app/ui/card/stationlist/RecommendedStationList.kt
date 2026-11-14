package com.altercharge.app.ui.card.stationlist

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.station.StationFilter
import com.altercharge.app.data.station.StationSort
import com.altercharge.app.ui.card.info.lastCheckedText
import com.altercharge.app.ui.card.station.ChargingType
import com.altercharge.app.ui.card.stationitem.CardItemUiState
import com.altercharge.app.ui.card.stationitem.StationItem
import com.altercharge.app.ui.common.searchbar.SearchBar
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.station.StationListMessage
import com.altercharge.app.ui.station.StationListUiState
import com.altercharge.app.ui.station.VoiceSearchState

/** 미리보기용 값. 실제 화면은 ViewModel이 만든 state를 넘긴다 */
private val previewStations = listOf(
    CardItemUiState(
        stationId = "ST-001",
        stationName = "홍대입구역 공영주차장",
        availableChargerCount = 2,
        totalChargerCount = 4,
        chargingType = ChargingType.DC,
        distance = 0.6,
        chargeStationAddress = "서울 마포구 양화로 160",
        connectorTypes = listOf(ConnectorType.DC_COMBO),
        visitCount = 3,
    ),
    CardItemUiState(
        stationId = "ST-002",
        stationName = "연남동 공영주차장",
        availableChargerCount = 3,
        totalChargerCount = 3,
        chargingType = ChargingType.DC,
        distance = 1.2,
        chargeStationAddress = "서울 마포구 동교로 263",
        connectorTypes = listOf(ConnectorType.DC_CHADEMO, ConnectorType.AC_3PHASE),
    ),
    CardItemUiState(
        stationId = "ST-003",
        stationName = "연희동 주민센터",
        availableChargerCount = 1,
        totalChargerCount = 1,
        chargingType = ChargingType.AC,
        distance = 1.8,
        chargeStationAddress = "서울 서대문구 연희로 140",
        connectorTypes = listOf(ConnectorType.AC_SLOW),
        visitCount = 1,
    ),
)

/**
 * R2 주변 충전소 목록 (REQ_SL_001 ~ REQ_SL_005).
 * 검색 · 정렬 · 필터는 [state]의 값을 따르고, 바뀌면 콜백으로 알린다 (값은 ViewModel이 만든다).
 */
@Composable
fun RecommendedStationList(
    modifier: Modifier = Modifier,
    state: StationListUiState,
    onQueryChange: (String) -> Unit = {},
    onMicClick: () -> Unit = {},
    onSortChange: (StationSort) -> Unit = {},
    onFilterChange: (StationFilter) -> Unit = {},
    onStationClick: (CardItemUiState) -> Unit = {},
    /** R1에 대신 보여 주는 충전소 (REQ_SL_006). 목록에서 강조한다 */
    selectedStationId: String? = null,
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(AlterRadius.XXLarge))
            // Figma bg_glow: 회색 바탕 위에 아래로 갈수록 진해지는 blue_100(55%). 바탕이 blue_50이면 선택 칩 배경이 묻힌다
            .background(AlterTheme.colors.containerBackgroundSubtle)
            .background(
                Brush.verticalGradient(
                    0f to AlterTheme.colors.blue100.copy(alpha = 0f),
                    0.55f to AlterTheme.colors.blue100.copy(alpha = 0.35f * 0.55f),
                    1f to AlterTheme.colors.blue100.copy(alpha = 0.55f),
                ),
            )
            .padding(AlterMargin.Normal),
    ) {
        // 타이틀. 오프라인으로 저장한 목록이면 옆에 "N분 전 기준" (Figma header · title_row · last_checked, REQ_CM_006)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.station_list_title),
                style = AlterTheme.typography.bodyXSmall,
                color = AlterTheme.colors.textPrimary,
                textAlign = TextAlign.Left,
                modifier = Modifier.alignByBaseline(),
            )
            val lastChecked = state.lastChecked
            if (state.offline && lastChecked != null) {
                Text(
                    text = lastCheckedText(lastChecked),
                    style = AlterTheme.typography.labelXSmall,
                    color = AlterTheme.colors.textTertiary,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }

        // 받아 둔 충전소가 아예 없으면 헤더만 남기고 검색 · 필터 · 목록을 숨긴다 (Figma selection = empty, 807:59523).
        // 검색 · 필터 결과가 없을 때는 조건을 바꿀 수 있게 그대로 둔다
        if (state.showEmptyIllustration) {
            StationListEmpty(modifier = Modifier.weight(1f))
            return@Column
        }

        // 1. 검색바 (REQ_SL_002 · REQ_SL_003)
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            value = state.query,
            onValueChange = onQueryChange,
            onMicClick = onMicClick,
            offline = state.offline,
        )

        // 2. 필터 영역 (정렬, 연결 타입, 속도)
        StationListFilterRow(
            state = state,
            onSortChange = onSortChange,
            onFilterChange = onFilterChange,
        )

        // 안내 줄: 불러오는 중 · 빈 목록 · 오프라인 · 음성 안내 (REQ_CM_006 · REQ_SL_003)
        listNotice(state)?.let { notice ->
            Text(
                text = notice,
                fontSize = 15.sp,
                color = AlterTheme.colors.textTertiary,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        // 3. 충전소 목록
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            // Figma list gap 8
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.items) { station ->
                StationItem(
                    state = station,
                    modifier = Modifier.clickable { onStationClick(station) },
                    selected = station.stationId == selectedStationId,
                )
            }
        }
    }
}

@Composable
private fun StationListEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.img_empty_station_alert),
            contentDescription = null,
            modifier = Modifier.size(width = 260.dp, height = 188.dp),
        )
        Text(
            text = stringResource(R.string.station_list_empty_title),
            style = AlterTheme.typography.bodyXSmall,
            color = AlterTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun listNotice(state: StationListUiState): String? = when {
    state.voice == VoiceSearchState.Listening -> stringResource(R.string.station_list_voice_listening)
    state.message == StationListMessage.SEARCH_UNAVAILABLE -> stringResource(R.string.station_list_offline_search_blocked)
    state.message == StationListMessage.VOICE_PERMISSION -> stringResource(R.string.station_list_voice_permission)
    state.message == StationListMessage.VOICE_UNAVAILABLE -> stringResource(R.string.station_list_voice_unavailable)
    state.message == StationListMessage.VOICE_ERROR -> stringResource(R.string.station_list_voice_error)
    state.isFilterEmpty -> stringResource(R.string.station_list_filter_empty)
    state.isLoading && state.items.isEmpty() -> stringResource(R.string.station_list_loading)
    // 검색 때문에 비었으면 반경 문구("3km 안에 없음") 대신 검색 결과 문구
    state.query.isNotBlank() && state.items.isEmpty() -> stringResource(R.string.station_list_search_empty)
    state.items.isEmpty() -> stringResource(R.string.station_list_empty)
    else -> null
}


private val previewState = StationListUiState(items = previewStations, visitSortEnabled = true)

@Preview(name = "RecommendedStationList(Light)", widthDp = 383, heightDp = 600)
@Composable
private fun RecommendedStationListLightPreview() {
    AlterChargeTheme(darkTheme = false) {
        RecommendedStationList(state = previewState)
    }
}

@Preview(name = "RecommendedStationList(Dark)", widthDp = 383, heightDp = 600)
@Composable
private fun RecommendedStationListDarkPreview() {
    AlterChargeTheme(darkTheme = true) {
        RecommendedStationList(state = previewState)
    }
}
