/** L2 그래프 아래 summary_block과 펼침 상세 통계(details) */
package com.altercharge.app.ui.card.driverecord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
internal fun ContentBlock(state: CardDriveRecordUiState, onToggleExpanded: () -> Unit) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AlterPadding.Normal, end = AlterPadding.Normal, bottom = AlterPadding.Normal),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Medium),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AlterMargin.Small),
            ) {
                // TODO 디자이너 확인: 지난 주를 볼 때 제목 시안이 없다. 지금은 "이번 주 주행 기록" 그대로
                Text(
                    text = stringResource(R.string.card_drive_record_title),
                    style = typography.titleSmall,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                // Figma state = empty
                if (!state.empty) {
                    CircleIconButton(
                        contentDescription = stringResource(
                            if (state.expanded) R.string.card_drive_record_collapse else R.string.card_drive_record_expand,
                        ),
                        iconRotation = if (state.expanded) -90f else 90f,
                        enabled = true,
                        onClick = onToggleExpanded,
                        iconTint = colors.textSecondary,
                        modifier = Modifier.bleedTouchInset(),
                    )
                }
            }
            if (state.empty) {
                Text(
                    text = stringResource(R.string.card_drive_record_empty_description),
                    style = typography.labelSmall,
                    color = colors.textTertiary,
                )
            } else {
                Column {
                    Text(
                        text = stringResource(R.string.card_drive_record_summary_average, state.averageKm),
                        style = typography.labelSmall,
                        color = colors.textTertiary,
                    )
                    // TODO 디자이너 확인: 지난주보다 덜 달렸을 때 · 같을 때 · 비교할 주가 없을 때 문구가 시안에 없다.
                    // 지금은 더 달렸을 때만 두 번째 줄을 보여준다.
                    val diff = state.diffFromLastWeekKm
                    if (diff != null && diff > 0) {
                        Text(
                            text = stringResource(R.string.card_drive_record_summary_diff_more, diff),
                            style = typography.labelSmall,
                            color = colors.textTertiary,
                        )
                    }
                }
            }
        }
        if (state.expanded && !state.empty) {
            StatTiles(state)
        }
    }
}

@Composable
private fun StatTiles(state: CardDriveRecordUiState) {
    val unknown = stringResource(R.string.card_drive_record_value_unknown)
    val km = stringResource(R.string.card_drive_record_unit_km)
    Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.Normal)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
            TileStat(
                label = stringResource(R.string.card_drive_record_tile_daily_average),
                value = state.averageKm.toString(),
                unit = km,
                modifier = Modifier.weight(1f),
            )
            TileStat(
                label = stringResource(R.string.card_drive_record_tile_weekly_total),
                value = state.totalKm.toString(),
                unit = km,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
            val days = state.daysSinceLastCharge
            TileStat(
                label = stringResource(R.string.card_drive_record_tile_last_charge),
                value = days?.toString() ?: unknown,
                unit = if (days != null) {
                    pluralStringResource(R.plurals.card_drive_record_unit_days_ago, days)
                } else {
                    null
                },
                // Figma 펼침 인스턴스는 마지막 충전도 emphasis=false(28, 타일 높이 97)다
                modifier = Modifier.weight(1f),
            )
            val sinceCharge = state.kmSinceLastCharge
            TileStat(
                label = stringResource(R.string.card_drive_record_tile_since_charge),
                value = sinceCharge?.toString() ?: unknown,
                unit = if (sinceCharge != null) km else null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** tile_stat. emphasis는 값만 한 단계 크게 */
@Composable
private fun TileStat(
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    emphasis: Boolean = false,
) {
    val colors = AlterTheme.colors
    val typography = AlterTheme.typography
    val divider = colors.surfaceAccent
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .drawBehind {
                drawRect(divider, size = Size(size.width, TileDividerThickness.toPx()))
            }
            // Figma는 위 구분선 1이 자리를 차지한다(→ 타일 97)
            .padding(top = TileDividerThickness + AlterPadding.Normal),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall),
    ) {
        Text(text = label, style = typography.labelSmall, color = colors.textTertiary, maxLines = 1)
        Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.XXSmall)) {
            Text(
                text = value,
                style = if (emphasis) typography.numericMedium else typography.numericSmall,
                color = colors.textPrimary,
                maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
            if (unit != null) {
                Text(
                    text = unit,
                    style = typography.labelSmall,
                    color = colors.textTertiary,
                    maxLines = 1,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
    }
}
