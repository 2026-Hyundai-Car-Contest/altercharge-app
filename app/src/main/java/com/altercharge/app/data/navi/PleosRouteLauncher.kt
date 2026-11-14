package com.altercharge.app.data.navi

import ai.pleos.playground.navi.constants.RouteOption
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.helper.NaviHelper
import android.content.Context
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Pleos NaviHelper로 내비 목적지를 바꾼다 (REQ_BR_005). 퍼미션 `pleos.car.permission.NAVI_ROUTE_SEARCH`.
 * 내비 서비스 연결(initialize)은 처음 안내할 때 한 번만 한다. 홈 ViewModel이 끝날 때 [release]를 부른다.
 *
 * requestRoute가 끝나도 경로 탐색이 끝난 건 아니다(Pleos 문서). 탐색 결과 · 오류는 내비 화면이 직접 보여 준다.
 */
class PleosRouteLauncher(context: Context) : RouteLauncher {

    private val naviHelper = NaviHelper(context.applicationContext)
    private val connectLock = Mutex()
    @Volatile private var connected = false

    override suspend fun launch(destination: RouteDestination): Boolean {
        if (!ensureConnected()) return false
        val sent = runCatching {
            naviHelper.requestRoute(
                RouteInfo(
                    latitude = destination.lat,
                    longitude = destination.lng,
                    displayLatitude = null,
                    displayLongitude = null,
                    poiName = destination.name,
                    // 비우면 내비가 요청을 받지 않는다(에뮬레이터 "navi not ready"). 내비 장소 ID가 아니라 장소 조회는 실패하지만
                    // 경로는 좌표로 잡힌다 (에뮬레이터에서 확인)
                    poiId = destination.stationId,
                    poiSubId = "0",
                    address = destination.address.orEmpty(),
                    routeOption = RouteOption.RECOMMENDED,
                ),
            )
        }.isSuccess
        // 내비 서비스가 끊겨 요청이 실패했으면 다음 탭에서 다시 연결한다
        if (!sent) connected = false
        return sent
    }

    private suspend fun ensureConnected(): Boolean = connectLock.withLock {
        if (connected) return@withLock true
        // 내비 서비스가 응답하지 않으면 버튼이 계속 막히지 않게 기다리는 시간을 둔다. 다음 탭에서 다시 연결한다
        connected = withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                runCatching {
                    naviHelper.initialize(
                        { if (cont.isActive) cont.resume(true) },
                        { if (cont.isActive) cont.resume(false) },
                    )
                }.onFailure { if (cont.isActive) cont.resume(false) }
            }
        } ?: false
        connected
    }

    override fun release() {
        runCatching { naviHelper.release() }
        connected = false
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 5_000L
    }
}
