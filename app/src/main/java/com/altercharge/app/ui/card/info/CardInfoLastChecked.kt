/**
 * L1 추천 충전소 줄 아래 "N분 전 기준" (오프라인 · 받아 둔 충전소 정보 있음, REQ_CM_006).
 * 받은 시각과 지금 시각으로 표시 단계를 고르는 함수(lastCheckedOf)와 단계별 문구(lastCheckedText)를 둔다.
 * Figma: CM_006 핸드오프 798:56855 "② 마지막 확인 시각 형식"의 L1 · card_station(짧게) 열.
 */
package com.altercharge.app.ui.card.info

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import com.altercharge.app.R
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * 충전소 정보를 마지막으로 받은 뒤 지난 시간을 표시 단계로 나눈 값.
 * 기준값은 "마지막으로 충전소 정보를 받은 시각"이다 (Figma 확정 2026-11-12).
 */
@Immutable
sealed interface LastChecked {
    /** 1분 미만 → "방금 기준" */
    data object JustNow : LastChecked

    /** 1~59분 → "N분 전 기준" */
    data class MinutesAgo(val minutes: Int) : LastChecked

    /** 1~23시간 → "N시간 전 기준" */
    data class HoursAgo(val hours: Int) : LastChecked

    /** 24시간 이상 → "11.11 기준" (받은 날짜) */
    data class OnDate(val month: Int, val day: Int) : LastChecked
}

/**
 * 받은 시각([checkedAt])과 지금([now])으로 표시 단계를 고른다.
 * 기기 시계가 어긋나 받은 시각이 미래면 "방금"으로 본다.
 * 날짜는 [zone] 기준이다 (테스트에서 고정하려고 인자로 받는다).
 */
fun lastCheckedOf(checkedAt: Instant, now: Instant, zone: ZoneId = ZoneId.systemDefault()): LastChecked {
    val elapsed = Duration.between(checkedAt, now)
    return when {
        elapsed.toMinutes() < 1 -> LastChecked.JustNow
        elapsed.toHours() < 1 -> LastChecked.MinutesAgo(elapsed.toMinutes().toInt())
        elapsed.toHours() < 24 -> LastChecked.HoursAgo(elapsed.toHours().toInt())
        else -> checkedAt.atZone(zone).let { LastChecked.OnDate(it.monthValue, it.dayOfMonth) }
    }
}

@Composable
internal fun lastCheckedText(lastChecked: LastChecked): String = when (lastChecked) {
    LastChecked.JustNow -> stringResource(R.string.card_info_last_checked_just_now)
    is LastChecked.MinutesAgo -> stringResource(R.string.card_info_last_checked_minutes, lastChecked.minutes)
    is LastChecked.HoursAgo -> stringResource(R.string.card_info_last_checked_hours, lastChecked.hours)
    is LastChecked.OnDate -> stringResource(R.string.card_info_last_checked_date, lastChecked.month, lastChecked.day)
}
