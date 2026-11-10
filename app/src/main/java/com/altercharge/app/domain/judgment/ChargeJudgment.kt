package com.altercharge.app.domain.judgment

import kotlin.math.floor

/**
 * 충전 판정에 넣는 값. data 쪽이 차량 데이터와 저장된 주행 기록으로 채운다.
 *
 * @property rangeKm 차량 주행가능거리(km)
 * @property socPercent 배터리 잔량(%)
 * @property recentDailyKm 최근 일별 주행거리(km). 오래된 날 → 오늘 순, 운행하지 않은 날은 0.
 *   기록이 있는 날만 담는다. 14일보다 많으면 마지막 14일만 쓴다.
 */
data class JudgmentInput(
    val rangeKm: Int,
    val socPercent: Int,
    val recentDailyKm: List<Int>,
)

/**
 * 판정 결과. 화면 쪽이 L1·C 카드 값으로 바꿔 쓴다.
 * 판정 문구(오늘 / 내일 / 3일내)는 화면 쪽에서 [daysLeft]로 고른다.
 *
 * @property averageDailyKm 일 평균 주행거리(km, 반올림). 표시용이고, 버틸 일수 계산은 반올림 전 값으로 한다.
 * @property reserveKm 예비 거리(km)
 * @property daysLeft 충전 없이 버틸 수 있는 일수. 0 이상, 최대 [MAX_DAYS_LEFT]
 * @property chargeRecommended 지금 충전을 권장하는지
 */
data class Judgment(
    val averageDailyKm: Int,
    val reserveKm: Int,
    val daysLeft: Int,
    val chargeRecommended: Boolean,
)

/** 기록이 이 일수보다 적으면 기본값을 쓴다 (REQ_JD_002 예외) */
const val MIN_RECORD_DAYS = 7

/** 일 평균과 예비 거리를 계산하는 기간 (REQ_JD_002) */
const val WINDOW_DAYS = 14

/** 기록이 부족할 때 쓰는 일 평균 · 예비 거리(km) (REQ_JD_002 예외) */
const val DEFAULT_DAILY_KM = 40

/** 예비 거리 최솟값(km) (REQ_JD_002) */
const val MIN_RESERVE_KM = 30

/** 이 일수 이하로 버티면 충전 권장 (REQ_ST_005) */
const val RECOMMEND_DAYS_LEFT = 1

/** 이 잔량(%) 이하이면 충전 권장 (REQ_ST_005) */
const val RECOMMEND_SOC_PERCENT = 30

/** 2주 동안 운행하지 않아 일 평균이 0일 때 버틸 일수 상한 */
const val MAX_DAYS_LEFT = 99

/**
 * 충전 판정. 기능명세서 REQ_JD_001 · REQ_JD_002 · REQ_ST_005.
 *
 * 숫자만 받아 결과를 돌려주는 순수 함수다. 저장 · 차량 SDK · 현재 시각을 모르고,
 * 언제 다시 계산할지(REQ_JD_004)는 부르는 쪽이 정한다.
 *
 * - 일 평균 = 최근 14일 주행거리 합 ÷ 14 (운행하지 않은 날 포함)
 * - 예비 거리 = 최근 14일 중 운행한 날 주행거리의 중앙값, 최소 30km
 * - 기록이 7일 미만이면 일 평균 40km, 예비 거리 40km
 * - 버틸 일수 = (주행가능거리 − 예비 거리) ÷ 일 평균, 소수점 버림, 0 미만은 0
 * - 충전 권장 = 버틸 일수 1 이하 또는 배터리 30% 이하
 */
fun judge(input: JudgmentInput): Judgment {
    val records = input.recentDailyKm.takeLast(WINDOW_DAYS).map { it.coerceAtLeast(0) }
    val enoughRecords = records.size >= MIN_RECORD_DAYS

    val averageDailyKm: Double =
        if (enoughRecords) records.sum().toDouble() / WINDOW_DAYS else DEFAULT_DAILY_KM.toDouble()
    val reserveKm: Int =
        if (enoughRecords) reserveFrom(records) else DEFAULT_DAILY_KM

    val drivableKm = (input.rangeKm.coerceAtLeast(0) - reserveKm).coerceAtLeast(0)
    val daysLeft = when {
        drivableKm == 0 -> 0
        averageDailyKm <= 0.0 -> MAX_DAYS_LEFT
        else -> floor(drivableKm / averageDailyKm).toInt().coerceIn(0, MAX_DAYS_LEFT)
    }

    val chargeRecommended =
        daysLeft <= RECOMMEND_DAYS_LEFT || input.socPercent <= RECOMMEND_SOC_PERCENT

    return Judgment(
        averageDailyKm = Math.round(averageDailyKm).toInt(),
        reserveKm = reserveKm,
        daysLeft = daysLeft,
        chargeRecommended = chargeRecommended,
    )
}

/** 운행한 날(0 초과) 주행거리의 중앙값, 최소 [MIN_RESERVE_KM]. 짝수 개면 가운데 두 값의 평균(반올림). */
private fun reserveFrom(records: List<Int>): Int {
    val driven = records.filter { it > 0 }.sorted()
    if (driven.isEmpty()) return MIN_RESERVE_KM
    val mid = driven.size / 2
    val median = if (driven.size % 2 == 1) {
        driven[mid].toDouble()
    } else {
        (driven[mid - 1] + driven[mid]) / 2.0
    }
    return Math.round(median).toInt().coerceAtLeast(MIN_RESERVE_KM)
}
