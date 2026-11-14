package com.altercharge.app.ui.card.stationlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
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
import com.altercharge.app.ui.common.dropdown.SelectableDropdownItem
import com.altercharge.app.ui.common.dropdown.SortDropDown
import com.altercharge.app.ui.common.dropdown.StationSortType
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterRadius
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

                DropdownMenu(
                    expanded = isPowerMenuExpanded,
                    onDismissRequest = { isPowerMenuExpanded = false },
                    shape = RoundedCornerShape(AlterRadius.Large),
                    containerColor = AlterTheme.colors.containerBackgroundSubtle,
                    tonalElevation = 0.dp,
                ) {
                    DropDownWrapper(
                        title = stringResource(R.string.station_list_filter_power_title),
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
}

private fun StationSort.toSortType(): StationSortType = when (this) {
    StationSort.DISTANCE -> StationSortType.DISTANCE
    StationSort.VISIT -> StationSortType.VISIT
}

private fun StationSortType.toSort(): StationSort = when (this) {
    StationSortType.DISTANCE -> StationSort.DISTANCE
    StationSortType.VISIT -> StationSort.VISIT
}
