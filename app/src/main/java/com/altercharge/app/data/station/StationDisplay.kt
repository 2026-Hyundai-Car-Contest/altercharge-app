/**
 * 목록 · 추천 카드에 쓰는 표시 값 (REQ_SL_001 · REQ_SL_004).
 * 서버가 주는 거리는 m, 방문 횟수는 없을 수 있어 화면에 쓸 형태로 바꾼다.
 */
package com.altercharge.app.data.station

import com.altercharge.app.data.remote.model.SpeedCategory
import kotlin.math.roundToInt

/** 바로 옆인데 "0.0km"로 보이면 어색해서 최소 0.1km. 소수 한 자리로 반올림한다 */
fun displayKm(meters: Int): Double = ((meters / 100.0).roundToInt() / 10.0).coerceAtLeast(MIN_DISPLAY_KM)

private const val MIN_DISPLAY_KM = 0.1

/** 50kW 미만 SLOW, 50~200 FAST, 200 이상 ULTRA_FAST (OpenAPI 공통 규칙). 출력을 모르면 null */
fun speedCategoryOf(outputKw: Double?): SpeedCategory? = when {
    outputKw == null -> null
    outputKw < SLOW_MAX_KW -> SpeedCategory.SLOW
    outputKw < ULTRA_FAST_MIN_KW -> SpeedCategory.FAST
    else -> SpeedCategory.ULTRA_FAST
}

private const val SLOW_MAX_KW = 50.0
private const val ULTRA_FAST_MIN_KW = 200.0
