/**
 * 추천 충전소 · 마지막 충전 이력 응답을 L1 · C 카드가 받는 값으로 바꾼다.
 * R1 · R2(충전소 카드 · 목록) 변환은 담당자(석기님) 쪽에서 한다.
 */
package com.altercharge.app.data.remote.mapper

import com.altercharge.app.data.remote.model.LatestChargeHistory
import com.altercharge.app.data.remote.model.Recommendation
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/** L1 추천 충전소 줄 이름. 반경 안에 충전소가 없으면 빈 문자열이고, L1은 빈 값이면 줄을 숨긴다 */
fun Recommendation.placeName(): String = station?.name.orEmpty()

/** C 카드 "가까운 충전소" 거리 km, 소수 첫째 자리 (1234m → 1.2). 충전소가 없으면 null ("--") */
fun Recommendation.nearestStationKm(): Double? = station?.let { (it.distanceMeters / 100.0).roundToInt() / 10.0 }

/**
 * C 카드 "마지막 충전" 시각 글자. 오늘이면 "07:30", 다른 날이면 "11.11" (디자이너 확정 2026-11-12 날짜 형식).
 * 완료 이벤트가 없으면 null ("--").
 */
fun LatestChargeHistory.lastChargedAtText(today: LocalDate, zone: ZoneId = KoreaZone): String? {
    val at = lastChargeCompletedAt?.atZone(zone) ?: return null
    val pattern = if (at.toLocalDate() == today) "HH:mm" else "M.d"
    return at.format(DateTimeFormatter.ofPattern(pattern))
}
