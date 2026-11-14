/**
 * 충전 시작을 충전소 방문으로 기록한다 (REQ_ST_008).
 * 방문 1회 = 충전 시작 위치가 충전소 반경 100m 안, 같은 충전소는 하루 1회.
 * 못 보낸 이벤트는 기기에 모아 두었다가 연결되면 보낸다 (REQ_CM_006). 화면의 횟수는 보내기 전에 이미 +1 된다.
 *
 *   val result = recorder.onChargingStarted(reading, now, lat, lng, stations)
 *   recorder.flush()   // 목록을 불러오기 전 · 연결이 돌아왔을 때
 */
package com.altercharge.app.data.upload

import com.altercharge.app.data.home.VehicleReading
import com.altercharge.app.data.local.KeyValueStore
import com.altercharge.app.data.local.VisitCountStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.ApiResult
import com.altercharge.app.data.remote.DEMO_VEHICLE_ID
import com.altercharge.app.data.remote.model.ChargingEventRequest
import com.altercharge.app.data.remote.model.ChargingEventType
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.data.station.nearestStationWithin
import com.altercharge.app.domain.time.KoreaZone
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class ChargingEventRecorder(
    private val api: AlterChargeApi,
    private val store: KeyValueStore,
    private val visits: VisitCountStore,
    private val vehicleId: String = DEMO_VEHICLE_ID,
) {
    /**
     * @property stationId 100m 안에서 찾은 충전소. 없으면 null
     * @property visitCount 오늘 처음 센 방문이면 늘어난 횟수, 같은 날 다시 왔으면 null
     */
    data class Recorded(val stationId: String?, val visitCount: Int?)

    /**
     * 충전이 시작될 때 부른다. 반경 100m 안 충전소가 있으면 그 충전소의 오늘 방문을 세고(하루 1회),
     * 서버로 보낼 START 이벤트를 대기열에 넣는다. 이벤트는 충전소를 못 찾아도 남긴다.
     */
    fun onChargingStarted(
        reading: VehicleReading,
        at: Instant,
        lat: Double,
        lng: Double,
        stations: List<StationSummary>,
        date: LocalDate = at.atZone(KoreaZone).toLocalDate(),
    ): Recorded {
        val station = nearestStationWithin(stations, lat, lng)
        val count = station?.let { visits.recordIfFirstToday(it.stationId, date) }
        enqueue(
            ChargingEventRequest(
                eventId = eventId(at, station?.stationId),
                vehicleId = vehicleId,
                eventType = ChargingEventType.START,
                occurredAt = at,
                lat = lat,
                lng = lng,
                socPercent = reading.socPercent,
                odometerKm = reading.odometerKm,
            ),
        )
        return Recorded(station?.stationId, count)
    }

    /**
     * 오래된 것부터 보낸다. 연결 안 됨 · 5xx · 408 · 429는 멈추고 남긴다.
     * 다른 4xx는 다시 보내도 같아서 버린다. 보내는 동안 새로 들어온 이벤트는 건드리지 않는다.
     * @return 보낸 개수
     */
    suspend fun flush(now: Instant = Instant.now()): Int {
        val toSend = pending().filter { isWithinRetention(it, now) }
        val done = mutableSetOf<String>()
        var sent = 0
        for (request in toSend) {
            val result = api.postChargingEvent(request)
            if (result.shouldRetryLater()) break
            if (result is ApiResult.Success) sent++
            done += request.eventId
        }
        save(pending().filter { it.eventId !in done && isWithinRetention(it, now) })
        return sent
    }

    fun pendingCount(): Int = pending().size

    private fun enqueue(request: ChargingEventRequest) {
        // 멱등 키가 같은 이벤트가 이미 대기 중이면 다시 넣지 않는다
        val queue = pending().filterNot { it.eventId == request.eventId } + request
        save(queue.takeLast(MAX_PENDING))
    }

    /** 시각과 충전소로 만든다. 길이 제한을 넘으면 뒤를 자르지 않고 충전소 ID를 해시로 줄인다: 잘린 두 ID가 같아지지 않게 */
    private fun eventId(at: Instant, stationId: String?): String {
        val full = "visit-${at.toEpochMilli()}-${stationId ?: "none"}"
        if (full.length <= MAX_EVENT_ID_LENGTH) return full
        return "visit-${at.toEpochMilli()}-h${Integer.toHexString(stationId.hashCode())}"
    }

    private fun isWithinRetention(request: ChargingEventRequest, now: Instant): Boolean =
        Duration.between(request.occurredAt, now) <= EVENT_RETENTION

    private fun ApiResult<*>.shouldRetryLater(): Boolean = when (this) {
        is ApiResult.NetworkError, ApiResult.NotConfigured -> true
        is ApiResult.HttpError -> status >= 500 || status == 408 || status == 429
        else -> false
    }

    private fun pending(): List<ChargingEventRequest> =
        store.getString(KEY_PENDING).orEmpty().lines().filter { it.isNotBlank() }.mapNotNull(::decode)

    private fun save(queue: List<ChargingEventRequest>) {
        store.putStrings(mapOf(KEY_PENDING to queue.joinToString("\n", transform = ::encode).ifEmpty { null }))
    }

    private fun encode(r: ChargingEventRequest): String = listOf(
        r.eventId,
        r.occurredAt.toString(),
        r.lat.toString(),
        r.lng.toString(),
        r.socPercent.toString(),
        r.odometerKm?.toString().orEmpty(),
    ).joinToString(SEP)

    private fun decode(line: String): ChargingEventRequest? = runCatching {
        val fields = line.split(SEP)
        require(fields.size == 6)
        ChargingEventRequest(
            eventId = fields[0],
            vehicleId = vehicleId,
            eventType = ChargingEventType.START,
            occurredAt = Instant.parse(fields[1]),
            lat = fields[2].toDouble(),
            lng = fields[3].toDouble(),
            socPercent = fields[4].toInt(),
            odometerKm = fields[5].toDoubleOrNull(),
        )
    }.getOrNull()

    private companion object {
        const val KEY_PENDING = "charging_event_pending"
        const val SEP = "|"

        /** 보관 기간이 지난 이벤트는 서버가 거절한다. 스냅샷과 같은 55일로 넉넉히 잡는다 */
        val EVENT_RETENTION: Duration = Duration.ofDays(55)

        /** 이틀 끊겨도 하루 한 번씩은 다 담는다 */
        const val MAX_PENDING = 60

        const val MAX_EVENT_ID_LENGTH = 100
    }
}
