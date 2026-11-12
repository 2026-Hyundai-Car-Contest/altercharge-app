/**
 * 차량 값을 서버로 보내 주행 기록을 쌓는다 (REQ_JD_002). 못 보낸 값은 기기에 모았다가 연결되면 보낸다 (REQ_CM_006).
 *
 *   uploader.record(VehicleReading(…), now)   // 차량 값을 읽을 때마다
 *   uploader.flush()                           // load 전에, 연결이 돌아왔을 때
 */
package com.altercharge.app.data.upload

import com.altercharge.app.data.home.VehicleReading
import com.altercharge.app.data.local.KeyValueStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.ApiResult
import com.altercharge.app.data.remote.DEMO_VEHICLE_ID
import com.altercharge.app.data.remote.model.VehicleSnapshotRequest
import com.altercharge.app.domain.time.KoreaZone
import java.time.Duration
import java.time.Instant

class SnapshotUploader(
    private val api: AlterChargeApi,
    private val store: KeyValueStore,
    private val vehicleId: String = DEMO_VEHICLE_ID,
) {
    /** 보낼 때가 되면 대기열에 넣는다. 실제 전송은 [flush] */
    fun record(reading: VehicleReading, now: Instant) {
        if (!isDue(lastQueuedAt(), now)) return
        val queue = (pending() + VehicleSnapshotRequest(vehicleId, now, reading.socPercent, reading.odometerKm, reading.rangeKm))
            .takeLast(MAX_PENDING)
        save(queue, lastQueuedAt = now)
    }

    /**
     * 대기열을 오래된 것부터 보낸다. 연결이 안 되면 멈추고 남은 것은 다음에 보낸다.
     * 서버가 거절한 값(400 · 404 · 409)은 다시 보내도 같으므로 버린다.
     * @return 이번에 보낸 개수
     */
    suspend fun flush(now: Instant = Instant.now()): Int {
        var queue = pending().filter { Duration.between(it.recordedAt, now) <= SERVER_RETENTION }
        var sent = 0
        while (queue.isNotEmpty()) {
            when (val r = api.postSnapshot(queue.first())) {
                is ApiResult.Success -> sent++
                is ApiResult.HttpError -> if (r.status >= 500) break
                is ApiResult.NetworkError, ApiResult.NotConfigured -> break
                is ApiResult.ParseError -> Unit
            }
            queue = queue.drop(1)
        }
        save(queue, lastQueuedAt())
        return sent
    }

    fun pendingCount(): Int = pending().size

    private fun pending(): List<VehicleSnapshotRequest> =
        store.getString(KEY_PENDING).orEmpty().lines().filter { it.isNotBlank() }.mapNotNull(::decode)

    private fun lastQueuedAt(): Instant? = store.getString(KEY_LAST_QUEUED_AT)?.let { runCatching { Instant.parse(it) }.getOrNull() }

    private fun save(queue: List<VehicleSnapshotRequest>, lastQueuedAt: Instant?) {
        store.putStrings(
            mapOf(
                KEY_PENDING to queue.joinToString("\n", transform = ::encode),
                KEY_LAST_QUEUED_AT to lastQueuedAt?.toString(),
            ),
        )
    }

    private fun encode(r: VehicleSnapshotRequest) =
        listOf(r.recordedAt, r.socPercent, r.odometerKm, r.drivableRangeKm ?: "").joinToString(SEP)

    private fun decode(line: String): VehicleSnapshotRequest? = runCatching {
        val (at, soc, odo, range) = line.split(SEP)
        VehicleSnapshotRequest(vehicleId, Instant.parse(at), soc.toInt(), odo.toDouble(), range.toIntOrNull())
    }.getOrNull()

    private companion object {
        const val KEY_PENDING = "snapshot_pending"
        const val KEY_LAST_QUEUED_AT = "snapshot_last_queued_at"
        const val SEP = "|"

        /** 서버가 보관하는 기간. 이보다 오래된 값은 보내도 거절된다 */
        val SERVER_RETENTION: Duration = Duration.ofDays(55)

        /** 30분마다 하루 이틀 끊겨도 다 담는다 */
        const val MAX_PENDING = 200
    }
}

/**
 * 보낼 때인지. 서버는 하루 기록을 "2건 이상 · 간격 6시간 이하"일 때 유효로 센다(OpenAPI 07 completeDayCount).
 * 넉넉하게 30분마다 보내고, 날짜가 바뀌면 바로 보내 그날 첫 기록을 남긴다.
 */
internal fun isDue(lastQueuedAt: Instant?, now: Instant): Boolean {
    if (lastQueuedAt == null) return true
    val newDay = lastQueuedAt.atZone(KoreaZone).toLocalDate() != now.atZone(KoreaZone).toLocalDate()
    return newDay || Duration.between(lastQueuedAt, now) >= SEND_INTERVAL
}

internal val SEND_INTERVAL: Duration = Duration.ofMinutes(30)
