/**
 * R2 주변 충전소 목록 (REQ_SL_001).
 * 받은 목록을 들고 있다가 화면 값을 만든다: 서버는 거리순 한 가지로만 준다 (REQ_CM_006).
 */
package com.altercharge.app.ui.station

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.altercharge.app.data.remote.freshness.RefreshRule
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.data.station.StationRepository
import com.altercharge.app.domain.time.KoreaZone
import com.altercharge.app.ui.card.info.lastCheckedOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant

class StationListViewModelFactory(
    private val repository: StationRepository,
    private val lat: Double,
    private val lng: Double,
    private val clock: Clock = Clock.system(KoreaZone),
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StationListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StationListViewModel(repository, lat, lng, clock) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

class StationListViewModel(
    private val repository: StationRepository,
    private val lat: Double,
    private val lng: Double,
    private val clock: Clock = Clock.system(KoreaZone),
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationListUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    /** 반경 3km 전체 목록. 화면 값은 언제나 이 목록에서 만든다 */
    private var allStations: List<StationSummary> = emptyList()

    private var localCounts: Map<String, Int> = emptyMap()
    private var receivedAt: Instant? = null

    init {
        refresh()
        retryWhileOffline()
    }

    /** 화면에 다시 들어왔거나 사용자가 다시 시도했을 때 */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // 저장한 목록이 있으면 먼저 보여 준다: 연결이 느리거나 끊겨도 빈 화면을 보지 않게
            repository.cached()?.let { applyStations(it.stations, it.receivedAt, offline = true, loading = true) }

            val result = repository.nearby(lat, lng)
            if (result.failure == null || result.stations.isNotEmpty()) {
                applyStations(result.stations, result.receivedAt, offline = result.offline, loading = false)
            } else {
                _uiState.update { it.copy(isLoading = false, offline = true) }
            }
        }
    }

    /** 방문 기록이 새로 생겼을 때(충전 시작) 화면 값을 다시 만든다. 서버를 부르지 않는다 (REQ_ST_008) */
    fun onVisitsChanged() {
        rebuild()
    }

    private fun applyStations(stations: List<StationSummary>, at: Instant?, offline: Boolean, loading: Boolean) {
        allStations = stations
        receivedAt = at
        localCounts = repository.localVisitCounts()
        _uiState.update {
            it.copy(
                isLoading = loading,
                offline = offline,
                lastChecked = if (!offline || at == null) null else lastCheckedOf(at, clock.instant()),
            )
        }
        rebuild()
    }

    /** 지금 목록으로 화면 값을 만든다 */
    private fun rebuild() {
        val built = buildStationList(
            all = allStations,
            receivedAt = receivedAt,
            now = clock.instant(),
        )
        _uiState.update { it.copy(items = built.items) }
    }

    /** 연결이 끊긴 동안 30초마다 다시 요청한다 (REQ_CM_006) */
    private fun retryWhileOffline() {
        viewModelScope.launch {
            while (isActive) {
                delay(RefreshRule.AUTO_RETRY_INTERVAL.toMillis())
                if (_uiState.value.offline) refresh()
            }
        }
    }
}
