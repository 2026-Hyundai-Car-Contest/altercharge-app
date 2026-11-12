/**
 * 백엔드 API 8개. 새 라이브러리 없이 HttpURLConnection을 쓴다 (Gradle을 건드리지 않으려고).
 * 취소해도 HttpURLConnection은 시간 초과(연결 10초 · 읽기 15초)까지 기다릴 수 있다.
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
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

/** 서버 시드 차량. MVP는 사용자 1명 고정이라 이 값을 쓴다 (REQ_JD_003) */
const val DEMO_VEHICLE_ID = "mock-vehicle-001"

/** 실패를 원인별로 나눠 화면이 오프라인과 서버 오류를 구분하게 한다 */
sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>

    sealed interface Failure : ApiResult<Nothing>

    data class NetworkError(val cause: IOException) : Failure

    data class HttpError(val status: Int, val error: ApiError?) : Failure

    data class ParseError(val cause: Exception) : Failure

    /** 출시 서버 주소가 비어 있다 */
    data object NotConfigured : Failure
}

class AlterChargeApi(
    private val baseUrl: String = BuildConfig.BASE_URL,
    private val http: HttpClient = UrlConnectionClient(),
) {
    // ---- 01 ~ 04 충전소 ----

    /** 거리순. page는 0부터 */
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

    suspend fun stationDetail(stationId: String): ApiResult<StationDetail> =
        get("stations/${encode(stationId)}") { ServerJson.stationDetail(it) }

    suspend fun recommendation(lat: Double, lng: Double, chargeRecommended: Boolean): ApiResult<Recommendation> =
        get("stations/recommendation", "lat" to lat, "lng" to lng, "chargeRecommended" to chargeRecommended) {
            ServerJson.recommendation(it)
        }

    suspend fun navigationCheck(stationId: String): ApiResult<NavigationCheck> =
        get("stations/${encode(stationId)}/navigation-check") { ServerJson.navigationCheck(it) }

    // ---- 05 · 08 충전 이벤트 ----

    suspend fun postChargingEvent(request: ChargingEventRequest): ApiResult<ChargingEventAccepted> =
        post("charging-events", { ServerJson.chargingEventRequest(request) }) { ServerJson.chargingEventAccepted(it) }

    suspend fun latestCharge(vehicleId: String): ApiResult<LatestChargeHistory> =
        get("charging-events/latest", "vehicleId" to vehicleId) { ServerJson.latestChargeHistory(it) }

    // ---- 06 · 07 차량 스냅샷 (주행 기록) ----

    suspend fun postSnapshot(request: VehicleSnapshotRequest): ApiResult<VehicleSnapshotSaved> =
        post("vehicles/snapshots", { ServerJson.snapshotRequest(request) }) { ServerJson.snapshotSaved(it) }

    /** from · to는 한국시간 날짜, 양끝 포함. 비우면 최근 56일 */
    suspend fun snapshots(vehicleId: String, from: LocalDate? = null, to: LocalDate? = null): ApiResult<VehicleSnapshotHistory> =
        get("vehicles/snapshots", "vehicleId" to vehicleId, "from" to from, "to" to to) { ServerJson.snapshotHistory(it) }

    // ---- 공통 ----

    private suspend fun <T> get(path: String, vararg query: Pair<String, Any?>, parse: (String) -> T): ApiResult<T> =
        call(urlOf(path, query.toList()), method = "GET", body = null, parse = parse)

    /** 차량 값이 NaN이면 본문을 못 만들어 예외가 난다. 앱이 죽지 않게 형식 오류로 돌려준다 */
    private suspend fun <T> post(path: String, body: () -> String, parse: (String) -> T): ApiResult<T> {
        val text = try {
            body()
        } catch (e: Exception) {
            return ApiResult.ParseError(e)
        }
        return call(urlOf(path, emptyList()), method = "POST", body = text, parse = parse)
    }

    /** 연결 · 응답 읽기 · JSON 변환을 모두 IO 스레드에서 한다 (화면 스레드를 막지 않게) */
    private suspend fun <T> call(url: String?, method: String, body: String?, parse: (String) -> T): ApiResult<T> {
        if (url == null) return ApiResult.NotConfigured
        return withContext(Dispatchers.IO) {
            val response = try {
                http.request(method, url, body)
            } catch (e: CancellationException) {
                throw e // 화면이 닫혀 취소된 것은 오류가 아니다
            } catch (e: IOException) {
                return@withContext ApiResult.NetworkError(e)
            } catch (e: Exception) {
                // 잘못된 주소 등 연결 전 오류도 앱이 죽지 않게 네트워크 오류로 돌려준다
                return@withContext ApiResult.NetworkError(IOException(e))
            }
            if (response.status !in 200..299) {
                return@withContext ApiResult.HttpError(response.status, ServerJson.apiErrorOrNull(response.body))
            }
            try {
                ApiResult.Success(parse(response.body))
            } catch (e: Exception) {
                ApiResult.ParseError(e)
            }
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
