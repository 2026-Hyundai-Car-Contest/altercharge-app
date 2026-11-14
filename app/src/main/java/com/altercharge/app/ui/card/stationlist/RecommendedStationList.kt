package com.altercharge.app.ui.card.stationlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.ui.card.info.lastCheckedText
import com.altercharge.app.ui.card.station.ChargingType
import com.altercharge.app.ui.card.stationitem.CardItemUiState
import com.altercharge.app.ui.card.stationitem.StationItem
import com.altercharge.app.ui.common.ChargePowerSlider
import com.altercharge.app.ui.common.dropdown.DropDownWrapper
import com.altercharge.app.ui.common.dropdown.SelectableDropdownItem
import com.altercharge.app.ui.common.dropdown.SortDropDown
import com.altercharge.app.ui.common.dropdown.StationSortType
import com.altercharge.app.ui.common.searchbar.SearchBar
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.station.StationListUiState

data class ConnectorTypeFilter(
    val id: String,
    val title: String,
    val description: String,
)

val defaultConnectorOptions = listOf(
    ConnectorTypeFilter(
        id = "ac_slow",
        title = "AC 완속 (5핀, Type 1)",
        description = "AC 단상 · 220V · 국내 대부분 차량",
    ),
    ConnectorTypeFilter(
        id = "ac_3phase",
        title = "AC 3상 (7핀, Type 2)",
        description = "AC 3상 · 380V · 르노 조에 등 일부",
    ),
    ConnectorTypeFilter(
        id = "dc_chademo",
        title = "DC 차데모 (CHAdeMO)",
        description = "DC 급속 · 약 50~500V · 구형 쏘울EV...",
    ),
    ConnectorTypeFilter(
        id = "dc_combo",
        title = "DC 콤보 (CCS1)",
        description = "DC 급속 · 약 200~1000V · 현대 · 기아",
    ),
)

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
 * R2 주변 충전소 목록 (REQ_SL_001). 받은 목록은 [state]에서 온다.
 */
@Composable
fun RecommendedStationList(
    modifier: Modifier = Modifier,
    state: StationListUiState,
    onMicClick: () -> Unit = {},
    onStationClick: (CardItemUiState) -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSortType by remember { mutableStateOf(StationSortType.DISTANCE) }

    // 연결 타입 필터 상태
    var isConnectorMenuExpanded by remember { mutableStateOf(false) }
    var selectedConnectors by remember { mutableStateOf(setOf<String>()) }
    var pendingConnectors by remember { mutableStateOf(setOf<String>()) }

    // 속도 (충전 출력) 필터 상태
    var isPowerMenuExpanded by remember { mutableStateOf(false) }
    var minPower by remember { mutableIntStateOf(3) }
    var maxPower by remember { mutableIntStateOf(350) }
    var pendingMinPower by remember { mutableIntStateOf(3) }
    var pendingMaxPower by remember { mutableIntStateOf(350) }

    // 검색어 및 정렬 필터링
    val filteredStations = remember(state.items, searchQuery, selectedSortType) {
        val stations = state.items
        val list = if (searchQuery.isBlank()) {
            stations
        } else {
            stations.filter {
                it.stationName.contains(searchQuery, ignoreCase = true) ||
                    it.chargeStationAddress.contains(searchQuery, ignoreCase = true)
            }
        }
        when (selectedSortType) {
            StationSortType.DISTANCE -> list.sortedBy { it.distance }
            StationSortType.VISIT -> list
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(AlterRadius.XXLarge))
            .background(AlterTheme.colors.blue50)
            .padding(AlterMargin.Normal),
    ) {
        // 타이틀
        Text(
            text = stringResource(R.string.station_list_title),
            fontWeight = AlterFontWeight.Strong,
            fontSize = 28.sp,
            color = AlterTheme.colors.textPrimary,
            textAlign = TextAlign.Left,
        )

        // 1. 검색바
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { searchQuery = it },
            onMicClick = onMicClick,
        )

        // 2. 필터 영역 (정렬 아이콘, 거리순/방문순, 연결 타입, 속도)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 정렬/필터 아이콘
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AlterTheme.colors.surfaceHigh),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_button_filter),
                    contentDescription = "정렬 필터",
                    colorFilter = ColorFilter.tint(AlterTheme.colors.contentNeutral),
                    modifier = Modifier.size(16.dp),
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 거리순 / 방문순 드롭다운
                SortDropDown(
                    items = listOf(StationSortType.DISTANCE, StationSortType.VISIT),
                    selectedItem = selectedSortType,
                    itemLabel = { it.label },
                    onItemSelected = { selectedSortType = it },
                )

                // 연결 타입 필터 드롭다운
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    val isFiltered = selectedConnectors.isNotEmpty()
                    val connectorChipLabel = when {
                        selectedConnectors.isEmpty() -> "연결 타입 전체"
                        selectedConnectors.size == 1 -> {
                            defaultConnectorOptions.firstOrNull { it.id == selectedConnectors.first() }
                                ?.title?.substringBefore(" (") ?: "연결 타입 1개"
                        }

                        else -> "연결 타입 ${selectedConnectors.size}개"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .wrapContentSize()
                            .clip(RoundedCornerShape(AlterRadius.Full))
                            .background(
                                if (isFiltered || isConnectorMenuExpanded) AlterTheme.colors.blue50
                                else AlterTheme.colors.surfaceHigh,
                            )
                            .clickable {
                                pendingConnectors = selectedConnectors
                                isConnectorMenuExpanded = true
                            }
                            .padding(start = 14.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
                    ) {
                        Text(
                            text = connectorChipLabel,
                            color = if (isFiltered || isConnectorMenuExpanded) AlterTheme.colors.blue500
                            else AlterTheme.colors.textSecondary,
                            fontWeight = AlterFontWeight.Strong,
                            fontSize = 15.sp,
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                if (isFiltered || isConnectorMenuExpanded) AlterTheme.colors.blue500
                                else AlterTheme.colors.textSecondary,
                            ),
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(if (isConnectorMenuExpanded) -90F else 90F),
                        )
                    }

                    DropdownMenu(
                        expanded = isConnectorMenuExpanded,
                        onDismissRequest = { isConnectorMenuExpanded = false },
                        shape = RoundedCornerShape(AlterRadius.Large),
                        containerColor = AlterTheme.colors.containerBackgroundSubtle,
                        tonalElevation = 0.dp,
                    ) {
                        DropDownWrapper(
                            title = "연결 타입",
                            subTitle = "여러 개 선택",
                            onReset = {
                                pendingConnectors = emptySet()
                            },
                            onApply = {
                                selectedConnectors = pendingConnectors
                                isConnectorMenuExpanded = false
                            },
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                defaultConnectorOptions.forEach { option ->
                                    SelectableDropdownItem(
                                        title = option.title,
                                        description = option.description,
                                        selected = option.id in pendingConnectors,
                                        onClick = {
                                            pendingConnectors = if (option.id in pendingConnectors) {
                                                pendingConnectors - option.id
                                            } else {
                                                pendingConnectors + option.id
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 속도 (충전 출력) 필터 드롭다운
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                val isFiltered = (minPower > 3 || maxPower < 350)
                val powerChipLabel = if (isFiltered) {
                    "${minPower}~${maxPower}kW"
                } else {
                    "속도 전체"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(AlterRadius.Full))
                        .background(
                            if (isFiltered || isPowerMenuExpanded) AlterTheme.colors.blue50
                            else AlterTheme.colors.surfaceHigh,
                        )
                        .clickable {
                            pendingMinPower = minPower
                            pendingMaxPower = maxPower
                            isPowerMenuExpanded = true
                        }
                        .padding(start = 14.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
                ) {
                    Text(
                        text = powerChipLabel,
                        color = if (isFiltered || isPowerMenuExpanded) AlterTheme.colors.blue500
                        else AlterTheme.colors.textSecondary,
                        fontWeight = AlterFontWeight.Strong,
                        fontSize = 15.sp,
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            if (isFiltered || isPowerMenuExpanded) AlterTheme.colors.blue500
                            else AlterTheme.colors.textSecondary,
                        ),
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(if (isPowerMenuExpanded) -90F else 90F),
                    )
                }

                DropdownMenu(
                    expanded = isPowerMenuExpanded,
                    onDismissRequest = { isPowerMenuExpanded = false },
                    shape = RoundedCornerShape(AlterRadius.Large),
                    containerColor = AlterTheme.colors.containerBackgroundSubtle,
                    tonalElevation = 0.dp,
                ) {
                    DropDownWrapper(
                        title = "속도 (충전 출력)",
                        subTitle = "${pendingMinPower}kW ~ ${pendingMaxPower}kW 이상",
                        onReset = {
                            pendingMinPower = 3
                            pendingMaxPower = 350
                        },
                        onApply = {
                            minPower = pendingMinPower
                            maxPower = pendingMaxPower
                            isPowerMenuExpanded = false
                        },
                    ) {
                        ChargePowerSlider(
                            minPower = pendingMinPower,
                            maxPower = pendingMaxPower,
                            onRangeChange = { min, max ->
                                pendingMinPower = min
                                pendingMaxPower = max
                            },
                        )
                    }
                }
            }
        }

        // 안내 줄: 불러오는 중 · 빈 목록 · 오프라인 (REQ_CM_006)
        listNotice(state)?.let { notice ->
            Text(
                text = notice,
                fontSize = 15.sp,
                color = AlterTheme.colors.textTertiary,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        // 3. 충전소 목록 (스크롤 가능한 LazyColumn)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(filteredStations) { station ->
                StationItem(
                    state = station,
                    modifier = Modifier.clickable { onStationClick(station) },
                )
            }
        }
    }
}

@Composable
private fun listNotice(state: StationListUiState): String? = when {
    state.isLoading && state.items.isEmpty() -> stringResource(R.string.station_list_loading)
    state.items.isEmpty() -> stringResource(R.string.station_list_empty)
    state.offline && state.lastChecked != null ->
        stringResource(R.string.station_list_offline_notice, lastCheckedText(state.lastChecked))
    else -> null
}

private val previewState = StationListUiState(items = previewStations)

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
