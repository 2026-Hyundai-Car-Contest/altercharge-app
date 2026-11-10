package com.altercharge.app.ui.card.station

import androidx.annotation.DrawableRes

enum class ChipTone {
    ACCENT,
    NEUTRAL
}

enum class ChargerStatus {
    AVAILABLE,          // 사용 가능
    UNDER_INSPECTION    // 점검 중
}

sealed class ChargeInfoTag {
    abstract val text: String

    data class Text(
        override val text: String
    ) : ChargeInfoTag()

    data class Icon(
        @DrawableRes val resId: Int,
        override val text: String
    ) : ChargeInfoTag()
}
