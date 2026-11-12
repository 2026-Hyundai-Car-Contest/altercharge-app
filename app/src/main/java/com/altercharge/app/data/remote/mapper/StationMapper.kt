/** 충전소 · 마지막 충전 응답 → L1 · C 카드 값. R1 · R2 변환은 석기님 쪽 */
package com.altercharge.app.data.remote.mapper

import com.altercharge.app.data.remote.model.LatestChargeHistory
import com.altercharge.app.data.remote.model.Recommendation
import com.altercharge.app.data.remote.model.StationPage
import com.altercharge.app.domain.time.KoreaZone
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/** 빈 문자열이면 L1이 장소 줄을 숨긴다 */
fun Recommendation.placeName(): String = station?.name.orEmpty()

/** 추천 충전소는 방문 횟수를 먼저 봐서 가장 가깝지 않을 수 있어, 거리순 목록에서 구한다 (REQ_VS_006) */
fun StationPage.nearestStationKm(): Double? = items.minByOrNull { it.distanceMeters }?.let { metersToKm(it.distanceMeters) }

/** 바로 옆인데 "0.0km"로 보이면 어색해서 최소 0.1km */
private fun metersToKm(meters: Int): Double = ((meters / 100.0).roundToInt() / 10.0).coerceAtLeast(MIN_DISPLAY_KM)

private const val MIN_DISPLAY_KM = 0.1

/** 오늘이면 "07:30", 다른 날이면 "11.11" */
fun LatestChargeHistory.lastChargedAtText(today: LocalDate, zone: ZoneId = KoreaZone): String? {
    val at = lastChargeCompletedAt?.atZone(zone) ?: return null
    val pattern = if (at.toLocalDate() == today) "HH:mm" else "M.d"
    return at.format(DateTimeFormatter.ofPattern(pattern))
}
