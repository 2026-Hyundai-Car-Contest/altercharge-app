package com.altercharge.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.altercharge.app.data.home.HomeCardsLoader
import com.altercharge.app.data.home.LatLng
import com.altercharge.app.data.home.toCardVehicleStatusUiState
import com.altercharge.app.data.vehicle.VehicleTelemetrySource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModelFactory(
    private val loader: HomeCardsLoader,
    private val telemetry: VehicleTelemetrySource,
    private val location: LatLng?,
    private val driverName: String,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(loader, telemetry, location, driverName) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}

/**
 * 홈 화면 카드 3종(C · L1 · R1) 값을 만든다.
 * C 카드는 차량 값만으로 바로 그리고, 서버 값(판정 · 가까운 충전소 · 마지막 충전)이 오면 다시 그린다 (REQ_CM_006).
 */
class HomeViewModel(
    private val loader: HomeCardsLoader,
    private val telemetry: VehicleTelemetrySource,
    private val location: LatLng?,
    private val driverName: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun loadHome() {
        val reading = telemetry.read()
        // 차량 값은 네트워크와 관계없이 그대로 보여 준다 (REQ_CM_006)
        _uiState.update {
            it.copy(vehicleStatus = reading.toCardVehicleStatusUiState(driverName, it.cards?.vehicleStatus))
        }

        val vehicle = reading.readingOrNull()
        if (vehicle == null) {
            // SOC · 주행가능거리를 못 읽으면 판정하지 않는다 (C 카드는 "정보 없음")
            _uiState.update { it.copy(isLoading = false, cards = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val cards = loader.load(vehicle, location)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cards = cards,
                        vehicleStatus = reading.toCardVehicleStatusUiState(driverName, cards.vehicleStatus),
                    )
                }
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
}
