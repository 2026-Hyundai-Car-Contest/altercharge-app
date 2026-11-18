package com.altercharge.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.altercharge.app.data.home.HomeCardsLoader
import com.altercharge.app.data.home.LatLng
import com.altercharge.app.data.home.RecommendedStationInfo
import com.altercharge.app.data.home.VehicleReading
import com.altercharge.app.data.home.toCardVehicleStatusUiState
import com.altercharge.app.data.home.toRouteDestination
import com.altercharge.app.data.home.toSelectedStationInfo
import com.altercharge.app.data.local.VisitCountStore
import com.altercharge.app.data.navi.StationNavigator
import com.altercharge.app.data.remote.freshness.RefreshRule
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.data.station.StationRepository
import com.altercharge.app.data.upload.ChargingEventRecorder
import com.altercharge.app.data.vehicle.VehicleTelemetry
import com.altercharge.app.data.vehicle.VehicleTelemetrySource
import com.altercharge.app.data.vehicle.isChargeStart
import com.altercharge.app.domain.time.KoreaZone
import com.altercharge.app.ui.card.driverecord.CardDriveRecordUiState
import com.altercharge.app.ui.card.vehiclestatus.ChargingState
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModelFactory(
    private val loader: HomeCardsLoader,
    private val telemetry: VehicleTelemetrySource,
    private val recorder: ChargingEventRecorder,
    private val visits: VisitCountStore,
    private val stations: StationRepository,
    private val location: LatLng?,
    private val driverName: String,
    private val navigator: StationNavigator,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(loader, telemetry, recorder, visits, stations, location, driverName, navigator) as T
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
    private val navigator: StationNavigator,
    private val clock: Clock = Clock.system(KoreaZone),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    /** 직전에 읽은 충전 상태. 미연결 → 충전 중으로 바뀌는 순간을 방문으로 센다 (isChargeStart) */
    private var lastCharging: ChargingState? = null

    private var loadJob: Job? = null

    /** L2에서 보고 있는 주(월요일). null이면 이번 주. 다시 불러와도 보던 주를 유지한다 */
    private var viewedWeekStart: LocalDate? = null

    /** 서버에 못 붙으면 저장한 값을 보여 주고 30초마다 다시 요청한다 (REQ_CM_006). 다시 요청할 때마다 차량 값도 새로 읽는다 */
    fun loadHome() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // 오프라인이면 30초, 연결돼 있으면 5분마다 다시 받는다 (REQ_CM_006 · REQ_CM_009)
            while (true) {
                val offline = loadOnce()
                val interval = if (offline) RefreshRule.AUTO_RETRY_INTERVAL else RefreshRule.ONLINE_REFRESH_INTERVAL
                delay(interval.toMillis())
            }
        }
    }

    /**
     * 차량 값만 다시 읽어 C 카드를 고친다. 상단 바 시각이 바뀔 때마다 불러서
     * "충전 완료 예정" 시각이 화면 시각 + 남은 시간과 어긋나지 않게 한다 (서버 요청은 하지 않는다)
     */
    fun refreshVehicleStatus() {
        showVehicleStatus(telemetry.read())
    }

    /** 차량 값은 네트워크와 관계없이 그대로 보여 준다 (REQ_CM_006). 서버 값은 마지막으로 받은 것을 쓴다 */
    private fun showVehicleStatus(reading: VehicleTelemetry) {
        _uiState.update {
            it.copy(vehicleStatus = reading.toCardVehicleStatusUiState(driverName, it.cards?.vehicleStatus))
        }
    }

    /** 내가 이 충전소에 충전하러 간 횟수 (REQ_ST_008). 충전소가 없으면 0 */
    private fun visitCountOf(station: RecommendedStationInfo?): Int = station?.let { visits.count(it.stationId) } ?: 0

    /** @return 서버에 못 붙어 다시 요청해야 하면 true */
    private suspend fun loadOnce(): Boolean {
        val reading = telemetry.read()
        showVehicleStatus(reading)

        val vehicle = reading.readingOrNull()
        if (vehicle == null) {
            // SOC · 주행가능거리를 못 읽으면 판정하지 않는다 (C 카드는 "정보 없음")
            lastCharging = reading.charging
            _uiState.update { it.copy(isLoading = false, cards = null) }
            return false
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        return try {
            // 못 보낸 방문 기록이 있으면 먼저 보낸다 (REQ_CM_006)
            recorder.flush()

            val cards = loader.load(vehicle, location)
            val recommended = cards.recommended
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cards = cards.copy(driveRecord = keepViewedWeek(cards.driveRecord)),
                    recommended = recommended,
                    // 목록에서 고른 충전소가 R1에 있으면 그 충전소의 방문 횟수를 유지한다 (REQ_SL_006)
                    myVisitCount = visitCountOf(it.selectedStation ?: recommended),
                    vehicleStatus = reading.toCardVehicleStatusUiState(driverName, cards.vehicleStatus),
                )
            }
            if (!telemetry.isSimulated) detectChargingStart(reading, vehicle)
            cards.offline
        } catch (e: CancellationException) {
            throw e // 새 loadHome이 이전 요청을 멈춘 것: 오류로 남기지 않는다
        } catch (e: Exception) {
            // 연결 실패는 loader가 저장값으로 처리하니 여기 오는 것은 코드 · 규격 오류다. 다시 해도 같아서 재요청하지 않는다
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message,
                )
            }
            false
        }
    }

    /**
     * R1 "여기로 안내" (REQ_BR_004 · REQ_BR_005). 서버에 충전기 상태를 다시 묻고, 갈 수 있으면 내비 목적지를 바꾼다.
     * 요청 중에는 다시 누르지 못하게 한다.
     */
    fun navigateToRecommended() {
        val station = _uiState.value.r1Station ?: return
        if (_uiState.value.navigating) return
        viewModelScope.launch {
            _uiState.update { it.copy(navigating = true) }
            val outcome = navigator.navigate(station.toRouteDestination())
            _uiState.update { it.copy(navigating = false, navigationResult = outcome) }
        }
    }

    /**
     * 목록에서 충전소를 고르면 R1에 추천 대신 보여 준다 (REQ_SL_006).
     * 같은 충전소를 다시 고르면 원래 추천으로 돌아간다.
     */
    fun onStationSelected(station: StationSummary) {
        val current = _uiState.value.selectedStation
        if (current?.stationId == station.stationId) {
            clearSelectedStation()
            return
        }
        _uiState.update {
            it.copy(selectedStation = station.toSelectedStationInfo(), myVisitCount = visits.count(station.stationId))
        }
    }

    /** 목록이 새로 오면 고른 충전소 값(가용 대수 · 관측 시각)을 새 값으로 바꾼다. 선택은 그대로 둔다 */
    fun refreshSelectedStation(station: StationSummary) {
        if (_uiState.value.selectedStation?.stationId != station.stationId) return
        _uiState.update { it.copy(selectedStation = station.toSelectedStationInfo()) }
    }

    /** 정렬을 바꾸면 원래 추천으로 돌아간다 (REQ_SL_006) */
    fun clearSelectedStation() {
        if (_uiState.value.selectedStation == null) return
        _uiState.update { state ->
            state.copy(selectedStation = null, myVisitCount = visitCountOf(state.recommended))
        }
    }

    fun onNavigationResultShown() {
        _uiState.update { it.copy(navigationResult = null) }
    }

    override fun onCleared() {
        navigator.release()
        super.onCleared()
    }

    fun showPreviousWeek() = moveWeek(-1)

    fun showNextWeek() = moveWeek(1)

    fun toggleDriveRecordExpanded() {
        val current = _uiState.value.cards?.driveRecord ?: return
        updateDriveRecord(current.weekStart, expanded = !current.expanded)
    }

    /** 다시 불러온 값은 이번 주 · 접힘이라, 보던 주와 펼침 상태로 바꿔 끼운다 */
    private fun keepViewedWeek(loaded: CardDriveRecordUiState): CardDriveRecordUiState {
        val expanded = _uiState.value.cards?.driveRecord?.expanded ?: false
        val week = viewedWeekStart ?: return loaded.copy(expanded = expanded)
        return loader.driveRecordFor(week, expanded = expanded)
    }

    private fun moveWeek(weeks: Long) {
        val current = _uiState.value.cards?.driveRecord ?: return
        val canMove = if (weeks < 0) current.hasPreviousWeek else current.hasNextWeek
        if (!canMove) return
        updateDriveRecord(current.weekStart.plusWeeks(weeks), expanded = current.expanded)
    }

    private fun updateDriveRecord(weekStart: LocalDate, expanded: Boolean) {
        val record = loader.driveRecordFor(weekStart, expanded = expanded)
        viewedWeekStart = if (record.todayIndex != null) null else weekStart
        _uiState.update { state -> state.copy(cards = state.cards?.copy(driveRecord = record)) }
    }

    /**
     * 충전이 시작된 순간이면 충전소 방문을 기록한다 (REQ_ST_008).
     * 충전소는 저장해 둔 3km 목록에서 100m 안으로 찾고, 없으면 목록을 한 번 받아 본다.
     */
    private suspend fun detectChargingStart(reading: VehicleTelemetry, vehicle: VehicleReading) {
        val previous = lastCharging
        lastCharging = reading.charging
        if (location == null) return
        if (!isChargeStart(previous, reading.charging)) return

        val saved = stations.cached()?.stations.orEmpty()
        val known = if (saved.isNotEmpty()) saved else stations.nearby(location.lat, location.lng).stations
        val recorded = recorder.onChargingStarted(vehicle, clock.instant(), location.lat, location.lng, known)
        val stationId = recorded.stationId ?: return
        _uiState.update { it.copy(myVisitCount = visits.count(stationId)) }
    }
}
