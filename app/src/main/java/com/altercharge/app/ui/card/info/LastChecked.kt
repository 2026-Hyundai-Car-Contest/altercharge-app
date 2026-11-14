/** 마지막 확인 시각 표시 값 (REQ_CM_006). 문구는 카드가 리소스로 만든다(CardInfoLastChecked.kt) */
package com.altercharge.app.ui.card.info

import androidx.compose.runtime.Immutable
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
