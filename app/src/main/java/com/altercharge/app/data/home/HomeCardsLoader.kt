/**
 * 홈 화면 카드 값을 한 번에 만든다. 서버 호출 · 판정 갱신(REQ_JD_004) · 오프라인 저장값(REQ_CM_006)을 안에서 처리한다.
 *
 *   val cards = loader.load(VehicleReading(soc, rangeKm, odometerKm), LatLng(lat, lng))
 *   CardInfo(state = cards.cardInfo)
 *   CardDriveRecord(state = cards.driveRecord, onPreviousWeek = { driveRecord = loader.driveRecordFor(week.minusWeeks(1)) }, …)
 *   CardVehicleStatusUiState(…차량 SDK 값…, chargeRecommended = cards.vehicleStatus.chargeRecommended,
 *       nearestStationKm = cards.vehicleStatus.nearestStationKm, lastChargedAt = cards.vehicleStatus.lastChargedAt)
 */
package com.altercharge.app.data.home

import com.altercharge.app.data.local.OfflineSnapshotStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.ApiResult
import com.altercharge.app.data.remote.DEMO_VEHICLE_ID
import com.altercharge.app.data.remote.freshness.Validity
import com.altercharge.app.data.remote.freshness.stationValidity
import com.altercharge.app.data.remote.mapper.lastChargedAtText
import com.altercharge.app.data.remote.mapper.nearestStationKm
import com.altercharge.app.data.remote.mapper.placeName
import com.altercharge.app.data.remote.mapper.toCardDriveRecordUiState
import com.altercharge.app.data.remote.mapper.toJudgmentInput
import com.altercharge.app.data.remote.mapper.todayKm
import com.altercharge.app.data.remote.mapper.weekStartOf
import com.altercharge.app.data.remote.model.LatestChargeHistory
import com.altercharge.app.data.remote.model.VehicleSnapshotHistory
import com.altercharge.app.domain.judgment.JudgmentInput
import com.altercharge.app.domain.judgment.JudgmentResult
import com.altercharge.app.domain.judgment.judge
import com.altercharge.app.domain.judgment.recomputeReason
import com.altercharge.app.domain.judgment.stamp
import com.altercharge.app.domain.time.KoreaZone
import com.altercharge.app.ui.card.driverecord.CardDriveRecordUiState
import com.altercharge.app.ui.card.info.CardInfoUiState
import com.altercharge.app.ui.card.info.LastChecked
import com.altercharge.app.ui.card.info.lastCheckedOf
import com.altercharge.app.ui.card.info.toCardInfoUiState
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

data class HomeCards(
    val cardInfo: CardInfoUiState,
    val driveRecord: CardDriveRecordUiState,
    val vehicleStatus: VehicleStatusServerValues,
    /** 이번 서버 요청 중 실패가 있었는지 (위치가 없어 안 부른 것은 제외) */
    val offline: Boolean,
)

class HomeCardsLoader(
    private val api: AlterChargeApi,
    private val store: OfflineSnapshotStore,
    private val clock: Clock = Clock.system(KoreaZone),
    private val vehicleId: String = DEMO_VEHICLE_ID,
) {
    // 주행 기록은 서버 보관이 원칙이라(REQ_JD_002) 기기에 저장하지 않고 앱이 켜져 있는 동안만 기억한다.
    // load(IO)와 driveRecordFor(화면 스레드)가 같이 읽어서 @Volatile.
    @Volatile private var lastHistory: VehicleSnapshotHistory? = null
    @Volatile private var lastCharge: LatestChargeHistory? = null

    /** 판정을 저장하므로 겹쳐 부르지 않는다. 위치를 모르면 null (충전소는 저장값) */
    suspend fun load(vehicle: VehicleReading, location: LatLng?): HomeCards {
        val now = clock.instant()
        val today = now.atZone(KoreaZone).toLocalDate() // LocalDate.ofInstant는 API 34부터라 못 쓴다

        val historyResult = api.snapshots(vehicleId)
        val chargeResult = api.latestCharge(vehicleId)
        (historyResult as? ApiResult.Success)?.let { lastHistory = it.value }
        (chargeResult as? ApiResult.Success)?.let { lastCharge = it.value }
        val history = lastHistory
        val charge = lastCharge

        val judgment = judgmentFor(vehicle, history, charge, now, today)
        val station = stationFor(location, judgment.chargeRecommended, now)

        val cardInfo = judgment.toCardInfoUiState(
            placeName = station.placeName,
            placeLastChecked = station.lastChecked,
        )
        val driveRecord = driveRecordFor(weekStartOf(today), today)

        return HomeCards(
            cardInfo = cardInfo,
            driveRecord = driveRecord,
            vehicleStatus = VehicleStatusServerValues(
                chargeRecommended = judgment.chargeRecommended,
                nearestStationKm = station.nearestStationKm,
                lastChargedAt = charge?.lastChargedAtText(today),
            ),
            offline = historyResult is ApiResult.Failure || chargeResult is ApiResult.Failure || station.offline,
        )
    }

    /** L2 이전 · 다음 주. 받아 둔 기록으로 만들어 서버를 다시 부르지 않는다 */
    fun driveRecordFor(
        weekStart: LocalDate,
        today: LocalDate = clock.instant().atZone(KoreaZone).toLocalDate(),
        expanded: Boolean = false,
    ): CardDriveRecordUiState = (lastHistory ?: emptyHistory())
        .toCardDriveRecordUiState(weekStart = weekStart, today = today, latestCharge = lastCharge, expanded = expanded)

    private fun emptyHistory() = VehicleSnapshotHistory(vehicleId, records = emptyList(), retentionDays = 0, completeDayCount = 0)

    /** 기록을 못 받았을 때의 판정은 저장하지 않는다: 연결되면 바로 다시 계산하게 */
    private fun judgmentFor(
        vehicle: VehicleReading,
        history: VehicleSnapshotHistory?,
        charge: LatestChargeHistory?,
        now: Instant,
        today: LocalDate,
    ): JudgmentResult {
        val saved = store.load()
        if (history == null) {
            return saved.judgment ?: judge(JudgmentInput(vehicle.rangeKm, vehicle.socPercent, emptyList()))
        }
        val todayKm = history.todayKm(vehicle.odometerKm, today)
        val reason = recomputeReason(saved.judgmentStamp, today, todayKm, charge?.lastChargeCompletedAt)
        val keep = saved.judgment
        if (reason == null && keep != null) return keep

        val result = judge(history.toJudgmentInput(vehicle.rangeKm, vehicle.socPercent, today))
        store.saveJudgment(result, result.stamp(now, todayKm))
        return result
    }

    private data class StationValues(
        val placeName: String,
        val nearestStationKm: Double?,
        val lastChecked: LastChecked?,
        val offline: Boolean,
    )

    /** 둘 다 받으면 저장, 하나라도 못 받으면 저장값. 7일 지나면 비운다 (REQ_CM_009) */
    private suspend fun stationFor(location: LatLng?, chargeRecommended: Boolean, now: Instant): StationValues {
        val requested = location != null
        if (location != null) {
            val recommendation = api.recommendation(location.lat, location.lng, chargeRecommended)
            val nearby = api.stations(location.lat, location.lng, size = NEAREST_PAGE_SIZE)
            if (recommendation is ApiResult.Success && nearby is ApiResult.Success) {
                val placeName = recommendation.value.placeName()
                val nearestKm = nearby.value.nearestStationKm()
                store.saveStation(placeName, nearestKm, receivedAt = now)
                return StationValues(placeName, nearestKm, lastChecked = null, offline = false)
            }
        }

        val saved = store.load()
        val receivedAt = saved.stationReceivedAt
        if (stationValidity(receivedAt, now).info != Validity.VALID || receivedAt == null) {
            return StationValues(placeName = "", nearestStationKm = null, lastChecked = null, offline = requested)
        }
        return StationValues(
            placeName = saved.placeName.orEmpty(),
            nearestStationKm = saved.nearestStationKm,
            lastChecked = lastCheckedOf(receivedAt, now),
            offline = requested,
        )
    }

    private companion object {
        const val NEAREST_PAGE_SIZE = 5
    }
}
