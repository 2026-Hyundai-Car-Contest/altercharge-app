/** L1 오프라인 "N분 전 기준" 문구 (REQ_CM_006). 값은 LastChecked.kt */
package com.altercharge.app.ui.card.info

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.altercharge.app.R

@Composable
internal fun lastCheckedText(lastChecked: LastChecked): String = when (lastChecked) {
    LastChecked.JustNow -> stringResource(R.string.card_info_last_checked_just_now)
    is LastChecked.MinutesAgo -> stringResource(R.string.card_info_last_checked_minutes, lastChecked.minutes)
    is LastChecked.HoursAgo -> stringResource(R.string.card_info_last_checked_hours, lastChecked.hours)
    is LastChecked.OnDate -> stringResource(R.string.card_info_last_checked_date, lastChecked.month, lastChecked.day)
}
