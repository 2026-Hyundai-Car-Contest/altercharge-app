package com.altercharge.app.ui.card.station

data class CardStationUiState(
    var chipTone: ChipTone
) {
    enum class ChipTone {
        ACCENT,
        NEUTRAL
    }
}
