package com.altercharge.app.ui.card.driverecord

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.altercharge.app.ui.designsystem.component.PlaceholderBox
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import java.time.LocalDate

/** L2 주간 주행 기록 카드. 담당: 서희님 */
@Composable
fun CardDriveRecord(
    state: CardDriveRecordUiState,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO 서희님: Figma card_drive_record (366:38474) 구현
    PlaceholderBox("L2 card_drive_record · ${state.weekStart} · 평균 ${state.averageKm}km", modifier)
}

@Preview(widthDp = 383, heightDp = 648)
@Composable
private fun CardDriveRecordPreview() {
    AlterChargeTheme {
        CardDriveRecord(
            state = CardDriveRecordUiState(
                weekStart = LocalDate.of(2026, 11, 2),
                dailyKm = listOf(35, 30, 52, 25, 15, null, null),
                todayIndex = 4,
                averageKm = 30,
                totalKm = 157,
                daysSinceLastCharge = 3,
                kmSinceLastCharge = 120,
                diffFromLastWeekKm = 4,
                hasPreviousWeek = true,
                hasNextWeek = false,
                expanded = false,
            ),
            onPreviousWeek = {},
            onNextWeek = {},
            onToggleExpanded = {},
        )
    }
}
