package com.altercharge.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.altercharge.app.R
import com.altercharge.app.ui.card.driverecord.CardDriveRecord
import com.altercharge.app.ui.card.info.CardInfo
import com.altercharge.app.ui.card.info.cardInfoModeFor
import com.altercharge.app.ui.card.station.CardStation
import com.altercharge.app.ui.card.station.CardStationEmpty
import com.altercharge.app.ui.card.station.cardStationHeightFor
import com.altercharge.app.ui.card.station.toCardStationUiState
import com.altercharge.app.ui.card.stationlist.RecommendedStationList
import com.altercharge.app.ui.card.vehiclestatus.CardVehicleStatus
import com.altercharge.app.ui.card.vehiclestatus.ChargingState
import com.altercharge.app.ui.designsystem.component.DriveState
import com.altercharge.app.ui.designsystem.component.StatusBar
import com.altercharge.app.ui.designsystem.component.StatusBarUiState
import com.altercharge.app.ui.designsystem.component.StatusValue
import com.altercharge.app.ui.designsystem.layout.AlterChargeLayout
import com.altercharge.app.ui.designsystem.layout.LocalAppLayout
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.station.StationListMessage
import com.altercharge.app.ui.station.StationListViewModel
import com.altercharge.app.ui.station.rememberVoiceSearch

/**
 * 임시 홈 화면 (#76). 카드마다 실제 값을 넣는다:
 * C 차량 상태(#79) · R1 추천 충전소(#82) · R2 주변 충전소 목록(#84 ~ #88).
 * 위치는 아직 FusedLocation이 Coming Soon이라 MainActivity가 넘긴 고정 좌표를 쓴다.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    stationViewModel: StationListViewModel,
) {
    // 화면 전체를 레이아웃 틀에 맡긴다: 바깥 여백 · 배경은 AlterChargeLayout과 앱 배경 토큰이 정한다 (디버그 카탈로그 layout과 같은 구성)
    AlterChargeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AlterTheme.colors.screenBackground),
        ) {

            val uiState by viewModel.uiState.collectAsState()
            val cards = uiState.cards
            val stationState by stationViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.loadHome()
            }
            // 충전이 시작돼 방문이 세어지면 목록의 방문 횟수 · 방문순도 바로 쓸 수 있게 한다 (REQ_ST_008)
            LaunchedEffect(uiState.myVisitCount) {
                stationViewModel.onVisitsChanged()
            }

            val chipRecommended = stringResource(R.string.card_station_chip_recommended)
            val chipFallback = stringResource(R.string.card_station_chip_fallback)

            // 음성 검색 (REQ_SL_003). 오프라인에서는 검색할 수 없어 마이크를 눌러도 안내만 한다
            val startVoiceSearch = rememberVoiceSearch(
                onResult = stationViewModel::onVoiceResult,
                onListening = stationViewModel::onVoiceListening,
                onError = stationViewModel::onVoiceError,
            )

            AlterChargeLayout(
                statusBar = {
                    StatusBar(
                        StatusBarUiState(
                            // 충전 중이면 충전 중, 아니면 주차 중 (REQ_VS_002 목업 충전 상태)
                            if (uiState.vehicleStatus?.charging == ChargingState.CHARGING) DriveState.CHARGING
                            else DriveState.PARKED,
                            battery = uiState.vehicleStatus?.socPercent?.let { StatusValue.Ready(it) }
                                ?: StatusValue.Unavailable,
                            range = uiState.vehicleStatus?.rangeKm?.let { StatusValue.Ready(it) }
                                ?: StatusValue.Unavailable,
                            // 차량 온도는 아직 SDK 값이 없어 숨긴다 (목업 숫자를 지어내지 않는다)
                            temperatureC = null
                        )
                    )
                },
                l1 = {
                    cards?.let {
                        CardInfo(
                            state = it.cardInfo,
                            mode = cardInfoModeFor(LocalAppLayout.current)
                        )
                    }
                },
                l2 = {
                    cards?.let {
                        CardDriveRecord(
                            state = it.driveRecord,
                            onPreviousWeek = viewModel::showPreviousWeek,
                            onNextWeek = viewModel::showNextWeek,
                            onToggleExpanded = viewModel::toggleDriveRecordExpanded
                        )
                    }
                },
                c = {
                    // 차량 값을 읽는 즉시 그린다 (REQ_VS_001 ~ REQ_VS_006)
                    uiState.vehicleStatus?.let { status ->
                        CardVehicleStatus(state = status)
                    }
                },
                r1 = {
                    val recommended = uiState.recommended
                    if (recommended != null) {
                        CardStation(
                            state = recommended.toCardStationUiState(
                                chipText = if (recommended.fallbackUsed) chipFallback else chipRecommended,
                                myChargingCount = uiState.myVisitCount,
                            )
                        )
                    } else {
                        // 추천을 못 받았을 때 (REQ_CM_006). 첫 요청 중에는 "못 불러왔다"고 미리 말하지 않게 빈 카드만 둔다
                        if (uiState.isLoading && cards == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cardStationHeightFor(LocalAppLayout.current))
                                    .clip(RoundedCornerShape(AlterRadius.XXLarge))
                                    .background(AlterTheme.colors.surface),
                            )
                        } else {
                            CardStationEmpty(
                                onRefresh = viewModel::loadHome,
                                refreshing = uiState.isLoading,
                                // 차량 값을 못 읽어 판정이 없을 때(cards == null)도 서버 탓이 아니니 오프라인 문구를 쓰지 않는다
                                offline = cards?.offline == true,
                            )
                        }
                    }
                },
                r2 = {
                    RecommendedStationList(
                        modifier = Modifier.fillMaxSize(),
                        state = stationState,
                        onQueryChange = stationViewModel::onQueryChange,
                        onMicClick = {
                            if (stationState.offline) {
                                stationViewModel.onVoiceError(StationListMessage.SEARCH_UNAVAILABLE)
                            } else {
                                startVoiceSearch()
                            }
                        },
                        onSortChange = stationViewModel::onSortChange,
                        onFilterChange = stationViewModel::onFilterChange,
                        onStationClick = {},
                    )
                },
                // L2를 펼치면(상황 15) 카드가 길어져 왼쪽 열만 스크롤한다
                leftColumnScrollable = cards?.driveRecord?.expanded == true,
            )
        }
    }
}
