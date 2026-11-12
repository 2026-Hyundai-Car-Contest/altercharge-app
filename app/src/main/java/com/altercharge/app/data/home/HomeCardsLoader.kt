/**
 * 홈 화면 카드 값을 한 번에 만든다 (#68). 홈 화면(석기님)은 차량 값과 위치만 넘기고, 결과를 각 카드에 넘기면 된다.
 *
 * 안에서 하는 일 (순서대로)
 * 1. 서버에서 주행 기록 · 마지막 충전을 받는다
 * 2. 판정을 다시 계산할지 정하고(REQ_JD_004), 계산했으면 결과와 계산 기록을 저장한다
 * 3. 추천 충전소 · 가까운 충전소를 받고 저장한다. 못 받으면 저장해 둔 값과 "N분 전 기준"을 쓴다 (REQ_CM_006)
 * 4. L1 · L2 카드 값과 C 카드에 넣을 서버 값을 만든다
 *
 * 화면 배치 · 차량 SDK · 위치는 모른다. 그 값은 부르는 쪽이 넘긴다.
 *
 * 사용 예 (홈 화면 ViewModel)
 *   val cards = loader.load(VehicleReading(soc, rangeKm, odometerKm), location = LatLng(lat, lng))
 *   CardInfo(state = cards.cardInfo, …)
 *   CardDriveRecord(state = cards.driveRecord, …)
 *   CardVehicleStatus(state = CardVehicleStatusUiState(
 *       …차량 SDK 값…,
 *       chargeRecommended = cards.vehicleStatus.chargeRecommended,
 *       nearestStationKm = cards.vehicleStatus.nearestStationKm,
 *       lastChargedAt = cards.vehicleStatus.lastChargedAt,
 *   ), …)
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

/** 차량 SDK에서 읽은 값 (홈 화면이 넘긴다) */
data class VehicleReading(
    val socPercent: Int,
    /** 주행가능거리 km */
    val rangeKm: Int,
    /** 누적 주행거리 km. 오늘 주행거리 계산에 쓴다 */
    val odometerKm: Double,
)

data class LatLng(val lat: Double, val lng: Double)

/** C 카드에 넣을 서버 · 판정 값. 나머지(충전 상태 · SOC 등)는 홈 화면이 차량 SDK로 채운다 */
data class VehicleStatusServerValues(
    /** 판정 결과의 충전 권장 (REQ_ST_005). C 카드 chargeRecommended에 항상 넘긴다 */
    val chargeRecommended: Boolean,
    /** 가까운 충전소 거리 km. 모르면 null ("--") */
    val nearestStationKm: Double?,
    /** 마지막 충전 시각 글자 ("07:30" · "11.11"). 모르면 null ("--") */
    val lastChargedAt: String?,
)

/** 홈 화면 카드 값 묶음 */
data class HomeCards(
    val cardInfo: CardInfoUiState,
    val driveRecord: CardDriveRecordUiState,
    val vehicleStatus: VehicleStatusServerValues,
    /** 이번에 서버를 하나라도 못 받았는지. 화면에서 따로 안내할 때 쓴다 */
    val offline: Boolean,
)

class HomeCardsLoader(
    private val api: AlterChargeApi,
    private val store: OfflineSnapshotStore,
    private val clock: Clock = Clock.system(KoreaZone),
    private val vehicleId: String = DEMO_VEHICLE_ID,
) {
    /**
     * 연결이 끊겼을 때 L2에 보여 줄 마지막 주행 기록. 앱이 켜져 있는 동안만 기억한다
     * (기기 저장은 판정 · 충전소만 한다 — 주행 기록은 서버 보관이 원칙, REQ_JD_002).
     */
    private var lastHistory: VehicleSnapshotHistory? = null
    private var lastCharge: LatestChargeHistory? = null

    /**
     * @param location 지금 위치. 모르면 null이고, 충전소는 저장해 둔 값을 쓴다
     */
    suspend fun load(vehicle: VehicleReading, location: LatLng?): HomeCards {
        val now = clock.instant()
        val today = now.atZone(KoreaZone).toLocalDate() // LocalDate.ofInstant는 API 34부터라 쓰지 않는다 (minSdk 28)

        // 1. 주행 기록 · 마지막 충전
        val historyResult = api.snapshots(vehicleId)
        val chargeResult = api.latestCharge(vehicleId)
        (historyResult as? ApiResult.Success)?.let { lastHistory = it.value }
        (chargeResult as? ApiResult.Success)?.let { lastCharge = it.value }
        val history = lastHistory
        val charge = lastCharge

        // 2. 판정
        val judgment = judgmentFor(vehicle, history, charge, now, today)

        // 3. 충전소
        val station = stationFor(location, judgment.chargeRecommended, now)

        // 4. 카드 값
        val cardInfo = judgment.toCardInfoUiState(
            placeName = station.placeName,
            placeLastChecked = station.lastChecked,
        )
        val driveRecord = (history ?: EMPTY_HISTORY.copy(vehicleId = vehicleId))
            .toCardDriveRecordUiState(weekStart = weekStartOf(today), today = today, latestCharge = charge)

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

    /**
     * 판정. 다시 계산할 때(REQ_JD_004)만 judge()를 부르고 결과 · 계산 기록을 저장한다. 아니면 저장한 판정을 그대로 쓴다.
     * 주행 기록을 한 번도 못 받았으면 기록 없음(수집 중)으로 계산하되 저장하지 않는다 — 나중에 받으면 바로 다시 계산하게.
     */
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
        /** 저장값을 쓰면 받은 시점, 방금 받았으면 null */
        val lastChecked: LastChecked?,
        val offline: Boolean,
    )

    /**
     * 추천 충전소(L1 이름) · 가까운 충전소(C 거리). 둘 다 받으면 저장하고, 하나라도 못 받으면 저장값을 쓴다.
     * 저장값이 7일을 넘었거나 없으면 이름 · 거리를 비운다 (L1은 장소 줄을 숨기고, C는 "--").
     */
    private suspend fun stationFor(location: LatLng?, chargeRecommended: Boolean, now: Instant): StationValues {
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
            return StationValues(placeName = "", nearestStationKm = null, lastChecked = null, offline = true)
        }
        return StationValues(
            placeName = saved.placeName.orEmpty(),
            nearestStationKm = saved.nearestStationKm,
            lastChecked = lastCheckedOf(receivedAt, now),
            offline = true,
        )
    }

    private companion object {
        /** 가까운 충전소만 보면 되므로 첫 페이지 몇 개면 충분하다 (서버가 거리순 정렬) */
        const val NEAREST_PAGE_SIZE = 5

        val EMPTY_HISTORY = VehicleSnapshotHistory(vehicleId = "", records = emptyList(), retentionDays = 0, completeDayCount = 0)
    }
}
