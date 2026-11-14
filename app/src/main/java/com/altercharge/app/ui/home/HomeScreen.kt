package com.altercharge.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.altercharge.app.data.home.LatLng
import com.altercharge.app.data.home.VehicleReading
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
import com.altercharge.app.ui.card.vehiclestatus.CardVehicleStatusUiState
import com.altercharge.app.ui.card.vehiclestatus.ChargingState
import com.altercharge.app.ui.designsystem.component.DriveState
import com.altercharge.app.ui.designsystem.component.StatusBar
import com.altercharge.app.ui.designsystem.component.StatusBarUiState
import com.altercharge.app.ui.designsystem.component.StatusValue
import com.altercharge.app.ui.designsystem.layout.AlterChargeLayout
import com.altercharge.app.ui.designsystem.layout.LocalAppLayout
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
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

            LaunchedEffect (Unit) {
                viewModel.loadHome(
                    vehicle = VehicleReading(
                        socPercent = 80,
                        rangeKm = 350,
                        odometerKm = 12500.0
                    ),
                    location = LatLng(37.5665, 126.9780)
                )
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

            val dummyVehicleStatus = CardVehicleStatusUiState(
                driverName = "기권",
                charging = ChargingState.CHARGING,
                socPercent = 32,
                rangeKm = 98,
                remainingMinutes = 45,
                fullAt = "오후 2:30",
                lastChargedAt = null,
                nearestStationKm = 1.2,
                scheduledAt = null,
                pauseReason = null,
                autoResumeAt = null,
                fault = null,
                lastUpdatedAt = "오후 1:45",
                retryInSeconds = null,
                chargeRecommended = false
            )

            AlterChargeLayout(
                statusBar = {
                    StatusBar(
                        StatusBarUiState(
                            DriveState.CHARGING,
                            battery = StatusValue.Ready(32),
                            range = StatusValue.Ready(98),
                            temperatureC = 12
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
                    CardVehicleStatus(
                        state = dummyVehicleStatus
                    )
                },
                r1 = { CardStation(state = dummyStation) },
                r2 = {
                    RecommendedStationList(
                        modifier = Modifier.fillMaxSize()
                    )
                },
                // L2를 펼치면(상황 15) 카드가 길어져 왼쪽 열만 스크롤한다
                leftColumnScrollable = cards?.driveRecord?.expanded == true,
            )
        }
    }
}
