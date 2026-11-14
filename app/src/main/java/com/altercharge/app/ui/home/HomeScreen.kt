package com.altercharge.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.card.driverecord.CardDriveRecord
import com.altercharge.app.ui.card.info.CardInfo
import com.altercharge.app.ui.card.info.cardInfoModeFor
import com.altercharge.app.ui.card.station.CardStation
import com.altercharge.app.ui.card.station.CardStationUiState
import com.altercharge.app.ui.card.station.ChargeInfoTag
import com.altercharge.app.ui.card.station.ChargeInfoType
import com.altercharge.app.ui.card.station.ChargerStatus
import com.altercharge.app.ui.card.station.ChipState
import com.altercharge.app.ui.card.station.ChipTone
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

/**
 * 임시 홈 화면 (#76). 카드마다 실제 값을 넣는다:
 * C 차량 상태(#79). R1 추천 충전소 · R2 주변 충전소 목록은 아직 목업이다.
 * 위치는 아직 FusedLocation이 Coming Soon이라 MainActivity가 넘긴 고정 좌표를 쓴다.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val uiState by viewModel.uiState.collectAsState()
        val cards = uiState.cards

        LaunchedEffect(Unit) {
            viewModel.loadHome()
        }
        // TODO: R1-충전소 카드 실제 데이터로 변경

        val dummyStation = CardStationUiState(
            chipState = ChipState(
                text = "추천",
                chipTone = ChipTone.ACCENT
            ),
            availableChargerCount = 3,
            occupiedChargerCount = 5,
            chargerStatus = ChargerStatus.AVAILABLE,
            stationName = "연희동 주차장",
            chargerInfo = listOf(
                ChargeInfoTag.Text(text = "100Km", type = ChargeInfoType.POWER)
            ),
            distance = 1.2,
            myChargingCount = 12
        )

        AlterChargeTheme() {
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
                            onPreviousWeek = {},
                            onNextWeek = {},
                            onToggleExpanded = {}
                        )
                    }
                },
                c = {
                    // 차량 값을 읽는 즉시 그린다 (REQ_VS_001 ~ REQ_VS_006)
                    uiState.vehicleStatus?.let { status ->
                        CardVehicleStatus(state = status)
                    }
                },
                r1 = { CardStation(state = dummyStation) },
                r2 = {
                    RecommendedStationList(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        }
    }
}
