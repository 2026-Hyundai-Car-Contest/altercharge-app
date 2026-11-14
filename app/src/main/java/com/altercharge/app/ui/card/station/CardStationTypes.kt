package com.altercharge.app.ui.card.station

import androidx.annotation.DrawableRes

enum class ChipTone {
    ACCENT,
    NEUTRAL
}

/** Figma card_station availability: available · fault(점검 중) · unknown(정보 없음) */
enum class ChargerStatus {
    AVAILABLE,          // 사용 가능
    UNDER_INSPECTION,   // 점검 중: 길 안내 버튼을 막는다 (REQ_EX_004)
    UNKNOWN,            // 가용 대수 유효 기간(15분)이 지나 확인할 수 없음 (REQ_CM_009)
}

sealed class ChargeInfoTag {
    abstract val text: String
    abstract val type: ChargeInfoType

    data class Text(
        override val text: String,
        override val type: ChargeInfoType
    ) : ChargeInfoTag()

    data class Icon(
        @DrawableRes val resId: Int,
        override val text: String,
        override val type: ChargeInfoType
    ) : ChargeInfoTag()
}

enum class ChargeInfoType {
    CHARGING_TYPE, // 급속, 완속
    POWER          // 100kW, 200kW
}

enum class ChargingType {
    AC,       // 완속
    DC,       // 급속
    ULTRA_FAST // 초급속
}
