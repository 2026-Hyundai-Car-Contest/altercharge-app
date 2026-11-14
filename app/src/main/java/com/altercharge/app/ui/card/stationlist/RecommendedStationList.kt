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
import com.altercharge.app.data.station.ConnectorFilter
import com.altercharge.app.data.station.POWER_STEPS
import com.altercharge.app.data.station.PowerRange
import com.altercharge.app.data.station.StationFilter
import com.altercharge.app.data.station.StationSort
import com.altercharge.app.ui.card.info.lastCheckedText
import com.altercharge.app.ui.card.station.ChargingType
import com.altercharge.app.ui.card.stationitem.CardItemUiState
import com.altercharge.app.ui.card.stationitem.StationItem
import com.altercharge.app.ui.common.ChargePowerSlider
import com.altercharge.app.ui.common.dropdown.DropDownWrapper
import com.altercharge.app.ui.common.dropdown.SelectableDropdownItem
import com.altercharge.app.ui.common.dropdown.FilterChip
import com.altercharge.app.ui.common.dropdown.SortDropDown
import com.altercharge.app.ui.common.dropdown.StationSortType
import com.altercharge.app.ui.common.searchbar.SearchBar
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.station.StationListMessage
import com.altercharge.app.ui.station.StationListUiState
import com.altercharge.app.ui.station.VoiceSearchState

/** 연결 타입 필터 한 줄 (REQ_SL_005). 설명 문구는 시안 핸드오프 값이다 */
private data class ConnectorOption(
    val filter: ConnectorFilter,
    val title: String,
    val description: String,
)

@Composable
private fun connectorOptions(): List<ConnectorOption> = listOf(
    ConnectorOption(
        filter = ConnectorFilter.AC_SLOW,
        title = "${stringResource(R.string.card_item_connector_ac_slow)} (5핀, Type 1)",
        description = "AC 단상 · 220V · 국내 대부분 차량",
    ),
    ConnectorOption(
        filter = ConnectorFilter.AC_THREE_PHASE,
        title = "${stringResource(R.string.card_item_connector_ac_three_phase)} (7핀, Type 2)",
        description = "AC 3상 · 380V · 르노 조에 등 일부",
    ),
    ConnectorOption(
        filter = ConnectorFilter.DC_CHADEMO,
        title = "${stringResource(R.string.card_item_connector_dc_chademo)} (CHAdeMO)",
        description = "DC 급속 · 약 50~500V · 구형 쏘울EV",
    ),
    ConnectorOption(
        filter = ConnectorFilter.DC_COMBO,
        title = "${stringResource(R.string.card_item_connector_dc_combo)} (CCS1)",
        description = "DC 급속 · 약 200~1000V · 현대 · 기아",
    ),
    ConnectorOption(
        filter = ConnectorFilter.NACS,
        title = stringResource(R.string.card_item_connector_nacs),
        description = "DC 급속 · 테슬라 표준 · 신규 충전소",
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
) {
    val options = connectorOptions()
    val filter = state.filter

    // 연결 타입 필터: 적용 전 선택은 메뉴 안에서만 바꾼다 (적용을 눌러야 목록에 반영)
    var isConnectorMenuExpanded by remember { mutableStateOf(false) }
    var pendingConnectors by remember { mutableStateOf(filter.connectors) }

    // 속도 (충전 출력) 필터
    var isPowerMenuExpanded by remember { mutableStateOf(false) }
    var pendingMinPower by remember { mutableIntStateOf(filter.power.minKw) }
    var pendingMaxPower by remember { mutableIntStateOf(filter.power.maxKw) }

    val sortLabels = StationSortType.entries.associateWith { stringResource(it.labelRes) }

    // 방문 기록이 없으면 방문순을 고를 수 없다 (REQ_SL_004 예외)
    val sortItems = if (state.visitSortEnabled) {
        listOf(StationSortType.DISTANCE, StationSortType.VISIT)
    } else {
        listOf(StationSortType.DISTANCE)
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
            style = AlterTheme.typography.bodyXSmall,
            color = AlterTheme.colors.textPrimary,
            textAlign = TextAlign.Left,
        )

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 정렬/필터 아이콘
            // Figma button_filter: 배경 없는 22 아이콘, 원형 히트 영역 40
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_button_filter),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(AlterTheme.colors.contentNeutral),
                    modifier = Modifier.size(22.dp),
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 거리순 / 방문순 드롭다운 (REQ_SL_004)
                SortDropDown(
                    items = sortItems,
                    selectedItem = state.sort.toSortType(),
                    itemLabel = { sortLabels.getValue(it) },
                    onItemSelected = { onSortChange(it.toSort()) },
                )

                // 연결 타입 필터 드롭다운 (REQ_SL_005)
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    val isFiltered = filter.connectors.isNotEmpty()
                    val connectorChipLabel = when {
                        filter.connectors.isEmpty() -> stringResource(R.string.station_list_filter_connector_all)
                        filter.connectors.size == 1 -> options.firstOrNull { it.filter == filter.connectors.first() }
                            ?.title?.substringBefore(" (") ?: stringResource(R.string.station_list_filter_connector_all)
                        else -> stringResource(R.string.station_list_filter_connector_count, filter.connectors.size)
                    }

                    FilterChip(
                        label = connectorChipLabel,
                        selected = isFiltered,
                        expanded = isConnectorMenuExpanded,
                        onClick = {
                            pendingConnectors = filter.connectors
                            isConnectorMenuExpanded = true
                        },
                    )

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
                            onReset = { pendingConnectors = emptySet() },
                            onApply = {
                                onFilterChange(filter.copy(connectors = pendingConnectors))
                                isConnectorMenuExpanded = false
                            },
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                options.forEach { option ->
                                    SelectableDropdownItem(
                                        title = option.title,
                                        description = option.description,
                                        selected = option.filter in pendingConnectors,
                                        onClick = {
                                            pendingConnectors = if (option.filter in pendingConnectors) {
                                                pendingConnectors - option.filter
                                            } else {
                                                pendingConnectors + option.filter
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                // 속도 (충전 출력) 필터 드롭다운 (REQ_SL_005)
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    val isFiltered = !filter.power.isAll
                    val powerChipLabel = if (isFiltered) {
                        if (filter.power.normalizedMaxKw >= POWER_STEPS.last()) {
                            stringResource(
                                R.string.station_list_filter_power_range_over,
                                filter.power.normalizedMinKw,
                                filter.power.normalizedMaxKw,
                            )
                        } else {
                            stringResource(
                                R.string.station_list_filter_power_range,
                                filter.power.normalizedMinKw,
                                filter.power.normalizedMaxKw,
                            )
                        }
                    } else {
                        stringResource(R.string.station_list_filter_power_all)
                    }

                    FilterChip(
                        label = powerChipLabel,
                        selected = isFiltered,
                        expanded = isPowerMenuExpanded,
                        onClick = {
                            pendingMinPower = filter.power.minKw
                            pendingMaxPower = filter.power.maxKw
                            isPowerMenuExpanded = true
                        },
                    )

                    DropdownMenu(
                        expanded = isPowerMenuExpanded,
                        onDismissRequest = { isPowerMenuExpanded = false },
                        shape = RoundedCornerShape(AlterRadius.Large),
                        containerColor = AlterTheme.colors.containerBackgroundSubtle,
                        tonalElevation = 0.dp,
                    ) {
                        DropDownWrapper(
                            title = "속도 (충전 출력)",
                            subTitle = if (pendingMaxPower >= POWER_STEPS.last()) {
                                stringResource(R.string.station_list_filter_power_range_over, pendingMinPower, pendingMaxPower)
                            } else {
                                stringResource(R.string.station_list_filter_power_range, pendingMinPower, pendingMaxPower)
                            },
                            onReset = {
                                pendingMinPower = POWER_STEPS.first()
                                pendingMaxPower = POWER_STEPS.last()
                            },
                            onApply = {
                                onFilterChange(filter.copy(power = PowerRange(pendingMinPower, pendingMaxPower)))
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
        }

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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.items) { station ->
                StationItem(
                    state = station,
                    modifier = Modifier.clickable { onStationClick(station) },
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
    state.offline && state.lastChecked != null ->
        stringResource(R.string.station_list_offline_notice, lastCheckedText(state.lastChecked))
    else -> null
}

private fun StationSort.toSortType(): StationSortType = when (this) {
    StationSort.DISTANCE -> StationSortType.DISTANCE
    StationSort.VISIT -> StationSortType.VISIT
}

private fun StationSortType.toSort(): StationSort = when (this) {
    StationSortType.DISTANCE -> StationSort.DISTANCE
    StationSortType.VISIT -> StationSort.VISIT
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
