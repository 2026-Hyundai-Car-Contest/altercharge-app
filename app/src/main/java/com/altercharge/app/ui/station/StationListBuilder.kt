/**
 * 받아 둔 목록 → R2 화면 값 (REQ_SL_001 · REQ_CM_006).
 * ViewModel과 테스트가 같이 쓴다: 안드로이드 없이 확인할 수 있게 순수 함수로 둔다.
 */
package com.altercharge.app.ui.station

import com.altercharge.app.data.remote.freshness.stationValidity
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.ui.card.stationitem.toCardItemUiState
import java.time.Instant

/**
 * @param receivedAt 목록을 받은 시각. 15분이 지나면 가용 대수를 "정보 없음"으로 보여 준다 (REQ_CM_009)
 */
fun buildStationList(
    all: List<StationSummary>,
    receivedAt: Instant?,
    now: Instant,
): StationListUiState {
    val availabilityUnknown = stationValidity(receivedAt, now).showAvailabilityUnknown
    val items = all.map { station ->
        station.toCardItemUiState(availabilityUnknown = availabilityUnknown)
    }
    return StationListUiState(items = items)
}
