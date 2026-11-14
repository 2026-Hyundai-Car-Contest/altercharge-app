package com.altercharge.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.altercharge.app.data.home.HomeCardsLoader
import com.altercharge.app.data.home.LatLng
import com.altercharge.app.data.home.VehicleReading
import com.altercharge.app.data.home.toCardVehicleStatusUiState
import com.altercharge.app.data.local.VisitCountStore
import com.altercharge.app.data.station.StationRepository
import com.altercharge.app.data.upload.ChargingEventRecorder
import com.altercharge.app.data.vehicle.VehicleTelemetry
import com.altercharge.app.data.vehicle.VehicleTelemetrySource
import com.altercharge.app.data.vehicle.isChargeStart
import com.altercharge.app.domain.time.KoreaZone
import com.altercharge.app.ui.card.vehiclestatus.ChargingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock

class HomeViewModelFactory(
    private val loader: HomeCardsLoader,
    private val telemetry: VehicleTelemetrySource,
    private val recorder: ChargingEventRecorder,
    private val visits: VisitCountStore,
    private val stations: StationRepository,
    private val location: LatLng?,
    private val driverName: String,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(loader, telemetry, recorder, visits, stations, location, driverName) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}

/**
 * 홈 화면 카드 3종(C · L1 · R1) 값을 만든다.
 * C 카드는 차량 값만으로 바로 그리고, 서버 값(판정 · 가까운 충전소 · 마지막 충전)이 오면 다시 그린다 (REQ_CM_006).
 * 충전이 시작되면 그 위치로 충전소 방문을 기록한다 (REQ_ST_008).
 */
class HomeViewModel(
    private val loader: HomeCardsLoader,
    private val telemetry: VehicleTelemetrySource,
    private val recorder: ChargingEventRecorder,
    private val visits: VisitCountStore,
    private val stations: StationRepository,
    private val location: LatLng?,
    private val driverName: String,
    private val clock: Clock = Clock.system(KoreaZone),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    /** 직전에 읽은 충전 상태. 미연결 → 충전 중으로 바뀌는 순간을 방문으로 센다 (isChargeStart) */
    private var lastCharging: ChargingState? = null

    fun loadHome() {
        val reading = telemetry.read()
        // 차량 값은 네트워크와 관계없이 그대로 보여 준다 (REQ_CM_006)
        _uiState.update {
            it.copy(vehicleStatus = reading.toCardVehicleStatusUiState(driverName, it.cards?.vehicleStatus))
        }

        val vehicle = reading.readingOrNull()
        if (vehicle == null) {
            // SOC · 주행가능거리를 못 읽으면 판정하지 않는다 (C 카드는 "정보 없음")
            lastCharging = reading.charging
            _uiState.update { it.copy(isLoading = false, cards = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 못 보낸 방문 기록이 있으면 먼저 보낸다 (REQ_CM_006)
                recorder.flush()

                val cards = loader.load(vehicle, location)
                val recommended = cards.recommended
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cards = cards,
                        recommended = recommended,
                        myVisitCount = recommended?.let { info -> visits.count(info.stationId) } ?: 0,
                        vehicleStatus = reading.toCardVehicleStatusUiState(driverName, cards.vehicleStatus),
                    )
                }
                detectChargingStart(reading, vehicle)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                    )
                }
            }
        }
    }

    /**
     * 충전이 시작된 순간이면 충전소 방문을 기록한다 (REQ_ST_008).
     * 충전소는 저장해 둔 3km 목록에서 100m 안으로 찾고, 없으면 목록을 한 번 받아 본다.
     */
    private suspend fun detectChargingStart(telemetry: VehicleTelemetry, vehicle: VehicleReading) {
        val previous = lastCharging
        lastCharging = telemetry.charging
        if (location == null) return
        if (!isChargeStart(previous, telemetry.charging)) return

        val saved = stations.cached()?.stations.orEmpty()
        val known = if (saved.isNotEmpty()) saved else stations.nearby(location.lat, location.lng).stations
        val recorded = recorder.onChargingStarted(vehicle, clock.instant(), location.lat, location.lng, known)
        val stationId = recorded.stationId ?: return
        _uiState.update { it.copy(myVisitCount = visits.count(stationId)) }
    }
}
