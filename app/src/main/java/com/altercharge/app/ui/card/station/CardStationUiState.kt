package com.altercharge.app.ui.card.station

import androidx.compose.runtime.Immutable
import com.altercharge.app.ui.card.common.ChargerSpeed
import java.time.LocalDate

/** R1이 무엇을 보여주는지. 기본은 추천, R2에서 고르면 선택한 충전소 */
enum class StationCardMode { RECOMMENDED, SELECTED }

/** 충전소를 쓸 수 없는 이유. 있으면 "여기로 안내"를 막고 사유를 보여준다 (상황 13) */
enum class StationUnavailableReason { BROKEN, MAINTENANCE }

/**
 * R1 추천 충전소 카드에 넘기는 값. Figma card_station (366:38369).
 *
 * 초안이다. 담당인 석기님이 구현하면서 필드를 고쳐도 된다.
 */
@Immutable
data class CardStationUiState(
    val mode: StationCardMode,
    val name: String,
    val distanceKm: Double,
    /** 차로 걸리는 시간 (분) */
    val driveMinutes: Int?,
    val speed: ChargerSpeed,
    val powerKw: Int?,
    val availableCount: Int,
    val totalCount: Int,
    val visitCount: Int?,
    val lastVisitDate: LocalDate?,
    /** 원/kWh. TODO 기획 확인: 요금 표시 여부와 데이터 출처 */
    val pricePerKwh: Int?,
    val unavailableReason: StationUnavailableReason?,
)
