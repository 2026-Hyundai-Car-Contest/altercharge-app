package com.altercharge.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.altercharge.app.data.home.HomeCardsLoader
import com.altercharge.app.data.home.LatLng
import com.altercharge.app.data.home.VehicleReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    fun loadHome(
        vehicle: VehicleReading,
        location: LatLng?
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            try {
                val cards = loader.load(vehicle, location)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cards = cards
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}
