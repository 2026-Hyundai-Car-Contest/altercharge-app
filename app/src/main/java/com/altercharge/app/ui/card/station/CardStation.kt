package com.altercharge.app.ui.card.station

import androidx.compose.ui.res.pluralStringResource
import com.altercharge.app.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.ui.card.stationitem.connectorLabelText
import com.altercharge.app.ui.designsystem.component.IviButton
import com.altercharge.app.ui.designsystem.component.IviButtonHierarchy
import com.altercharge.app.ui.designsystem.component.IviButtonSize
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterColors
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
fun CardStation(
    state: CardStationUiState,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AlterRadius.XXLarge)

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .clip(shape)
            .background(AlterTheme.colors.surface)
            .padding(AlterMargin.Normal)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CardStationChip(
                    state = state.chipState,
                )

                ChargerAvailability(
                    availableChargerCount = state.availableChargerCount,
                    totalChargerCount = state.totalChargerCount,
                    chargerStatus = state.chargerStatus
                )
            }

            // 충전소 이름
            Text(
                text = state.stationName,
                color = AlterTheme.colors.textPrimary,
                fontWeight = AlterFontWeight.Strong,
                fontSize = 32.sp
            )

            // 충전소 주소 (REQ_ST_007 상세 정보). 상세를 못 받았으면 줄을 숨긴다
            state.stationAddress?.takeIf { it.isNotBlank() }?.let { address ->
                Text(
                    text = address,
                    color = AlterTheme.colors.textTertiary,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.chargerInfo.forEach { chargeInfoTag ->
                    ChargingSpec(
                        chargeInfoTag = chargeInfoTag
                    )
                }

                // 충전기 연결 타입 (REQ_ST_007). 여러 규격은 "DC콤보 · AC완속"으로 잇는다
                connectorLabelText(state.connectorTypes)?.let { label ->
                    Text(
                        text = label,
                        color = AlterTheme.colors.textTertiary,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Column(
            modifier = modifier
                .padding(top = 91.dp)
        ) {
            Divider()
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                ) {
                Column() {
                    Text(
                        text = "${state.distance}km",
                        fontSize = 32.sp,
                        fontWeight = AlterFontWeight.Strong,
                        color = AlterTheme.colors.textPrimary
                    )
                    Text(
                        text = pluralStringResource(R.plurals.card_station_visit_count, state.myChargingCount, state.myChargingCount),
                        style = AlterTheme.typography.labelXSmall,
                        color = AlterTheme.colors.textTertiary
                    )
                }

                IviButton(
                    hierarchy = IviButtonHierarchy.Contrast,
                    size = IviButtonSize.Compact,
                    text = stringResource(R.string.card_station_navigate),
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun Divider() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(1.5.dp)
        .background(AlterTheme.colors.surfaceAccent)
    ) {}
}

@Composable
fun ChargingSpec(
    chargeInfoTag: ChargeInfoTag,
    modifier: Modifier = Modifier
) {
Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(AlterPadding.XXSmall),
    modifier = modifier
        .clip(RoundedCornerShape(AlterRadius.Small))
        .background(AlterTheme.colors.surfaceHigh)
        .padding(vertical = AlterPadding.XXSmall, horizontal = AlterPadding.Small)
) {
    if (chargeInfoTag is ChargeInfoTag.Icon) {
        // Figma tag_speed: ic_charger 18 (속도별 아이콘)
        Image(
            painter = painterResource(id = chargeInfoTag.resId),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
    val displayText = when (chargeInfoTag.type) {
        ChargeInfoType.CHARGING_TYPE -> {
            when (chargeInfoTag.text) {
                "완속" -> stringResource(R.string.card_station_charging_type_slow)
                "급속" -> stringResource(R.string.card_station_charging_type_fast)
                "초급속" -> stringResource(R.string.card_station_charging_type_ultra_fast)
                else -> chargeInfoTag.text
            }
        }

        ChargeInfoType.POWER -> chargeInfoTag.text
    }
    Text(
        text = displayText,
        color = AlterTheme.colors.textPrimary,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        modifier = modifier
    )
}
}

@Composable
fun ChargerAvailability(
    availableChargerCount: Int,
    totalChargerCount: Int,
    chargerStatus: ChargerStatus,
    modifier: Modifier = Modifier
) {
    val chargerAvailableText =
        if(chargerStatus == ChargerStatus.AVAILABLE) stringResource(R.string.card_station_availability_possible, "$availableChargerCount / $totalChargerCount")
        else stringResource(R.string.card_station_availability_impossibility)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
    ) {
Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
        .clip(RoundedCornerShape(AlterMargin.XSmall))
        .background(chargerStatus.statusBackgroundColor())
        .padding(horizontal = 12.dp)
) {
    Box(
        modifier = modifier
            .size(10.dp)
            .background(chargerStatus.statusColor(), shape = CircleShape)
    )
    Text(
        text = chargerAvailableText,
        color = AlterTheme.colors.textPrimary,
        fontWeight = AlterFontWeight.Strong,
        lineHeight = 30.sp,
        modifier = modifier
            .padding(start = AlterMargin.XSmall)

    )
}
    }
}

@Composable
fun CardStationChip(
    state: ChipState,
    modifier: Modifier = Modifier
) {
    Text(
        text = state.text,
        fontSize = 16.sp,
        color = state.chipTone.textColor(),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(state.chipTone.backgroundColor())
            .padding(vertical = 4.dp, horizontal = 12.dp)
    )
}

// extension
@Composable
private fun ChipTone.backgroundColor(): Color {
    return when(this) {
        ChipTone.ACCENT -> AlterTheme.colors.blue50
        ChipTone.NEUTRAL -> AlterTheme.colors.blue50
    }
}

@Composable
private fun ChipTone.textColor(): Color {
    return when(this) {
        ChipTone.ACCENT -> AlterTheme.colors.blue700
        ChipTone.NEUTRAL -> AlterTheme.colors.contentNeutral
    }
}

@Composable
private fun ChargerStatus.statusColor(): Color {
    return when(this) {
        ChargerStatus.AVAILABLE -> AlterTheme.colors.blue500
        ChargerStatus.UNDER_INSPECTION -> AlterTheme.colors.negative
    }
}

@Composable
private fun ChargerStatus.statusBackgroundColor(): Color {
    return when(this) {
        ChargerStatus.AVAILABLE -> AlterTheme.colors.blue50
        ChargerStatus.UNDER_INSPECTION -> AlterTheme.colors.negative.copy(alpha = 0.16f)
    }
}

// Preview 띄우기용 샘플 데이터
val samplePreviewUiState = CardStationUiState(
    chipState = ChipState(chipTone = ChipTone.NEUTRAL, text = "추천"),
    availableChargerCount = 3,
    totalChargerCount = 4,
    chargerStatus = ChargerStatus.UNDER_INSPECTION,
    stationName = "연희 사러가 마트",
    chargerInfo = listOf(
        ChargeInfoTag.Icon(resId = R.drawable.ic_charger_fast, "급속", type = ChargeInfoType.CHARGING_TYPE),
        ChargeInfoTag.Text("100Kw", type = ChargeInfoType.POWER)
    ),
    distance = 1.8,
    myChargingCount = 3,
    stationAddress = "서울 서대문구 연희맛로 25",
    connectorTypes = listOf(ConnectorType.DC_COMBO, ConnectorType.AC_SLOW)
)

