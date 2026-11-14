package com.altercharge.app.data.home

import com.altercharge.app.data.remote.ServerJson
import com.altercharge.app.data.remote.mapper.lastChargedAtText
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.data.remote.model.SpeedCategory
import com.altercharge.app.data.station.ConnectorFilter
import com.altercharge.app.data.station.PowerRange
import com.altercharge.app.data.station.StationFilter
import com.altercharge.app.data.station.StationSort
import com.altercharge.app.data.vehicle.VehicleTelemetry
import com.altercharge.app.ui.card.station.ChargeInfoTag
import com.altercharge.app.ui.card.station.ChargingType
import com.altercharge.app.ui.card.station.toCardStationUiState
import com.altercharge.app.ui.card.vehiclestatus.ChargingState as VehicleChargingState
import com.altercharge.app.ui.card.vehiclestatus.VehicleSituation
import com.altercharge.app.ui.station.StationListMessage
import com.altercharge.app.ui.station.buildStationList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * 배포 서버가 실제로 돌려준 JSON을 화면 값(C · R1 · R2)까지 이어서 확인한다.
 * 응답 본문은 2026-11-14 18:44 KST에 https://15-164-244-37.sslip.io/api/v1 에서 받은 것이다.
 */
class StationUiStateMappingTest {

    // GET /stations?lat=37.5500&lng=127.1400&radiusKm=3&size=3
    private val listJson = """
        {"items":[
          {"stationId":"NT101224","name":"강동 암사e편한세상아파트","address":"서울특별시 강동구 구천면로 365-13",
           "lat":37.5511368,"lng":127.13926,"distanceMeters":142,"availableCount":1,"totalValidCount":3,
           "chargerTypes":["AC_SLOW"],"maxOutputKw":7.0,"visitCount":null,"statusObservedAt":"2026-11-14T09:43:43.478679Z"},
          {"stationId":"PI801611","name":"동구햇살아파트","address":"서울특별시 강동구 상암로41길 45",
           "lat":37.548225749,"lng":127.14083503,"distanceMeters":211,"availableCount":1,"totalValidCount":2,
           "chargerTypes":["AC_SLOW"],"maxOutputKw":7.0,"visitCount":null,"statusObservedAt":"2026-11-14T09:43:43.478679Z"},
          {"stationId":"CV001816","name":"강동명일동공영주차장","address":"서울특별시 강동구 구천면로 395",
           "lat":37.550854,"lng":127.1426773,"distanceMeters":254,"availableCount":0,"totalValidCount":1,
           "chargerTypes":["DC_COMBO"],"maxOutputKw":50.0,"visitCount":null,"statusObservedAt":"2026-11-14T09:43:43.478679Z"}],
         "page":0,"size":3,"hasNext":true,"fetchedAt":"2026-11-14T09:44:12.831819127Z"}
    """.trimIndent()

    // GET /stations/recommendation?lat=37.5500&lng=127.1400&chargeRecommended=true
    private val recommendationJson = """
        {"station":{"stationId":"KP000224","name":"명일동 공영주차장","distanceMeters":290,"availableCount":1,
          "visitCount":null,"speedCategory":"FAST","maxOutputKw":50.0},
         "reasonCode":"VISITS_THEN_DISTANCE","fallbackUsed":false,"statusObservedAt":"2026-11-14T09:43:43.478679Z"}
    """.trimIndent()

    // GET /stations/KP000224
    private val detailJson = """
        {"stationId":"KP000224","name":"명일동 공영주차장","address":"서울특별시 강동구 구천면로 395(명일동)",
         "lat":37.551194,"lng":127.142924,"availableCount":1,"totalValidCount":2,
         "chargers":[
          {"chargerId":"01","connectorType":"DC_CHADEMO+AC_3PHASE+DC_COMBO","connectorTypes":["DC_CHADEMO","AC_3PHASE","DC_COMBO"],
           "chgerType":"06","outputKw":50.0,"status":"CHARGING","stat":"3","statusUpdatedAt":"2026-11-14T04:30:05Z",
           "statusObservedAt":"2026-11-14T09:43:43.478679Z"},
          {"chargerId":"02","connectorType":"DC_CHADEMO+AC_3PHASE+DC_COMBO","connectorTypes":["DC_CHADEMO","AC_3PHASE","DC_COMBO"],
           "chgerType":"06","outputKw":50.0,"status":"AVAILABLE","stat":"2","statusUpdatedAt":"2026-11-14T05:20:23Z",
           "statusObservedAt":"2026-11-14T09:43:43.478679Z"}],
         "statusObservedAt":"2026-11-14T09:43:43.478679Z"}
    """.trimIndent()

    // GET /charging-events/latest?vehicleId=mock-vehicle-001 (아직 COMPLETE 이벤트가 없을 때 실제 응답)
    private val latestJson = """
        {"vehicleId":"mock-vehicle-001","lastChargeCompletedAt":null,"odometerAtLastChargeKm":null,
         "currentOdometerKm":null,"distanceSinceChargeKm":null,"source":null,"trusted":false}
    """.trimIndent()

    private val page = ServerJson.stationPage(listJson)
    private val now: Instant = page.fetchedAt.plusSeconds(60)

    private fun listState(
        sort: StationSort = StationSort.DISTANCE,
        filter: StationFilter = StationFilter(),
        counts: Map<String, Int> = emptyMap(),
        at: Instant = now,
    ) = buildStationList(
        all = page.items,
        searchResults = null,
        sort = sort,
        filter = filter,
        localCounts = counts,
        receivedAt = page.fetchedAt,
        now = at,
    )

    // 앱이 처음 열릴 때 쓰는 위치(서울시청 37.5665, 126.9780)의 실제 응답
    private val cityHallRecommendationJson = """
        {"station":{"stationId":"SE000014","name":"서울시 본관청사","distanceMeters":25,"availableCount":4,
          "visitCount":null,"speedCategory":"FAST","maxOutputKw":100.0},
         "reasonCode":"VISITS_THEN_DISTANCE","fallbackUsed":false,"statusObservedAt":"2026-11-14T09:43:43.478679Z"}
    """.trimIndent()

    private val cityHallDetailJson = """
        {"stationId":"SE000014","name":"서울시 본관청사","address":"서울특별시 중구 세종대로 110",
         "lat":37.5663174,"lng":126.9778292,"availableCount":4,"totalValidCount":4,
         "chargers":[
          {"chargerId":"05","connectorType":"DC_COMBO","connectorTypes":["DC_COMBO"],"chgerType":"04",
           "outputKw":100.0,"status":"AVAILABLE","stat":"2","statusUpdatedAt":"2026-11-14T05:03:41Z",
           "statusObservedAt":"2026-11-14T09:43:43.478679Z"}],
         "statusObservedAt":"2026-11-14T09:43:43.478679Z"}
    """.trimIndent()

    // ---- R2 목록 ----

    @Test
    fun 목록_응답이_거리순_카드_값으로_바뀐다() {
        val state = listState()

        assertEquals(listOf("NT101224", "PI801611", "CV001816"), state.items.map { it.stationId })
        val first = state.items.first()
        assertEquals("강동 암사e편한세상아파트", first.stationName)
        assertEquals("서울특별시 강동구 구천면로 365-13", first.chargeStationAddress)
        assertEquals(0.1, first.distance, 0.0001) // 142m
        assertEquals(1, first.availableChargerCount)
        assertEquals(3, first.totalChargerCount)
        assertEquals(listOf(ConnectorType.AC_SLOW), first.connectorTypes)
        assertEquals(ChargingType.AC, first.chargingType) // 7kW → 완속
        assertFalse(first.showVisitCount)
        assertFalse(first.availabilityUnknown)
        assertNull(state.message)
    }

    @Test
    fun 출력_50kW_충전소는_급속_아이콘으로() {
        val third = listState().items.last() // CV001816, 50kW
        assertEquals(ChargingType.DC, third.chargingType)
        assertEquals(0, third.availableChargerCount)
        assertEquals(1, third.totalChargerCount)
        assertEquals(0.3, third.distance, 0.0001) // 254m
    }

    @Test
    fun 기기에_센_방문이_있으면_방문순으로_바뀐다() {
        val state = listState(sort = StationSort.VISIT, counts = mapOf("CV001816" to 2))

        assertTrue(state.visitSortEnabled)
        assertEquals(StationSort.VISIT, state.sort)
        assertEquals("CV001816", state.items.first().stationId)
        assertTrue(state.items.first().showVisitCount)
        assertEquals(2, state.items.first().visitCount)
        assertEquals(0.3, state.items.first().distance, 0.0001) // 정렬만 바뀌고 값은 그대로
        assertEquals(0, state.items.last().visitCount)
    }

    @Test
    fun 방문_기록이_없으면_방문순을_고를_수_없다() {
        val state = listState(sort = StationSort.VISIT)
        assertFalse(state.visitSortEnabled)
        assertEquals(StationSort.DISTANCE, state.sort)
        assertFalse(state.items.any { it.showVisitCount })
    }

    @Test
    fun 연결타입_필터가_실제_목록에_걸린다() {
        val state = listState(filter = StationFilter(connectors = setOf(ConnectorFilter.DC_COMBO)))
        assertEquals(listOf("CV001816"), state.items.map { it.stationId })
    }

    @Test
    fun 출력_필터가_실제_목록에_걸린다() {
        val slow = listState(filter = StationFilter(power = PowerRange(3, 11)))
        assertEquals(listOf("NT101224", "PI801611"), slow.items.map { it.stationId })

        val fast = listState(filter = StationFilter(power = PowerRange(50, 200)))
        assertEquals(listOf("CV001816"), fast.items.map { it.stationId })
    }

    @Test
    fun 조건에_맞는_곳이_없으면_안내_문구를_남긴다() {
        val state = listState(filter = StationFilter(connectors = setOf(ConnectorFilter.NACS)))
        assertTrue(state.items.isEmpty())
        assertEquals(StationListMessage.FILTER_EMPTY, state.message)
    }

    @Test
    fun 받은_지_15분이_지나면_가용_대수를_정보_없음으로() {
        val fresh = listState(at = page.fetchedAt.plusSeconds(14 * 60))
        assertFalse(fresh.items.any { it.availabilityUnknown })

        val stale = listState(at = page.fetchedAt.plusSeconds(16 * 60))
        assertTrue(stale.items.all { it.availabilityUnknown })
    }

    @Test
    fun 처음_여는_위치의_실제_추천이_R1_카드로_그려진다() {
        val info = ServerJson.recommendation(cityHallRecommendationJson)
            .toRecommendedStationInfo(detail = ServerJson.stationDetail(cityHallDetailJson))
        val card = requireNotNull(info).toCardStationUiState(chipText = "추천", myChargingCount = 0)

        assertEquals("서울시 본관청사", card.stationName)
        assertEquals(0.1, card.distance, 0.0001) // 25m → 최소 표시 0.1km
        assertEquals(4, card.availableChargerCount)
        assertEquals(4, card.totalChargerCount)
        assertEquals("서울특별시 중구 세종대로 110", card.stationAddress)
        assertEquals(listOf("급속", "100kW"), card.chargerInfo.map { it.text })
        assertEquals(listOf(ConnectorType.DC_COMBO), card.connectorTypes)
        assertEquals(0, card.myChargingCount)
    }

    // ---- R1 충전기 수 칩 상태 (Figma availability) ----

    private fun r1(observedAt: java.time.Instant?, chargers: List<com.altercharge.app.data.remote.model.ChargerStatus> = emptyList()) =
        com.altercharge.app.data.home.RecommendedStationInfo(
            stationId = "ST1", name = "충전소", distanceMeters = 500, availableCount = 0, visitCount = null,
            speedCategory = null, maxOutputKw = 50.0, fallbackUsed = false,
            reasonCode = com.altercharge.app.data.remote.model.RecommendationReason.VISITS_THEN_DISTANCE,
            chargers = chargers.mapIndexed { i, st ->
                com.altercharge.app.data.remote.model.ChargerDetail("C$i", "04", emptyList(), "04", 50.0, st, "2", null, null)
            },
            statusObservedAt = observedAt,
        )

    private val nowR1 = java.time.Instant.parse("2026-11-14T12:00:00Z")

    @Test
    fun 빈_충전기_0곳도_관측이_15분_안이면_사용_가능_모양() {
        val card = r1(nowR1.minusSeconds(60)).toCardStationUiState("추천", 0, now = nowR1)
        assertEquals(com.altercharge.app.ui.card.station.ChargerStatus.AVAILABLE, card.chargerStatus)
    }

    @Test
    fun 관측이_15분_지나면_정보_없음() {
        val card = r1(nowR1.minusSeconds(16 * 60)).toCardStationUiState("추천", 0, now = nowR1)
        assertEquals(com.altercharge.app.ui.card.station.ChargerStatus.UNKNOWN, card.chargerStatus)
    }

    @Test
    fun 충전기가_모두_점검_정지면_점검_중() {
        val all = listOf(com.altercharge.app.data.remote.model.ChargerStatus.MAINTENANCE, com.altercharge.app.data.remote.model.ChargerStatus.STOPPED)
        val card = r1(nowR1, all).toCardStationUiState("추천", 0, now = nowR1)
        assertEquals(com.altercharge.app.ui.card.station.ChargerStatus.UNDER_INSPECTION, card.chargerStatus)
    }

    // ---- R1 추천 충전소 ----

    @Test
    fun 추천과_상세_응답이_R1_카드_값으로_바뀐다() {
        val info = ServerJson.recommendation(recommendationJson)
            .toRecommendedStationInfo(detail = ServerJson.stationDetail(detailJson))
        requireNotNull(info)

        assertEquals("KP000224", info.stationId)
        assertEquals(SpeedCategory.FAST, info.speedCategory)
        assertEquals(
            listOf(ConnectorType.DC_CHADEMO, ConnectorType.AC_3PHASE, ConnectorType.DC_COMBO),
            info.allConnectorTypes,
        )

        val card = info.toCardStationUiState(chipText = "추천", myChargingCount = 2)
        assertEquals("명일동 공영주차장", card.stationName)
        assertEquals("추천", card.chipState.text)
        assertEquals(0.3, card.distance, 0.0001) // 290m
        assertEquals(1, card.availableChargerCount)
        assertEquals(2, card.totalChargerCount) // 상세의 totalValidCount
        assertEquals("서울특별시 강동구 구천면로 395(명일동)", card.stationAddress)
        assertEquals(2, card.myChargingCount)
        assertEquals(listOf("급속", "50kW"), card.chargerInfo.map { it.text })
        assertEquals(3, card.connectorTypes.size)
    }

    @Test
    fun 상세를_못_받아도_추천은_보여_준다() {
        val info = ServerJson.recommendation(recommendationJson).toRecommendedStationInfo(detail = null)
        val card = requireNotNull(info).toCardStationUiState(chipText = "대체 추천", myChargingCount = 0)

        assertNull(card.stationAddress)
        assertTrue(card.connectorTypes.isEmpty())
        assertEquals(1, card.totalChargerCount) // 가용 수로 자리만 채운다
        assertEquals(listOf("급속", "50kW"), card.chargerInfo.map { it.text })
        assertEquals("대체 추천", card.chipState.text)
    }

    @Test
    fun 충전소가_없는_추천은_카드를_만들지_않는다() {
        val empty = ServerJson.recommendation(
            """{"station":null,"reasonCode":"NO_STATIONS_IN_RADIUS","fallbackUsed":false,"statusObservedAt":null}""",
        )
        assertNull(empty.toRecommendedStationInfo(detail = null))
    }

    // ---- C 차량 상태 ----

    private fun telemetry(charging: VehicleChargingState = VehicleChargingState.CHARGING) = VehicleTelemetry(
        socPercent = 32,
        rangeKm = 98,
        odometerKm = 12500.0,
        charging = charging,
        remainingMinutes = 45,
        fullAt = "오후 2:30",
    )

    @Test
    fun C_카드는_차량_값과_서버_값을_합친다() {
        val latest = ServerJson.latestChargeHistory(latestJson)
        val today = LocalDate.of(2026, 11, 14)
        val serverValues = VehicleStatusServerValues(
            chargeRecommended = true,
            nearestStationKm = 0.4,
            lastChargedAt = latest.lastChargedAtText(today),
        )

        val card = telemetry().toCardVehicleStatusUiState("성진", serverValues)

        assertEquals("성진", card.driverName)
        assertEquals(32, card.socPercent)
        assertEquals(98, card.rangeKm)
        assertEquals(45, card.remainingMinutes)
        assertEquals("오후 2:30", card.fullAt)
        assertEquals(0.4, card.nearestStationKm!!, 0.0001)
        assertNull(card.lastChargedAt) // 완료 이력이 없으면 null (실제 응답)
        assertEquals(VehicleSituation.CHARGING, card.situation)
    }

    @Test
    fun 서버가_충전을_권하면_미연결_카드는_충전_권장() {
        val card = telemetry(charging = VehicleChargingState.UNPLUGGED)
            .toCardVehicleStatusUiState("성진", VehicleStatusServerValues(true, 1.2, null))
        assertEquals(VehicleSituation.CHARGE_RECOMMENDED, card.situation)
    }

    @Test
    fun 충전완료_이력이_있으면_C_카드에_시각이_들어간다() {
        // OpenAPI 08 예시(문서 837줄) — 완료 이벤트가 쌓인 뒤의 응답 모양
        val latest = ServerJson.latestChargeHistory(
            """{"vehicleId":"mock-vehicle-001","lastChargeCompletedAt":"2026-11-11T22:30:00Z",
                "odometerAtLastChargeKm":1000.0,"currentOdometerKm":1040.0,"distanceSinceChargeKm":40.0,
                "source":"MOCK","trusted":false}""",
        )
        val card = telemetry(charging = VehicleChargingState.UNPLUGGED).toCardVehicleStatusUiState(
            "성진",
            VehicleStatusServerValues(false, null, latest.lastChargedAtText(LocalDate.of(2026, 11, 12))),
        )
        assertEquals("07:30", card.lastChargedAt)
        assertEquals(VehicleSituation.READY, card.situation)
    }

    @Test
    fun 차량_값이_없으면_정보_없음_상황() {
        val card = VehicleTelemetry(
            socPercent = null,
            rangeKm = null,
            odometerKm = null,
            charging = VehicleChargingState.UNPLUGGED,
        ).toCardVehicleStatusUiState("성진", null)
        assertNull(card.socPercent)
        assertEquals(VehicleSituation.NO_DATA, card.situation)
        assertFalse(card.chargeRecommended)
    }
}
