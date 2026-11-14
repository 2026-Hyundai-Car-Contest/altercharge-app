package com.altercharge.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.altercharge.app.data.home.HomeCardsLoader
import com.altercharge.app.data.home.LatLng
import com.altercharge.app.data.home.VehicleReading
import com.altercharge.app.data.remote.freshness.RefreshRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModelFactory(
    private val loader: HomeCardsLoader
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(loader) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}

class HomeViewModel(
    private val loader: HomeCardsLoader
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /** L2에서 보고 있는 주(월요일). null이면 이번 주. 다시 불러와도 보던 주를 유지한다 */
    private var viewedWeekStart: LocalDate? = null

    /**
     * 서버에 못 붙으면 저장한 값을 보여 주고 30초마다 다시 요청한다 (REQ_CM_006).
     * 다시 요청할 때도 처음 받은 [vehicle]을 쓴다: 차량 SDK를 실제로 읽게 되면 매번 새로 읽은 값을 넘기도록 바꾼다.
     */
    fun loadHome(
        vehicle: VehicleReading,
        location: LatLng?
    ) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            do {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val retry = try {
                    val cards = loader.load(vehicle, location)
                    val week = viewedWeekStart
                    val driveRecord = if (week == null) {
                        cards.driveRecord.copy(expanded = _uiState.value.cards?.driveRecord?.expanded ?: false)
                    } else {
                        loader.driveRecordFor(week, expanded = _uiState.value.cards?.driveRecord?.expanded ?: false)
                    }
                    _uiState.update { it.copy(isLoading = false, cards = cards.copy(driveRecord = driveRecord)) }
                    cards.offline
                } catch (e: CancellationException) {
                    throw e // 새 loadHome이 이전 요청을 멈춘 것: 오류로 남기지 않는다
                } catch (e: Exception) {
                    // 연결 실패는 loader가 저장값으로 처리하니 여기 오는 것은 코드 · 규격 오류다. 다시 해도 같아서 재요청하지 않는다
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    false
                }
                if (retry) delay(RefreshRule.AUTO_RETRY_INTERVAL.toMillis())
            } while (retry)
        }
    }

    fun showPreviousWeek() = moveWeek(-1)

    fun showNextWeek() = moveWeek(1)

    fun toggleDriveRecordExpanded() {
        val current = _uiState.value.cards?.driveRecord ?: return
        updateDriveRecord(current.weekStart, expanded = !current.expanded)
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
}
