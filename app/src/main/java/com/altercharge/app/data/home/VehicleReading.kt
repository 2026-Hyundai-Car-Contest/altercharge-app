/** 차량 값과 좌표. 차량 값은 Pleos Vehicle SDK에서 읽고([com.altercharge.app.data.vehicle.VehicleTelemetry]), 좌표는 아직 고정값이다 */
package com.altercharge.app.data.home

/** 차량 SDK 값. 거리는 km */
data class VehicleReading(val socPercent: Int, val rangeKm: Int, val odometerKm: Double)

data class LatLng(val lat: Double, val lng: Double)
