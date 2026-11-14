/** 추천 충전소 정보 → R1 카드 (REQ_ST_007) */
package com.altercharge.app.ui.card.station

import com.altercharge.app.R
import com.altercharge.app.data.home.RecommendedStationInfo
import com.altercharge.app.data.remote.model.SpeedCategory
import com.altercharge.app.data.station.displayKm
import com.altercharge.app.data.station.speedCategoryOf
import kotlin.math.roundToInt

/**
 * @param chipText "추천" / "대체 추천". 안내 직전 상태 확인(04)이 필요한 대체 후보는 색을 달리한다
 * @param myChargingCount 내 방문 횟수 (REQ_ST_008)
 */
fun RecommendedStationInfo.toCardStationUiState(
    chipText: String,
    myChargingCount: Int,
): CardStationUiState = CardStationUiState(
    chipState = ChipState(
        text = chipText,
        chipTone = if (fallbackUsed) ChipTone.NEUTRAL else ChipTone.ACCENT,
    ),
    availableChargerCount = availableCount,
    // 상세를 못 받으면 가용 수로 자리만 채운다
    totalChargerCount = totalValidCount ?: availableCount,
    // 가용 0곳은 점검 중이라고 단정할 수 없어 상태 문구 대신 0/전체를 보여 준다
    chargerStatus = ChargerStatus.AVAILABLE,
    stationName = name,
    chargerInfo = chargerInfoTags(),
    distance = displayKm(distanceMeters),
    myChargingCount = myChargingCount,
    stationAddress = address,
    connectorTypes = allConnectorTypes,
)

/** 충전 속도(완속 · 급속 · 초급속)와 최대 출력. 출력을 모르면 속도 태그도 붙이지 않는다 */
private fun RecommendedStationInfo.chargerInfoTags(): List<ChargeInfoTag> {
    val tags = mutableListOf<ChargeInfoTag>()
    when (speedCategory ?: speedCategoryOf(maxOutputKw)) {
        SpeedCategory.SLOW -> tags += ChargeInfoTag.Icon(R.drawable.ic_charger_slow, "완속", ChargeInfoType.CHARGING_TYPE)
        SpeedCategory.FAST -> tags += ChargeInfoTag.Icon(R.drawable.ic_charger_fast, "급속", ChargeInfoType.CHARGING_TYPE)
        SpeedCategory.ULTRA_FAST -> tags += ChargeInfoTag.Icon(R.drawable.ic_charger_ultra, "초급속", ChargeInfoType.CHARGING_TYPE)
        null -> Unit
    }
    maxOutputKw?.let { tags += ChargeInfoTag.Text("${it.roundToInt()}kW", ChargeInfoType.POWER) }
    return tags
}
