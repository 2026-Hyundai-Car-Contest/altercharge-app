package com.altercharge.app.ui.card.stationlist

import com.altercharge.app.ui.common.dropdown.FilterRowBounds
import kotlin.math.roundToInt
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.data.station.ConnectorFilter
import com.altercharge.app.data.station.POWER_STEPS
import com.altercharge.app.data.station.PowerRange
import com.altercharge.app.data.station.StationFilter
import com.altercharge.app.data.station.StationSort
import com.altercharge.app.ui.common.ChargePowerSlider
import com.altercharge.app.ui.common.dropdown.DropDownWrapper
import com.altercharge.app.ui.common.dropdown.FilterChip
import com.altercharge.app.ui.common.dropdown.FilterDropdownPopup
import com.altercharge.app.ui.common.dropdown.SelectableDropdownItem
import com.altercharge.app.ui.common.dropdown.SortDropDown
import com.altercharge.app.ui.common.dropdown.StationSortType
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.station.StationListUiState

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
        title = "${stringResource(R.string.card_item_connector_ac_slow)} ${stringResource(R.string.station_list_connector_ac_slow_spec)}",
        description = stringResource(R.string.station_list_connector_ac_slow_desc),
    ),
    ConnectorOption(
        filter = ConnectorFilter.AC_THREE_PHASE,
        title = "${stringResource(R.string.card_item_connector_ac_three_phase)} ${stringResource(R.string.station_list_connector_ac_three_phase_spec)}",
        description = stringResource(R.string.station_list_connector_ac_three_phase_desc),
    ),
    ConnectorOption(
        filter = ConnectorFilter.DC_CHADEMO,
        title = "${stringResource(R.string.card_item_connector_dc_chademo)} (CHAdeMO)",
        description = stringResource(R.string.station_list_connector_dc_chademo_desc),
    ),
    ConnectorOption(
        filter = ConnectorFilter.DC_COMBO,
        title = "${stringResource(R.string.card_item_connector_dc_combo)} (CCS1)",
        description = stringResource(R.string.station_list_connector_dc_combo_desc),
    ),
    ConnectorOption(
        filter = ConnectorFilter.NACS,
        title = stringResource(R.string.card_item_connector_nacs),
        description = stringResource(R.string.station_list_connector_nacs_desc),
    ),
)

/**
 * R2 필터 줄: 필터 아이콘 + 정렬 · 연결 타입 · 속도 칩 (Figma filter_row). 칩은 한 줄 가로 스크롤.
 * 연결 타입 · 속도는 메뉴 안에서 고른 값을 "적용"을 눌러야 목록에 반영한다 (REQ_SL_004 · REQ_SL_005).
 */
@Composable
internal fun StationListFilterRow(
    state: StationListUiState,
    onSortChange: (StationSort) -> Unit,
    onFilterChange: (StationFilter) -> Unit,
    /** 정렬 · 연결 타입 · 속도 메뉴 중 하나라도 열려 있으면 true. 목록을 어둡게 덮는 데 쓴다 (Figma scrim) */
    onMenuOpenChange: (Boolean) -> Unit = {},
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

    // Figma dropdown_sort는 거리순 · 방문순 두 항목이다. 방문 기록이 없으면 방문순은 흐리게 두고 고를 수 없다 (REQ_SL_004 예외)
    val sortItems = listOf(StationSortType.DISTANCE, StationSortType.VISIT)
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    val anyMenuOpen = isSortMenuExpanded || isConnectorMenuExpanded || isPowerMenuExpanded
    LaunchedEffect(anyMenuOpen) { onMenuOpenChange(anyMenuOpen) }

    // 드롭다운을 패널 폭에 맞추려고 필터 줄의 자리를 잰다 (디자이너 QA 06)
    var rowBounds by remember { mutableStateOf<FilterRowBounds?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val rect = coordinates.boundsInWindow()
                rowBounds = FilterRowBounds(left = rect.left.roundToInt(), width = rect.width.roundToInt(), bottom = rect.bottom.roundToInt())
            },
        horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Figma button_filter: 히트 영역 40 안의 15x9 글리프. 벡터가 40 viewport라 40으로 그린다
        Image(
            painter = painterResource(R.drawable.ic_button_filter),
            contentDescription = null,
            colorFilter = ColorFilter.tint(AlterTheme.colors.contentNeutral),
            modifier = Modifier.size(40.dp),
        )

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
                isEnabled = { it != StationSortType.VISIT || state.visitSortEnabled },
                onExpandedChange = { isSortMenuExpanded = it },
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

                FilterDropdownPopup(
                    expanded = isConnectorMenuExpanded,
                    onDismissRequest = { isConnectorMenuExpanded = false },
                    panel = rowBounds,
                ) {
                    DropDownWrapper(
                        title = stringResource(R.string.station_list_filter_connector_title),
                        subTitle = stringResource(R.string.station_list_filter_connector_subtitle),
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

                FilterDropdownPopup(
                    expanded = isPowerMenuExpanded,
                    onDismissRequest = { isPowerMenuExpanded = false },
                    panel = rowBounds,
                ) {
                    DropDownWrapper(
                        title = stringResource(R.string.station_list_filter_power_title),
                        subTitle = if (pendingMaxPower >= POWER_STEPS.last()) {
                            stringResource(R.string.station_list_filter_power_meta_over, pendingMinPower, pendingMaxPower)
                        } else {
                            stringResource(R.string.station_list_filter_power_meta, pendingMinPower, pendingMaxPower)
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
}

private fun StationSort.toSortType(): StationSortType = when (this) {
    StationSort.DISTANCE -> StationSortType.DISTANCE
    StationSort.VISIT -> StationSortType.VISIT
}

private fun StationSortType.toSort(): StationSort = when (this) {
    StationSortType.DISTANCE -> StationSort.DISTANCE
    StationSortType.VISIT -> StationSort.VISIT
}
