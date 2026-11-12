/** L1 오프라인 "N분 전 기준" (REQ_CM_006). Figma CM_006 핸드오프 798:56855 "② 마지막 확인 시각 형식" */
package com.altercharge.app.ui.card.info

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import com.altercharge.app.R
import com.altercharge.app.domain.time.KoreaZone
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** 기준은 "마지막으로 충전소 정보를 받은 시각"이다 (Figma 확정 2026-11-12) */
@Immutable
sealed interface LastChecked {
    data object JustNow : LastChecked

    data class MinutesAgo(val minutes: Int) : LastChecked

    data class HoursAgo(val hours: Int) : LastChecked

    data class OnDate(val month: Int, val day: Int) : LastChecked
}

/**
 * 기기 시계가 어긋나 받은 시각이 미래면 "방금"으로 본다.
 * 기본 zone이 한국시간이라 기기 시간대가 UTC여도 같은 날짜가 나온다.
 */
fun lastCheckedOf(checkedAt: Instant, now: Instant, zone: ZoneId = KoreaZone): LastChecked {
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
