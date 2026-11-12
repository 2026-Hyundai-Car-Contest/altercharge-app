/** L2 기간 헤더 */
package com.altercharge.app.ui.card.driverecord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import java.time.format.DateTimeFormatter

@Composable
internal fun PeriodHeader(
    state: CardDriveRecordUiState,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val cardColors = driveRecordColors()
    val locale = LocalConfiguration.current.locales[0]
    val pattern = stringResource(R.string.card_drive_record_period_date_pattern)
    val formatter = remember(pattern, locale) { DateTimeFormatter.ofPattern(pattern, locale) }
    val range = stringResource(
        R.string.card_drive_record_period_range,
        state.weekStart.format(formatter),
        state.weekStart.plusDays((DAYS_IN_WEEK - 1).toLong()).format(formatter),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            // TODO 디자이너 확인: 지난 주를 볼 때 기간 라벨("지난 주" 등) 시안이 없다. 지금은 날짜만 보인다
            if (state.todayIndex != null) {
                Text(
                    text = stringResource(R.string.card_drive_record_period_this_week),
                    style = typography.labelSmall,
                    color = colors.textTertiary,
                    maxLines = 1,
                )
            }
            Text(
                text = range,
                style = typography.bodySmall,
                color = cardColors.periodRange,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // 두 버튼의 터치 영역은 원 사이 간격 가운데에서 맞닿고 겹치지 않는다
        val halfGap = PeriodNavGap / 2
        Row(Modifier.bleedTouchInset()) {
            CircleIconButton(
                contentDescription = stringResource(R.string.card_drive_record_previous_week),
                iconRotation = 180f,
                enabled = state.hasPreviousWeek && !state.empty,
                onClick = onPreviousWeek,
                touchPadding = PaddingValues(
                    start = CircleButtonTouchInset,
                    top = CircleButtonTouchInset,
                    end = halfGap,
                    bottom = CircleButtonTouchInset,
                ),
            )
            CircleIconButton(
                contentDescription = stringResource(R.string.card_drive_record_next_week),
                iconRotation = 0f,
                enabled = state.hasNextWeek && !state.empty,
                onClick = onNextWeek,
                touchPadding = PaddingValues(
                    start = halfGap,
                    top = CircleButtonTouchInset,
                    end = CircleButtonTouchInset,
                    bottom = CircleButtonTouchInset,
                ),
            )
        }
    }
}
