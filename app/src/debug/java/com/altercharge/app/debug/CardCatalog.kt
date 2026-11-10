package com.altercharge.app.debug

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.card.driverecord.CardDriveRecord
import com.altercharge.app.ui.card.driverecord.CardDriveRecordMode
import com.altercharge.app.ui.card.driverecord.CardDriveRecordSampleData
import com.altercharge.app.ui.card.driverecord.CardDriveRecordUiState
import com.altercharge.app.ui.card.driverecord.cardDriveRecordModeFor
import com.altercharge.app.ui.card.info.CardInfo
import com.altercharge.app.ui.card.info.CardInfoMode
import com.altercharge.app.ui.card.info.CardInfoSampleData
import com.altercharge.app.ui.card.info.cardInfoModeFor
import com.altercharge.app.ui.card.vehiclestatus.CardVehicleStatus
import com.altercharge.app.ui.card.vehiclestatus.CardVehicleStatusMode
import com.altercharge.app.ui.card.vehiclestatus.CardVehicleStatusSampleData
import com.altercharge.app.ui.card.vehiclestatus.cardVehicleStatusModeFor
import com.altercharge.app.ui.designsystem.component.DriveState
import com.altercharge.app.ui.designsystem.component.StatusBar
import com.altercharge.app.ui.designsystem.component.StatusBarUiState
import com.altercharge.app.ui.designsystem.component.StatusValue
import com.altercharge.app.ui.designsystem.layout.AlterChargeLayout
import com.altercharge.app.ui.designsystem.layout.LocalAppLayout
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import java.util.Locale

/*
 * 카드 확인용 화면. 디버그 빌드에만 들어간다. 샘플 값은 각 카드 패키지의 …SampleData를 쓴다.
 */

/** page "layout": 레이아웃 틀에 L1 · L2 · C 실제 카드를 넣는다. R1 · R2는 자리 표시. L2 펼침 버튼으로 상황 15를 본다. */
@Composable
fun LayoutCatalog() {
    var driveRecord by remember { mutableStateOf(CardDriveRecordSampleData.ThisWeek) }
    AlterChargeLayout(
        leftColumnScrollable = driveRecord.expanded,
        statusBar = {
            StatusBar(
                StatusBarUiState(
                    DriveState.PARKED,
                    battery = StatusValue.Ready(32),
                    range = StatusValue.Ready(98),
                    temperatureC = 12,
                ),
            )
        },
        l1 = {
            CardInfo(state = CardInfoSampleData.Today, mode = cardInfoModeFor(LocalAppLayout.current))
        },
        l2 = {
            CardDriveRecord(
                state = driveRecord,
                onPreviousWeek = { driveRecord = CardDriveRecordSampleData.LastWeek.copy(expanded = driveRecord.expanded) },
                onNextWeek = { driveRecord = CardDriveRecordSampleData.ThisWeek.copy(expanded = driveRecord.expanded) },
                onToggleExpanded = { driveRecord = driveRecord.copy(expanded = !driveRecord.expanded) },
                mode = cardDriveRecordModeFor(LocalAppLayout.current),
            )
        },
        c = {
            CardVehicleStatus(
                state = CardVehicleStatusSampleData.Charging,
                mode = cardVehicleStatusModeFor(LocalAppLayout.current),
            )
        },
        r1 = { PlaceholderBox("R1 추천 충전소", Modifier.height(368.dp)) },
        r2 = { PlaceholderBox("R2 충전소 목록") },
    )
}

/**
 * page "cv": C 카드 상황 01~12, 가로(790×1040) 두 장씩. 위아래 · 좌우로 스크롤한다.
 * 12 다음에 변형(판정으로 고른 01, 충전 오류 커넥터 · 충전기)이 이어진다.
 */
@Composable
fun VehicleStatusCatalog() {
    val labeled = CardVehicleStatusSampleData.all.mapIndexed { index, state ->
        String.format(Locale.ROOT, "%02d %s", index + 1, state.situation.name) to state
    } + CardVehicleStatusSampleData.variants.map { state ->
        val detail = state.fault?.name ?: "chargeRecommended=${state.chargeRecommended} · SOC ${state.socPercent}"
        "변형 ${state.situation.name} · $detail" to state
    }
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(AlterPadding.XLarge),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.XLarge),
    ) {
        labeled.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.XLarge)) {
                row.forEach { (label, state) ->
                    Column(verticalArrangement = Arrangement.spacedBy(AlterMargin.XSmall)) {
                        CatalogLabel(label)
                        CardVehicleStatus(
                            state = state,
                            mode = CardVehicleStatusMode.LANDSCAPE,
                            modifier = Modifier.size(VehicleStatusSizes.getValue(CardVehicleStatusMode.LANDSCAPE)),
                        )
                    }
                }
            }
        }
    }
}

/** page "cards": L1 · L2 · C를 모드별로. 크기는 각 모드의 시안 크기다. */
@Composable
fun CardsCatalog() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(AlterPadding.XLarge),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.XLarge),
    ) {
        // L1: 모드마다 오늘 · 내일 · 3일내. 높이는 카드가 mode로 정한다.
        CardInfoMode.entries.forEach { mode ->
            CatalogLabel("L1 card_info · ${mode.name} · ${CardInfoWidths.getValue(mode).value.toInt()}×${mode.height.value.toInt()}")
            Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
                CardInfoSampleData.all.forEach { state ->
                    CardInfo(state = state, mode = mode, modifier = Modifier.width(CardInfoWidths.getValue(mode)))
                }
            }
        }
        // L2: 모드마다 접힘 · 펼침 (버튼으로 바꿀 수 있다)
        CardDriveRecordMode.entries.forEach { mode ->
            CatalogLabel("L2 card_drive_record · ${mode.name} · 폭 ${DriveRecordWidths.getValue(mode).value.toInt()}")
            Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
                DriveRecordSample(CardDriveRecordSampleData.ThisWeek, mode)
                DriveRecordSample(CardDriveRecordSampleData.ThisWeek.copy(expanded = true), mode)
                DriveRecordSample(CardDriveRecordSampleData.LastWeek, mode)
            }
        }
        // C: 모드마다 충전 중 · 미연결 배터리 위험 (Figma variant charging · unplugged/critical)
        CardVehicleStatusMode.entries.forEach { mode ->
            val size = VehicleStatusSizes.getValue(mode)
            CatalogLabel("C card_vehicle_status · ${mode.name} · ${size.width.value.toInt()}×${size.height.value.toInt()}")
            Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
                listOf(CardVehicleStatusSampleData.Charging, CardVehicleStatusSampleData.CriticalUnplugged).forEach { state ->
                    CardVehicleStatus(
                        state = state,
                        mode = mode,
                        modifier = Modifier.size(size),
                    )
                }
            }
        }
    }
}

@Composable
private fun DriveRecordSample(initial: CardDriveRecordUiState, mode: CardDriveRecordMode) {
    var state by remember { mutableStateOf(initial) }
    CardDriveRecord(
        state = state,
        onPreviousWeek = {},
        onNextWeek = {},
        onToggleExpanded = { state = state.copy(expanded = !state.expanded) },
        mode = mode,
        modifier = Modifier.width(DriveRecordWidths.getValue(mode)),
    )
}

@Composable
private fun CatalogLabel(text: String) {
    Text(text, style = AlterTheme.typography.labelSmall, color = AlterTheme.colors.textTertiary)
}

/** L1 모드별 시안 폭. 가로는 홈에 실제로 놓이는 383 */
private val CardInfoWidths: Map<CardInfoMode, Dp> = mapOf(
    CardInfoMode.LANDSCAPE to 383.dp,
    CardInfoMode.PORTRAIT to 378.dp,
    CardInfoMode.WF_LANDSCAPE to 387.dp,
    CardInfoMode.WF_PORTRAIT to 580.dp,
)

/** L2 모드별 시안 폭 */
private val DriveRecordWidths: Map<CardDriveRecordMode, Dp> = mapOf(
    CardDriveRecordMode.LANDSCAPE to 383.dp,
    CardDriveRecordMode.LANDSCAPE_TALL to 375.dp,
    CardDriveRecordMode.PORTRAIT to 378.dp,
    CardDriveRecordMode.WF_LANDSCAPE to 387.dp,
    CardDriveRecordMode.WF_PORTRAIT to 580.dp,
)

/** C 모드별 시안 크기 (Figma 스펙 시트 366:39011 인스턴스) */
private val VehicleStatusSizes: Map<CardVehicleStatusMode, DpSize> = mapOf(
    CardVehicleStatusMode.LANDSCAPE to DpSize(790.dp, 1040.dp),
    CardVehicleStatusMode.LANDSCAPE_TALL to DpSize(770.dp, 1256.dp),
    CardVehicleStatusMode.PORTRAIT to DpSize(776.dp, 652.dp),
    CardVehicleStatusMode.PORTRAIT_TALL to DpSize(736.dp, 816.dp),
    CardVehicleStatusMode.WF_LANDSCAPE to DpSize(387.dp, 751.dp),
    CardVehicleStatusMode.WF_PORTRAIT to DpSize(580.dp, 666.dp),
)
