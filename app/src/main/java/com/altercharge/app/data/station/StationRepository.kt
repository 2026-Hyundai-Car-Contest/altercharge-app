/**
 * 주변 충전소 목록을 받아 저장하고, 연결이 끊기면 저장한 목록을 돌려준다 (REQ_SL_001 · REQ_CM_006).
 * 검색(REQ_SL_002)은 서버 01의 keyword로 한다. 검색 결과는 저장하지 않는다: 저장할 값은 반경 3km 전체 목록이다.
 */
package com.altercharge.app.data.station

import java.time.Duration
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.Mutex
import com.altercharge.app.data.local.StationCacheStore
import com.altercharge.app.data.local.VisitCountStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.ApiResult
import com.altercharge.app.data.remote.model.StationSummary
import com.altercharge.app.domain.time.KoreaZone
import java.time.Clock
import java.time.Instant

/** REQ_SL_001 반경은 서버 기본값과 같은 3km다 */
const val NEARBY_RADIUS_KM: Double = 3.0

class StationRepository(
    private val api: AlterChargeApi,
    private val cache: StationCacheStore,
    private val visits: VisitCountStore,
    private val clock: Clock = Clock.system(KoreaZone),
    private val radiusKm: Double = NEARBY_RADIUS_KM,
) {
    /**
     * @property receivedAt 목록을 받은 시각. 저장값을 썼으면 그때 받은 시각이다
     * @property offline 서버에서 받지 못해 저장값(또는 일부만 받은 값)을 쓴다
     * @property failure 검색처럼 실패를 화면에 알려야 할 때
     */
    data class NearbyResult(
        val stations: List<StationSummary>,
        val receivedAt: Instant?,
        val offline: Boolean,
        val fromCache: Boolean,
        val failure: ApiResult.Failure? = null,
    )

    /**
     * 방금 받은 전체 목록 (위치 · 받은 시각). 홈 시작 때 목록 · 충전 시작 판정이 같은 목록을 연달아 부르므로
     * [FRESH_REUSE] 안에서는 다시 받지 않는다. 동시에 불러도 [nearbyLock]으로 한 번만 받는다.
     */
    private var lastFresh: Triple<Double, Double, NearbyResult>? = null
    private val nearbyLock = Mutex()

    /** 받아 둔 목록. 없으면 null */
    fun cached(): NearbyResult? = cache.load()?.let {
        NearbyResult(it.stations, it.receivedAt, offline = true, fromCache = true)
    }

    /** 기기에 센 방문 횟수 (REQ_ST_008) */
    fun localVisitCounts(): Map<String, Int> = visits.counts()

    /**
     * 반경 3km 목록을 끝 페이지까지 받는다. 서울 3km에는 400곳이 넘어 한 페이지로는 모자라다.
     * keyword가 비어 있지 않으면 서버가 이름 · 주소로 찾은 결과를 받는다 (REQ_SL_002).
     */
    suspend fun nearby(lat: Double, lng: Double, keyword: String? = null): NearbyResult {
        val word = keyword?.trim()?.takeIf { it.isNotEmpty() }
        if (word != null) return fetch(lat, lng, word)
        return nearbyLock.withLock {
            val recent = lastFresh
            val now = clock.instant()
            if (recent != null && recent.first == lat && recent.second == lng &&
                recent.third.receivedAt?.let { Duration.between(it, now) < FRESH_REUSE } == true
            ) {
                recent.third
            } else {
                fetch(lat, lng, word = null).also { if (!it.offline) lastFresh = Triple(lat, lng, it) }
            }
        }
    }

    private suspend fun fetch(lat: Double, lng: Double, word: String?): NearbyResult {
        val collected = mutableListOf<StationSummary>()
        var failure: ApiResult.Failure? = null
        var page = 0
        var hasNext = true
        while (hasNext && page < MAX_PAGES) {
            when (val result = api.stations(lat, lng, radiusKm = radiusKm, keyword = word, page = page, size = PAGE_SIZE)) {
                is ApiResult.Success -> {
                    collected += result.value.items
                    hasNext = result.value.hasNext
                    page++
                }
                is ApiResult.Failure -> {
                    failure = result
                    hasNext = false
                }
            }
        }

        val now = clock.instant()
        if (failure == null) {
            // 검색 결과는 저장하지 않는다. 저장값은 다음에 연결이 끊겼을 때 쓰는 전체 목록이다
            if (word == null) cache.save(collected, now)
            return NearbyResult(collected, now, offline = false, fromCache = false)
        }
        if (collected.isNotEmpty()) {
            // 일부 페이지까지만 받았다. 보여 주되 오프라인으로 알린다 (REQ_CM_006)
            return NearbyResult(collected, now, offline = true, fromCache = false, failure = failure)
        }
        if (word != null) {
            // 검색은 저장값으로 대신할 수 없다 (REQ_SL_002 예외)
            return NearbyResult(emptyList(), null, offline = true, fromCache = false, failure = failure)
        }
        val saved = cache.load()
        return if (saved != null) {
            NearbyResult(saved.stations, saved.receivedAt, offline = true, fromCache = true, failure = failure)
        } else {
            NearbyResult(emptyList(), null, offline = true, fromCache = false, failure = failure)
        }
    }

    private companion object {
        /** 다시 받지 않고 쓰는 시간. 가용 대수 유효 15분(REQ_CM_009)보다 훨씬 짧게 둔다 */
        val FRESH_REUSE: Duration = Duration.ofSeconds(30)

        /** 서버가 허용하는 최대 페이지 크기 */
        const val PAGE_SIZE = 200

        /** 200곳 × 10장 = 2,000곳. 반경 3km에서 이보다 많으면 목록을 나눠 보여 줘야 한다 */
        const val MAX_PAGES = 10
    }
}
