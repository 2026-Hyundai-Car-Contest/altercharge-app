/** 추천 충전소 정보 → R1 카드 (REQ_ST_007) */
package com.altercharge.app.ui.card.station

import com.altercharge.app.data.remote.freshness.validityOf
import com.altercharge.app.data.remote.freshness.Validity
import com.altercharge.app.data.remote.freshness.AVAILABILITY_VALIDITY
import java.time.Instant
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
    now: Instant = Instant.now(),
): CardStationUiState = CardStationUiState(
    chipState = ChipState(
        text = chipText,
        chipTone = if (fallbackUsed) ChipTone.NEUTRAL else ChipTone.ACCENT,
    ),
    availableChargerCount = availableCount,
    // 상세를 못 받으면 가용 수로 자리만 채운다
    totalChargerCount = totalValidCount ?: availableCount,
    chargerStatus = availabilityStatus(now),
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

/**
 * Figma availability. 상세의 충전기가 모두 점검 · 정지면 점검 중(길 안내 막음), 관측 15분이 지나면 정보 없음.
 * 가용 0곳만으로는 점검 중이라고 단정할 수 없어 available(0 / 전체)로 둔다.
 */
private fun RecommendedStationInfo.availabilityStatus(now: Instant): ChargerStatus = when {
    chargers.isNotEmpty() && chargers.all { it.status in INSPECTION_STATUSES } -> ChargerStatus.UNDER_INSPECTION
    validityOf(statusObservedAt, now, AVAILABILITY_VALIDITY) == Validity.EXPIRED -> ChargerStatus.UNKNOWN
    else -> ChargerStatus.AVAILABLE
}

private val INSPECTION_STATUSES = setOf(
    com.altercharge.app.data.remote.model.ChargerStatus.MAINTENANCE,
    com.altercharge.app.data.remote.model.ChargerStatus.STOPPED,
)
