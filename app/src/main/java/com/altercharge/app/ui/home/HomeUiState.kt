package com.altercharge.app.ui.home

import com.altercharge.app.data.home.HomeCards

data class HomeUiState(
    val isLoading: Boolean = false,
    val cards: HomeCards? = null,
    val error: String? = null
)
