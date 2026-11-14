/**
 * 차에서 읽는 값 (REQ_VS_001 ~ REQ_VS_004). 화면은 이 값으로 C 카드를 만든다.
 *
 * 지금은 Pleos Vehicle SDK 대신 목업을 쓴다: MVP 시연은 목업 데이터를 쓰기로 정했다 (REQ_VS_002 · REQ_VS_003).
 * SDK로 바꿀 때는 [VehicleTelemetrySource] 구현만 갈아 끼우면 되고, 화면 · 판정 코드는 그대로 둔다.
 */
package com.altercharge.app.data.vehicle

import java.time.format.FormatStyle
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import com.altercharge.app.data.home.VehicleReading
import com.altercharge.app.ui.card.vehiclestatus.ChargeFault
import com.altercharge.app.ui.card.vehiclestatus.ChargingState
import java.time.Instant

/** @property observedAt 이 값을 차에서 읽은 시각. 모르면 null */
data class VehicleTelemetry(
    val socPercent: Int?,
    val rangeKm: Int?,
    val odometerKm: Double?,
    val charging: ChargingState,
    /** 충전 중이면 남은 시간(분) */
    val remainingMinutes: Int? = null,
    /** 충전 중 · 예약이면 완충 예정 시각, 완료면 완료 시각 ("오후 2:30") */
    val fullAt: String? = null,
    val fault: ChargeFault? = null,
    val observedAt: Instant? = null,
) {
    /** 판정 · 기록 전송에 쓰는 값은 세 가지가 모두 있어야 한다. 없으면 판정하지 않는다 */
    fun readingOrNull(): VehicleReading? {
        val soc = socPercent ?: return null
        val range = rangeKm ?: return null
        val odometer = odometerKm ?: return null
        return VehicleReading(socPercent = soc, rangeKm = range, odometerKm = odometer)
    }
}

/**
 * 충전이 시작된 순간인지 (REQ_ST_008).
 * 앱을 켰을 때 이미 충전 중이면(직전 값을 모름) 그 충전도 새 방문으로 본다:
 * 같은 충전소 하루 1회 규칙이 중복을 막고, 차량 SDK가 상태 변화를 주기 전까지는 이 판단이 필요하다.
 */
fun isChargeStart(previous: ChargingState?, current: ChargingState): Boolean =
    current == ChargingState.CHARGING && previous != ChargingState.CHARGING

/** 테스트와 목업이 갈아 끼우는 자리 */
fun interface VehicleTelemetrySource {
    fun read(): VehicleTelemetry

    /** 차에서 읽은 값이 아니다(목업). 방문 기록 · 충전 이벤트처럼 남는 기록은 만들지 않는다 */
    val isSimulated: Boolean get() = false
}

/** Pleos Vehicle SDK 연동 전까지 쓰는 목업 값. 시안의 '충전 중' 화면과 같은 숫자다 */
class MockVehicleTelemetrySource(
    private val telemetry: VehicleTelemetry = DEFAULT,
) : VehicleTelemetrySource {
    override fun read(): VehicleTelemetry = telemetry

    // 목업은 늘 충전 중이라 앱을 켤 때마다 방문 +1 · 공용 시연 차량에 START 이벤트가 쌓인다
    override val isSimulated: Boolean get() = true

    companion object {
        val DEFAULT: VehicleTelemetry = VehicleTelemetry(
            socPercent = 32,
            rangeKm = 98,
            odometerKm = 12500.0,
            charging = ChargingState.CHARGING,
            remainingMinutes = 45,
            // 화면 언어에 맞춘 시각 (KO "오후 2:30", EN "2:30 PM")
            fullAt = LocalTime.of(14, 30).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
        )
    }
}
