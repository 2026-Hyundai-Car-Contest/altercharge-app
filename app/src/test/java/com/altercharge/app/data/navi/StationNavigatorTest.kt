package com.altercharge.app.data.navi

import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.remote.HttpClient
import com.altercharge.app.data.remote.HttpResponse
import com.altercharge.app.data.remote.model.NavigationReason
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class StationNavigatorTest {

    /** 배포 서버(2026-11-14) 응답 모양 그대로: latestStatuses 없음 */
    private class FakeServer : HttpClient {
        var online = true
        var body = """{"stationId":"ST1","navigationAllowed":true,"reasonCode":"OK","checkedAt":"2026-11-14T12:55:32Z"}"""
        val urls = mutableListOf<String>()
        override fun request(method: String, url: String, body: String?): HttpResponse {
            urls += url
            if (!online) throw IOException("offline")
            return HttpResponse(200, this.body)
        }
    }

    private class FakeLauncher(var accept: Boolean = true) : RouteLauncher {
        val launched = mutableListOf<RouteDestination>()
        override suspend fun launch(destination: RouteDestination): Boolean {
            launched += destination
            return accept
        }
    }

    private val server = FakeServer()
    private val launcher = FakeLauncher()
    private val navigator = StationNavigator(AlterChargeApi("https://example.test/api/v1/", server), launcher)
    private val destination = RouteDestination("ST1", "서울시 본관청사", 37.5665, 126.978, "서울특별시 중구 세종대로 110")

    @Test
    fun 갈_수_있으면_내비_목적지를_바꾼다() = runBlocking {
        assertEquals(NavigationOutcome.Started, navigator.navigate(destination))
        assertEquals(listOf(destination), launcher.launched)
        assertTrue(server.urls.single().endsWith("stations/ST1/navigation-check"))
    }

    @Test
    fun 빈_충전기가_없으면_안내하지_않는다() = runBlocking {
        server.body = """{"stationId":"ST1","navigationAllowed":false,"reasonCode":"NO_AVAILABLE_CHARGER","checkedAt":"2026-11-14T12:55:32Z"}"""
        assertEquals(NavigationOutcome.Unavailable(NavigationReason.NO_AVAILABLE_CHARGER), navigator.navigate(destination))
        assertTrue(launcher.launched.isEmpty())
    }

    @Test
    fun 오프라인이면_확인할_수_없다고_알린다() = runBlocking {
        server.online = false
        assertEquals(NavigationOutcome.Offline, navigator.navigate(destination))
        assertTrue(launcher.launched.isEmpty())
    }

    @Test
    fun 좌표가_없으면_서버에_묻지_않는다() = runBlocking {
        assertEquals(NavigationOutcome.NoDestination, navigator.navigate(null))
        assertTrue(server.urls.isEmpty())
    }

    @Test
    fun 내비가_요청을_받지_못하면_실패() = runBlocking {
        launcher.accept = false
        assertEquals(NavigationOutcome.Failed, navigator.navigate(destination))
    }
}
