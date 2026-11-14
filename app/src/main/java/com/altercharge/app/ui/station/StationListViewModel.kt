/**
 * R2 주변 충전소 목록 (REQ_SL_001 · REQ_SL_002).
 * 받은 목록을 들고 있다가 검색어가 바뀔 때마다 앱에서 다시 만든다:
 * 서버는 거리순 한 가지로만 주고, 저장한 목록에서도 검색이 동작해야 하기 때문이다 (REQ_CM_006).
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
import kotlinx.coroutines.Job
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

    /** 반경 3km 전체 목록. 화면 값은 언제나 이 목록(또는 검색 결과)에서 만든다 */
    private var allStations: List<StationSummary> = emptyList()

    /** 서버 검색 결과. 검색어를 지우면 null이 되어 전체 목록으로 돌아간다 (REQ_SL_002) */
    private var searchResults: List<StationSummary>? = null

    /** null이면 전체 목록, 값이 있으면 서버 검색 결과 */
    private var searchWord: String? = null

    private var localCounts: Map<String, Int> = emptyMap()
    private var receivedAt: Instant? = null
    private var searchJob: Job? = null

    init {
        refresh()
        retryWhileOffline()
    }

    /** 화면에 다시 들어왔거나 사용자가 다시 시도했을 때 */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            // 저장한 목록이 있으면 먼저 보여 준다: 연결이 느리거나 끊겨도 빈 화면을 보지 않게
            repository.cached()?.let { applyStations(it.stations, it.receivedAt, offline = true, loading = true) }

            val result = repository.nearby(lat, lng, keyword = null)
            if (result.failure == null || result.stations.isNotEmpty()) {
                applyStations(result.stations, result.receivedAt, offline = result.offline, loading = false)
            } else {
                _uiState.update { it.copy(isLoading = false, offline = true) }
            }
            // 검색어가 남아 있으면 검색 결과도 다시 받는다
            searchWord?.let { word -> search(word) }
        }
    }

    /** 검색어. 목록은 서버가 이름 · 주소로 찾는다 (REQ_SL_002). 타이핑 중에는 잠깐 기다린다 */
    fun onQueryChange(text: String) {
        _uiState.update { it.copy(query = text, message = null) }
        searchJob?.cancel()
        val word = text.trim()
        if (word.isEmpty()) {
            searchWord = null
            searchResults = null
            rebuild()
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            search(word)
        }
    }

    /** 음성 인식 결과. 텍스트 검색과 같은 결과를 낸다 (REQ_SL_003) */
    fun onVoiceResult(text: String) {
        _uiState.update { it.copy(query = text, voice = VoiceSearchState.Idle, message = null) }
        searchJob?.cancel()
        val word = text.trim()
        if (word.isEmpty()) {
            searchWord = null
            searchResults = null
            rebuild()
        } else {
            viewModelScope.launch { search(word) }
        }
    }

    fun onVoiceListening() {
        _uiState.update { it.copy(voice = VoiceSearchState.Listening) }
    }

    /** 마이크 권한 거부 · 인식기 없음 · 인식 실패 (REQ_SL_003) */
    fun onVoiceError(message: StationListMessage) {
        _uiState.update { it.copy(voice = VoiceSearchState.Idle, message = message) }
    }

    fun onVoiceFinished() {
        _uiState.update { it.copy(voice = VoiceSearchState.Idle) }
    }

    /** 방문 기록이 새로 생겼을 때(충전 시작) 화면 값을 다시 만든다. 서버를 부르지 않는다 (REQ_ST_008) */
    fun onVisitsChanged() {
        rebuild()
    }

    private suspend fun search(word: String) {
        if (_uiState.value.offline) {
            // 오프라인에서는 검색할 수 없다 (REQ_SL_002 예외)
            _uiState.update { it.copy(message = StationListMessage.SEARCH_UNAVAILABLE) }
            return
        }
        searchWord = word
        _uiState.update { it.copy(isLoading = true) }
        val result = repository.nearby(lat, lng, keyword = word)
        if (result.failure != null && result.stations.isEmpty()) {
            _uiState.update {
                it.copy(isLoading = false, message = StationListMessage.SEARCH_UNAVAILABLE)
            }
            return
        }
        searchResults = result.stations
        _uiState.update { it.copy(isLoading = false, offline = result.offline) }
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

    /** 지금 목록(전체 또는 검색 결과)으로 화면 값을 만든다 */
    private fun rebuild() {
        val built = buildStationList(
            all = allStations,
            searchResults = searchResults,
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

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
