/**
 * 추천 충전소로 길 안내 (REQ_BR_004 안내 요청 · REQ_BR_005 내비 목적지 변경).
 * 안내 직전에 서버에 충전기 상태를 한 번 더 묻고(navigation-check), 갈 수 있을 때만 내비 목적지를 바꾼다.
 * 경로 계산은 내비가 맡는다.
 */
package com.altercharge.app.data.navi

import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.ApiResult
import com.altercharge.app.data.remote.model.NavigationReason

/** 내비에 넘길 목적지 */
data class RouteDestination(
    val stationId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String?,
)

/** 내비 목적지를 바꾸는 자리. 실제 차는 [PleosRouteLauncher], 테스트는 가짜를 끼운다 */
fun interface RouteLauncher {
    /** @return 목적지 요청을 내비에 넘겼으면 true. 경로 탐색 결과는 내비가 화면에 보여 준다 */
    suspend fun launch(destination: RouteDestination): Boolean

    /** 내비 연결을 닫는다. 화면이 아니라 홈 ViewModel이 끝날 때 부른다 (화면을 다시 만들어도 연결을 재사용) */
    fun release() {}
}

sealed interface NavigationOutcome {
    data object Started : NavigationOutcome

    /** 서버가 지금은 안내하지 않는 게 낫다고 판단 (빈 충전기 없음 · 상태 오래됨 · 운영 중지 등) */
    data class Unavailable(val reason: NavigationReason) : NavigationOutcome

    /** 상세를 못 받아 좌표가 없다 */
    data object NoDestination : NavigationOutcome

    /** 연결이 끊겨 확인할 수 없다 (REQ_CM_006) */
    data object Offline : NavigationOutcome

    /** 내비가 응답하지 않거나 서버 오류 */
    data object Failed : NavigationOutcome
}

class StationNavigator(
    private val api: AlterChargeApi,
    private val launcher: RouteLauncher,
) {
    suspend fun navigate(destination: RouteDestination?): NavigationOutcome {
        if (destination == null) return NavigationOutcome.NoDestination
        return when (val check = api.navigationCheck(destination.stationId)) {
            is ApiResult.Success -> when {
                !check.value.navigationAllowed -> NavigationOutcome.Unavailable(check.value.reasonCode)
                launcher.launch(destination) -> NavigationOutcome.Started
                else -> NavigationOutcome.Failed
            }
            is ApiResult.NetworkError -> NavigationOutcome.Offline
            // 서버 주소 미설정 · 서버 오류 · 응답 형식 오류
            else -> NavigationOutcome.Failed
        }
    }

    fun release() = launcher.release()
}
