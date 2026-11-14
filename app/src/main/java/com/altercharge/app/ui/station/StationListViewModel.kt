/**
 * R2 주변 충전소 목록 (REQ_SL_001 ~ REQ_SL_005).
 * 받은 목록을 들고 있다가 검색어 · 정렬 · 필터가 바뀔 때마다 앱에서 다시 만든다:
 * 서버는 거리순 한 가지로만 주고, 정렬 · 필터는 저장한 목록에서도 동작해야 하기 때문이다 (REQ_CM_006).
 */
package com.altercharge.app.ui.station

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.altercharge.app.data.remote.freshness.RefreshRule
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.data.station.StationFilter
import com.altercharge.app.data.station.StationRepository
import com.altercharge.app.data.station.StationSort
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

    /** 반경 3km 전체 목록. 정렬 · 필터는 언제나 이 목록(또는 검색 결과)에서 한다 */
    private var allStations: List<StationSummary> = emptyList()

    /** 서버 검색 결과. 검색어를 지우면 null이 되어 전체 목록으로 돌아간다 (REQ_SL_002) */
    private var searchResults: List<StationSummary>? = null

    /** null이면 전체 목록, 값이 있으면 서버 검색 결과 */
    private var searchWord: String? = null

    private var localCounts: Map<String, Int> = emptyMap()
    private var receivedAt: Instant? = null
    private var searchJob: Job? = null

    /** 재시도 · 화면 복귀가 겹쳐도 목록 요청은 하나만 둔다: 늦게 끝난 옛 요청이 새 값을 덮지 않게 */
    private var refreshJob: Job? = null

    init {
        refresh()
        retryWhileOffline()
    }

    /** 목록에서 고른 충전소의 원본 값 (REQ_SL_006: R1에 대신 보여 줄 때 쓴다) */
    fun summaryOf(stationId: String): StationSummary? =
        (searchResults.orEmpty() + allStations).firstOrNull { it.stationId == stationId }

    /** 화면에 다시 들어왔거나 사용자가 다시 시도했을 때 */
    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            // 처음에만 저장한 목록을 먼저 보여 준다: 연결이 느리거나 끊겨도 빈 화면을 보지 않게.
            // 이미 목록이 있으면 다시 요청할 때마다 '오프라인'으로 깜빡이지 않게 그대로 둔다
            if (allStations.isEmpty()) {
                repository.cached()?.let { applyStations(it.stations, it.receivedAt, offline = true, loading = true) }
            }

            val result = repository.nearby(lat, lng, keyword = null)
            if (result.failure == null || result.stations.isNotEmpty()) {
                applyStations(result.stations, result.receivedAt, offline = result.offline, loading = false)
            } else {
                _uiState.update { it.copy(isLoading = false, offline = true) }
            }
            // 검색어가 남아 있으면 검색 결과도 다시 받는다
            searchWord?.let { word -> startSearch(word) }
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
            // 검색 요청 중에 지웠으면 그 요청은 취소됐으니 로딩 표시도 내린다
            _uiState.update { it.copy(isLoading = false) }
            rebuild()
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            search(word)
        }
    }

    /** 검색 요청도 하나만 둔다: 음성 · 타이핑 · 재요청이 겹치면 마지막 검색어 결과만 남긴다 */
    private fun startSearch(word: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { search(word) }
    }

    /** 음성 인식 결과. 텍스트 검색과 같은 결과를 낸다 (REQ_SL_003) */
    fun onVoiceResult(text: String) {
        _uiState.update { it.copy(query = text, voice = VoiceSearchState.Idle, message = null) }
        searchJob?.cancel()
        val word = text.trim()
        if (word.isEmpty()) {
            searchWord = null
            searchResults = null
            _uiState.update { it.copy(isLoading = false) }
            rebuild()
        } else {
            startSearch(word)
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

    /** 방문 기록이 새로 생겼을 때(충전 시작) 횟수만 다시 읽는다. 서버를 부르지 않는다 (REQ_ST_008) */
    fun onVisitsChanged() {
        rebuild()
    }

    /** 거리순 · 방문순 (REQ_SL_004). 방문 기록이 없으면 방문순으로 바꾸지 않는다 */
    fun onSortChange(sort: StationSort) {
        if (sort == StationSort.VISIT && !_uiState.value.visitSortEnabled) return
        _uiState.update { it.copy(sort = sort, message = null) }
        rebuild()
    }

    /** 연결 타입 · 출력 필터 (REQ_SL_005) */
    fun onFilterChange(filter: StationFilter) {
        _uiState.update { it.copy(filter = filter, message = null) }
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
            // 이전 검색어 결과가 새 검색어 아래 남지 않게 비운다
            searchResults = emptyList()
            _uiState.update {
                it.copy(isLoading = false, message = StationListMessage.SEARCH_UNAVAILABLE)
            }
            rebuild()
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

    /** 지금 목록(전체 또는 검색 결과)에 필터 · 정렬을 적용해 화면 값을 만든다 */
    private fun rebuild() {
        // 방문 횟수는 화면을 그릴 때마다 기기 저장값에서 읽는다 (충전 시작으로 바로 늘어난 값을 반영)
        localCounts = repository.localVisitCounts()
        val built = buildStationList(
            all = allStations,
            searchResults = searchResults,
            sort = _uiState.value.sort,
            filter = _uiState.value.filter,
            localCounts = localCounts,
            receivedAt = receivedAt,
            now = clock.instant(),
        )
        _uiState.update {
            it.copy(
                items = built.items,
                sort = built.sort,
                visitSortEnabled = built.visitSortEnabled,
                message = when {
                    built.message != null -> built.message
                    it.message == StationListMessage.FILTER_EMPTY -> null
                    else -> it.message
                },
            )
        }
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
