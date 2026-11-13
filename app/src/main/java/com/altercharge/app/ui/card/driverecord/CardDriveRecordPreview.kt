package com.altercharge.app.ui.card.driverecord

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
private fun PreviewCard(
    state: CardDriveRecordUiState,
    mode: CardDriveRecordMode,
    darkTheme: Boolean,
) {
    AlterChargeTheme(darkTheme = darkTheme) {
        // 미리보기 인터랙티브 모드에서 펼침 버튼을 눌러볼 수 있게 상태를 둔다
        var expanded by remember { mutableStateOf(state.expanded) }
        Box(
            Modifier
                .background(AlterTheme.colors.background)
                .padding(AlterPadding.XSmall),
        ) {
            CardDriveRecord(
                state = state.copy(expanded = expanded),
                onPreviousWeek = {},
                onNextWeek = {},
                onToggleExpanded = { expanded = !expanded },
                mode = mode,
            )
        }
    }
}

@Preview(name = "가로 · 접힘 · 라이트", widthDp = 399)
@Composable
private fun LandscapeCollapsedLightPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek, CardDriveRecordMode.LANDSCAPE, darkTheme = false)

@Preview(name = "가로 · 접힘 · 다크", widthDp = 399)
@Composable
private fun LandscapeCollapsedDarkPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek, CardDriveRecordMode.LANDSCAPE, darkTheme = true)

@Preview(name = "가로 · 펼침 · 라이트", widthDp = 399)
@Composable
private fun LandscapeExpandedLightPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek.copy(expanded = true), CardDriveRecordMode.LANDSCAPE, darkTheme = false)

@Preview(name = "가로 · 펼침 · 다크", widthDp = 399)
@Composable
private fun LandscapeExpandedDarkPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek.copy(expanded = true), CardDriveRecordMode.LANDSCAPE, darkTheme = true)

@Preview(name = "세로 · 접힘 · 라이트", widthDp = 394)
@Composable
private fun PortraitCollapsedLightPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek, CardDriveRecordMode.PORTRAIT, darkTheme = false)

@Preview(name = "세로 · 접힘 · 다크", widthDp = 394)
@Composable
private fun PortraitCollapsedDarkPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek, CardDriveRecordMode.PORTRAIT, darkTheme = true)

@Preview(name = "세로 · 펼침 · 라이트", widthDp = 394)
@Composable
private fun PortraitExpandedLightPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek.copy(expanded = true), CardDriveRecordMode.PORTRAIT, darkTheme = false)

@Preview(name = "세로 · 펼침 · 다크", widthDp = 394)
@Composable
private fun PortraitExpandedDarkPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek.copy(expanded = true), CardDriveRecordMode.PORTRAIT, darkTheme = true)

@Preview(name = "지난 주 · 가로 · 라이트", widthDp = 399)
@Composable
private fun LastWeekLandscapeLightPreview() =
    PreviewCard(CardDriveRecordSampleData.LastWeek, CardDriveRecordMode.LANDSCAPE, darkTheme = false)

@Preview(name = "지난 주 · 세로 · 펼침 · 다크", widthDp = 394)
@Composable
private fun LastWeekPortraitExpandedDarkPreview() =
    PreviewCard(CardDriveRecordSampleData.LastWeek.copy(expanded = true), CardDriveRecordMode.PORTRAIT, darkTheme = true)

@Preview(name = "영문 · 세로 · 펼침 · 라이트", widthDp = 394, locale = "en")
@Composable
private fun EnglishPortraitExpandedPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek.copy(expanded = true), CardDriveRecordMode.PORTRAIT, darkTheme = false)

@Preview(name = "W-Ext 가로 · 접힘 · 라이트", widthDp = 391)
@Composable
private fun LandscapeTallPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek, CardDriveRecordMode.LANDSCAPE_TALL, darkTheme = false)

@Preview(name = "WF 가로 · 접힘 · 다크", widthDp = 403)
@Composable
private fun WfLandscapePreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek, CardDriveRecordMode.WF_LANDSCAPE, darkTheme = true)

@Preview(name = "WF 세로 · 펼침 · 라이트", widthDp = 600)
@Composable
private fun WfPortraitPreview() =
    PreviewCard(CardDriveRecordSampleData.ThisWeek.copy(expanded = true), CardDriveRecordMode.WF_PORTRAIT, darkTheme = false)
