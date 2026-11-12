/**
 * 서버 JSON ↔ 응답 · 요청 모델(model/ServerModels.kt) 변환. 안드로이드 기본 org.json만 쓴다 (새 라이브러리 없음).
 *
 * 규칙
 * - 필수 값이 없거나 null이거나 형식이 틀리면 JSONException → 호출부에서 "응답 형식 오류"로 처리한다
 * - null 가능 값은 키가 없거나 JSON null이면 null
 * - 모르는 enum 값은 UNKNOWN이 있으면 UNKNOWN, 없으면 null (서버가 값을 늘려도 앱이 죽지 않게)
 */
package com.altercharge.app.data.remote

import com.altercharge.app.data.remote.model.ApiError
import com.altercharge.app.data.remote.model.ChargerDetail
import com.altercharge.app.data.remote.model.ChargerStatus
import com.altercharge.app.data.remote.model.ChargingEventAccepted
import com.altercharge.app.data.remote.model.ChargingEventRequest
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.LatestChargeHistory
import com.altercharge.app.data.remote.model.NavigationCheck
import com.altercharge.app.data.remote.model.NavigationReason
import com.altercharge.app.data.remote.model.Recommendation
import com.altercharge.app.data.remote.model.RecommendationReason
import com.altercharge.app.data.remote.model.RecommendedStation
import com.altercharge.app.data.remote.model.SpeedCategory
import com.altercharge.app.data.remote.model.StationDetail
import com.altercharge.app.data.remote.model.StationPage
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.data.remote.model.VehicleSnapshotHistory
import com.altercharge.app.data.remote.model.VehicleSnapshotRecord
import com.altercharge.app.data.remote.model.VehicleSnapshotRequest
import com.altercharge.app.data.remote.model.VehicleSnapshotSaved
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.OffsetDateTime

internal object ServerJson {

    // ---- 응답 읽기 ----

    fun stationPage(text: String): StationPage = JSONObject(text).run {
        StationPage(
            items = getJSONArray("items").objects().map(::stationSummary),
            page = getInt("page"),
            size = getInt("size"),
            hasNext = getBoolean("hasNext"),
            fetchedAt = instant("fetchedAt"),
        )
    }

    private fun stationSummary(o: JSONObject) = StationSummary(
        stationId = o.requiredString("stationId"),
        name = o.requiredString("name"),
        address = o.requiredString("address"),
        lat = o.getDouble("lat"),
        lng = o.getDouble("lng"),
        distanceMeters = o.getInt("distanceMeters"),
        availableCount = o.getInt("availableCount"),
        totalValidCount = o.getInt("totalValidCount"),
        chargerTypes = o.getJSONArray("chargerTypes").strings().map { enumOr(it, ConnectorType.UNKNOWN) },
        maxOutputKw = o.optDoubleOrNull("maxOutputKw"),
        visitCount = o.optIntOrNull("visitCount"),
        statusObservedAt = o.optInstant("statusObservedAt"),
    )

    fun stationDetail(text: String): StationDetail = JSONObject(text).run {
        StationDetail(
            stationId = requiredString("stationId"),
            name = requiredString("name"),
            address = requiredString("address"),
            lat = getDouble("lat"),
            lng = getDouble("lng"),
            availableCount = getInt("availableCount"),
            totalValidCount = getInt("totalValidCount"),
            chargers = getJSONArray("chargers").objects().map(::chargerDetail),
            statusObservedAt = optInstant("statusObservedAt"),
        )
    }

    private fun chargerDetail(o: JSONObject) = ChargerDetail(
        chargerId = o.requiredString("chargerId"),
        connectorType = o.requiredString("connectorType"),
        connectorTypes = o.getJSONArray("connectorTypes").strings().map { enumOr(it, ConnectorType.UNKNOWN) },
        chgerType = o.requiredString("chgerType"),
        outputKw = o.optDoubleOrNull("outputKw"),
        status = enumOr(o.requiredString("status"), ChargerStatus.UNKNOWN),
        stat = o.requiredString("stat"),
        statusUpdatedAt = o.optInstant("statusUpdatedAt"),
        statusObservedAt = o.optInstant("statusObservedAt"),
    )

    fun navigationCheck(text: String): NavigationCheck = JSONObject(text).run {
        NavigationCheck(
            stationId = requiredString("stationId"),
            navigationAllowed = getBoolean("navigationAllowed"),
            // 모르는 이유는 막는 쪽으로: 상태를 확인할 수 없다고 본다
            reasonCode = enumOr(requiredString("reasonCode"), NavigationReason.STATUS_UNKNOWN),
            latestStatuses = getJSONArray("latestStatuses").strings().map { enumOr(it, ChargerStatus.UNKNOWN) },
            checkedAt = instant("checkedAt"),
        )
    }

    fun recommendation(text: String): Recommendation = JSONObject(text).run {
        Recommendation(
            station = optJSONObject("station")?.let(::recommendedStation),
            reasonCode = enumOr(requiredString("reasonCode"), RecommendationReason.UNKNOWN),
            fallbackUsed = getBoolean("fallbackUsed"),
            statusObservedAt = optInstant("statusObservedAt"),
        )
    }

    private fun recommendedStation(o: JSONObject) = RecommendedStation(
        stationId = o.requiredString("stationId"),
        name = o.requiredString("name"),
        distanceMeters = o.getInt("distanceMeters"),
        availableCount = o.getInt("availableCount"),
        visitCount = o.optIntOrNull("visitCount"),
        speedCategory = o.optStringOrNull("speedCategory")?.let { enumOrNull<SpeedCategory>(it) },
        maxOutputKw = o.optDoubleOrNull("maxOutputKw"),
    )

    fun snapshotSaved(text: String): VehicleSnapshotSaved = JSONObject(text).run {
        VehicleSnapshotSaved(requiredString("snapshotId"), getBoolean("saved"), instant("recordedAt"))
    }

    fun snapshotHistory(text: String): VehicleSnapshotHistory = JSONObject(text).run {
        VehicleSnapshotHistory(
            vehicleId = requiredString("vehicleId"),
            records = getJSONArray("records").objects().map {
                VehicleSnapshotRecord(
                    snapshotId = it.requiredString("snapshotId"),
                    recordedAt = it.instant("recordedAt"),
                    socPercent = it.getInt("socPercent"),
                    odometerKm = it.getDouble("odometerKm"),
                    drivableRangeKm = it.optIntOrNull("drivableRangeKm"),
                )
            },
            retentionDays = getInt("retentionDays"),
            completeDayCount = getInt("completeDayCount"),
        )
    }

    fun chargingEventAccepted(text: String): ChargingEventAccepted = JSONObject(text).run {
        ChargingEventAccepted(
            eventId = requiredString("eventId"),
            accepted = getBoolean("accepted"),
            matchedStationId = optStringOrNull("matchedStationId"),
            visitCountAdded = getInt("visitCountAdded"),
            dailyDuplicate = getBoolean("dailyDuplicate"),
        )
    }

    fun latestChargeHistory(text: String): LatestChargeHistory = JSONObject(text).run {
        LatestChargeHistory(
            vehicleId = requiredString("vehicleId"),
            lastChargeCompletedAt = optInstant("lastChargeCompletedAt"),
            odometerAtLastChargeKm = optDoubleOrNull("odometerAtLastChargeKm"),
            currentOdometerKm = optDoubleOrNull("currentOdometerKm"),
            distanceSinceChargeKm = optDoubleOrNull("distanceSinceChargeKm"),
            trusted = getBoolean("trusted"),
        )
    }

    /** 오류 본문. 형식이 다르면 null (본문 없는 502 등) */
    fun apiErrorOrNull(text: String?): ApiError? = runCatching {
        JSONObject(text.orEmpty()).run { ApiError(requiredString("error"), requiredString("message")) }
    }.getOrNull()

    // ---- 요청 쓰기 ----

    fun snapshotRequest(r: VehicleSnapshotRequest): String = JSONObject()
        .put("vehicleId", r.vehicleId)
        .put("recordedAt", r.recordedAt.toString())
        .put("socPercent", r.socPercent)
        .put("odometerKm", r.odometerKm)
        .put("drivableRangeKm", r.drivableRangeKm ?: JSONObject.NULL)
        .toString()

    fun chargingEventRequest(r: ChargingEventRequest): String = JSONObject()
        .put("eventId", r.eventId)
        .put("vehicleId", r.vehicleId)
        .put("eventType", r.eventType.name)
        // 외부 경로는 지금 MOCK만 받는다 (서버가 REAL 거부)
        .put("source", "MOCK")
        .put("occurredAt", r.occurredAt.toString())
        .put("lat", r.lat)
        .put("lng", r.lng)
        .put("socPercent", r.socPercent)
        .put("odometerKm", r.odometerKm ?: JSONObject.NULL)
        .toString()

    // ---- 도우미 ----

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    private fun JSONArray.strings(): List<String> = (0 until length()).map { i ->
        if (isNull(i)) throw JSONException("배열 $i 번째 값이 null") else getString(i)
    }

    /**
     * 필수 문자열. 기기의 org.json은 JSON null을 getString으로 읽으면 글자 "null"을 돌려주고,
     * 테스트용 org.json은 예외를 던진다. 둘이 같게 null이면 예외로 통일한다.
     */
    private fun JSONObject.requiredString(key: String): String =
        if (isNull(key)) throw JSONException("$key 값이 없음") else getString(key)

    private fun JSONObject.isAbsent(key: String) = !has(key) || isNull(key)

    private fun JSONObject.optStringOrNull(key: String): String? = if (isAbsent(key)) null else getString(key)

    private fun JSONObject.optIntOrNull(key: String): Int? = if (isAbsent(key)) null else getInt(key)

    private fun JSONObject.optDoubleOrNull(key: String): Double? = if (isAbsent(key)) null else getDouble(key)

    /** 서버 시각은 오프셋 포함 ISO 8601 ("2026-11-12T14:30:00+09:00", "…Z") */
    private fun JSONObject.instant(key: String): Instant = OffsetDateTime.parse(requiredString(key)).toInstant()

    private fun JSONObject.optInstant(key: String): Instant? = optStringOrNull(key)?.let { OffsetDateTime.parse(it).toInstant() }

    private inline fun <reified E : Enum<E>> enumOrNull(name: String): E? = enumValues<E>().firstOrNull { it.name == name }

    private inline fun <reified E : Enum<E>> enumOr(name: String, fallback: E): E = enumOrNull<E>(name) ?: fallback
}
