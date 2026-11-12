package com.altercharge.app.domain.judgment

/**
 * 충전 판정에 넣는 값. data 쪽이 차량 데이터와 저장된 주행 기록으로 채운다.
 *
 * @property rangeKm 차량 주행가능거리(km)
 * @property socPercent 배터리 잔량(%)
 * @property recentDailyKm 최근 일별 주행거리(km). 오래된 날 → 오늘 순, 운행하지 않은 날은 0.
 *   기록이 있는 날만 담는다(앱을 쓴 지 10일이면 10개). 14일보다 많으면 마지막 14일만 쓴다.
 *   14개 미만이면 "기록 수집 중"이라 일 평균 · 버틸 일수를 계산하지 않는다 (REQ_JD_002 예외).
 */
data class JudgmentInput(
    val rangeKm: Int,
    val socPercent: Int,
    val recentDailyKm: List<Int>,
)

/**
 * 기록이 14일 이상 쌓였을 때의 판정. 화면 쪽이 L1·C 카드 값으로 바꿔 쓴다.
 * 판정 문구(오늘 / 내일 / N일내)는 화면 쪽에서 [daysLeft]로 고른다.
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

/** [judge]의 결과. 기록이 14일 미만이면 [Collecting], 이상이면 [Ready]. */
sealed interface JudgmentResult {
    /** 지금 충전을 권장하는지. 수집 중에도 배터리 잔량만으로 판정한다 (REQ_ST_005 예외). */
    val chargeRecommended: Boolean

    /**
     * 기록 수집 중 (REQ_JD_002 예외). 다음 충전일(버틸 일수)은 없고, 주행거리 · 배터리 상태 · 충전소 안내만 보여 준다.
     * @property recordedDays 지금까지 쌓인 기록 일수 (0~13). L1 진행 바 "N/14"에 쓴다.
     */
    data class Collecting(val recordedDays: Int, override val chargeRecommended: Boolean) : JudgmentResult

    data class Ready(val judgment: Judgment) : JudgmentResult {
        override val chargeRecommended: Boolean get() = judgment.chargeRecommended
    }
}

/** 일 평균과 예비 거리를 계산하는 기간. 기록이 이 일수보다 적으면 수집 중이다 (REQ_JD_002) */
const val WINDOW_DAYS = 14

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
 * - 기록이 14일 미만이면 [JudgmentResult.Collecting]. 충전 권장은 배터리 30% 이하로만 판정한다.
 * - 일 평균 = 최근 14일 주행거리 합 ÷ 14 (운행하지 않은 날 포함)
 * - 예비 거리 = 최근 14일 중 운행한 날 주행거리의 중앙값, 최소 30km
 * - 버틸 일수 = (주행가능거리 − 예비 거리) ÷ 일 평균, 소수점 버림, 0 미만은 0
 * - 충전 권장 = 버틸 일수 1 이하 또는 배터리 30% 이하
 */
fun judge(input: JudgmentInput): JudgmentResult {
    val records = input.recentDailyKm.takeLast(WINDOW_DAYS).map { it.coerceAtLeast(0) }
    val lowBattery = input.socPercent <= RECOMMEND_SOC_PERCENT
    if (records.size < WINDOW_DAYS) {
        return JudgmentResult.Collecting(recordedDays = records.size, chargeRecommended = lowBattery)
    }

    val totalKm = records.sum()
    val averageDailyKm = totalKm.toDouble() / WINDOW_DAYS
    val reserveKm = reserveFrom(records)

    val drivableKm = (input.rangeKm.coerceAtLeast(0) - reserveKm).coerceAtLeast(0)
    // 소수로 나누면 딱 떨어지는 경계에서 하루 모자라게 나올 수 있어(예: 9 ÷ (9/14) = 13.99…) 정수로 계산한다.
    // 거리 ÷ (합 ÷ 14) = 거리 × 14 ÷ 합
    val daysLeft = when {
        drivableKm == 0 -> 0
        totalKm == 0 -> MAX_DAYS_LEFT
        else -> (drivableKm.toLong() * WINDOW_DAYS / totalKm).toInt()
    }.coerceIn(0, MAX_DAYS_LEFT)

    return JudgmentResult.Ready(
        Judgment(
            averageDailyKm = Math.round(averageDailyKm).toInt(),
            reserveKm = reserveKm,
            daysLeft = daysLeft,
            chargeRecommended = daysLeft <= RECOMMEND_DAYS_LEFT || lowBattery,
        ),
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
