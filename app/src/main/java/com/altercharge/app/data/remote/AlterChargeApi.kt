/**
 * 백엔드 API 8개 호출. 주소는 BuildConfig.BASE_URL (디버그 = 로컬 서버, 출시 = 배포 서버).
 * 안드로이드 기본 HttpURLConnection만 쓴다 (새 라이브러리 없음). 모든 함수는 IO 스레드에서 돌고 [ApiResult]를 돌려준다.
 *
 * 화면 쪽 사용 예
 *   when (val r = api.recommendation(lat, lng, chargeRecommended = true)) {
 *       is ApiResult.Success -> r.value.placeName()
 *       is ApiResult.Failure -> 저장해 둔 값(OfflineSnapshotStore) 사용
 *   }
 */
package com.altercharge.app.data.remote

import com.altercharge.app.BuildConfig
import com.altercharge.app.data.remote.model.ApiError
import com.altercharge.app.data.remote.model.ChargingEventAccepted
import com.altercharge.app.data.remote.model.ChargingEventRequest
import com.altercharge.app.data.remote.model.LatestChargeHistory
import com.altercharge.app.data.remote.model.NavigationCheck
import com.altercharge.app.data.remote.model.Recommendation
import com.altercharge.app.data.remote.model.StationDetail
import com.altercharge.app.data.remote.model.StationPage
import com.altercharge.app.data.remote.model.VehicleSnapshotHistory
import com.altercharge.app.data.remote.model.VehicleSnapshotRequest
import com.altercharge.app.data.remote.model.VehicleSnapshotSaved
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant

/**
 * 시연용 차량 ID. 서버에 미리 넣어 둔 차량(시드)이고 OpenAPI 예시와 같다. 다른 값은 VEHICLE_NOT_FOUND.
 * MVP는 사용자 1명 고정이라 이 값을 쓴다 (사용자 이름 고정값과 같은 이유, REQ_JD_003).
 */
const val DEMO_VEHICLE_ID = "mock-vehicle-001"

/** API 호출 결과. 실패는 원인별로 나눠 화면이 오프라인 · 서버 오류를 구분하게 한다 */
sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>

    sealed interface Failure : ApiResult<Nothing>

    /** 연결 안 됨 · 시간 초과. 오프라인 처리(REQ_CM_006)로 간다 */
    data class NetworkError(val cause: IOException) : Failure

    /** 서버가 4xx · 5xx로 답했다. [error]는 서버 오류 본문 (예: STATION_DATA_UNAVAILABLE, VEHICLE_NOT_FOUND) */
    data class HttpError(val status: Int, val error: ApiError?) : Failure

    /** 응답은 왔는데 형식이 문서와 다르다 */
    data class ParseError(val cause: Exception) : Failure

    /** 서버 주소가 비어 있다 (출시 주소 미설정) */
    data object NotConfigured : Failure
}

class AlterChargeApi(
    private val baseUrl: String = BuildConfig.BASE_URL,
    private val http: HttpClient = UrlConnectionClient(),
) {
    // ---- 01 ~ 04 충전소 ----

    /** 01 주변 충전소 목록 · 검색. page는 0부터 */
    suspend fun stations(
        lat: Double,
        lng: Double,
        radiusKm: Double? = null,
        keyword: String? = null,
        page: Int? = null,
        size: Int? = null,
    ): ApiResult<StationPage> = get(
        "stations",
        "lat" to lat, "lng" to lng, "radiusKm" to radiusKm, "keyword" to keyword, "page" to page, "size" to size,
    ) { ServerJson.stationPage(it) }

    /** 02 충전소 상세 · 충전기 상태 */
    suspend fun stationDetail(stationId: String): ApiResult<StationDetail> =
        get("stations/${encode(stationId)}") { ServerJson.stationDetail(it) }

    /** 03 추천 충전소 1곳 */
    suspend fun recommendation(lat: Double, lng: Double, chargeRecommended: Boolean): ApiResult<Recommendation> =
        get("stations/recommendation", "lat" to lat, "lng" to lng, "chargeRecommended" to chargeRecommended) {
            ServerJson.recommendation(it)
        }

    /** 04 길 안내 직전 상태 확인 (EX_004) */
    suspend fun navigationCheck(stationId: String): ApiResult<NavigationCheck> =
        get("stations/${encode(stationId)}/navigation-check") { ServerJson.navigationCheck(it) }

    // ---- 05 · 08 충전 이벤트 ----

    /** 05 충전 이벤트 저장. 같은 eventId로 다시 보내도 안전하다 (멱등) */
    suspend fun postChargingEvent(request: ChargingEventRequest): ApiResult<ChargingEventAccepted> =
        post("charging-events", ServerJson.chargingEventRequest(request)) { ServerJson.chargingEventAccepted(it) }

    /** 08 최근 충전 완료 이력 */
    suspend fun latestCharge(vehicleId: String): ApiResult<LatestChargeHistory> =
        get("charging-events/latest", "vehicleId" to vehicleId) { ServerJson.latestChargeHistory(it) }

    // ---- 06 · 07 차량 스냅샷 (주행 기록) ----

    /** 06 차량 스냅샷 저장. 같은 값 재전송은 기존 ID를 돌려준다 */
    suspend fun postSnapshot(request: VehicleSnapshotRequest): ApiResult<VehicleSnapshotSaved> =
        post("vehicles/snapshots", ServerJson.snapshotRequest(request)) { ServerJson.snapshotSaved(it) }

    /** 07 최근 차량 스냅샷 기록. from · to를 비우면 서버 보관 기간 전체 */
    suspend fun snapshots(vehicleId: String, from: Instant? = null, to: Instant? = null): ApiResult<VehicleSnapshotHistory> =
        get("vehicles/snapshots", "vehicleId" to vehicleId, "from" to from, "to" to to) { ServerJson.snapshotHistory(it) }

    // ---- 공통 ----

    private suspend fun <T> get(path: String, vararg query: Pair<String, Any?>, parse: (String) -> T): ApiResult<T> =
        call(urlOf(path, query.toList()), method = "GET", body = null, parse = parse)

    private suspend fun <T> post(path: String, body: String, parse: (String) -> T): ApiResult<T> =
        call(urlOf(path, emptyList()), method = "POST", body = body, parse = parse)

    private suspend fun <T> call(url: String?, method: String, body: String?, parse: (String) -> T): ApiResult<T> {
        if (url == null) return ApiResult.NotConfigured
        val response = try {
            withContext(Dispatchers.IO) { http.request(method, url, body) }
        } catch (e: IOException) {
            return ApiResult.NetworkError(e)
        }
        if (response.status !in 200..299) return ApiResult.HttpError(response.status, ServerJson.apiErrorOrNull(response.body))
        return try {
            ApiResult.Success(parse(response.body))
        } catch (e: Exception) {
            ApiResult.ParseError(e)
        }
    }

    /** null 값은 쿼리에서 뺀다. 주소가 비어 있으면 null */
    internal fun urlOf(path: String, query: List<Pair<String, Any?>>): String? {
        if (baseUrl.isBlank()) return null
        val base = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val params = query.mapNotNull { (k, v) -> v?.let { "$k=${encode(it.toString())}" } }
        return base + path + if (params.isEmpty()) "" else "?" + params.joinToString("&")
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
}

/** HTTP 응답. 본문은 오류여도 읽어 둔다 (서버 오류 본문에 이유가 있다) */
data class HttpResponse(val status: Int, val body: String)

/** 실제 연결을 바꿔 끼우는 자리. 테스트는 가짜 응답을 돌려준다 */
fun interface HttpClient {
    @Throws(IOException::class)
    fun request(method: String, url: String, body: String?): HttpResponse
}

class UrlConnectionClient(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000,
) : HttpClient {
    override fun request(method: String, url: String, body: String?): HttpResponse {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = connectTimeoutMs
            conn.readTimeout = readTimeoutMs
            conn.setRequestProperty("Accept", "application/json")
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            return HttpResponse(status, text)
        } finally {
            conn.disconnect()
        }
    }
}
