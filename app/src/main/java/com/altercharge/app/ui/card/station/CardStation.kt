package com.altercharge.app.ui.card.station

import androidx.compose.ui.res.pluralStringResource
import com.altercharge.app.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
fun CardStation(
    state: CardStationUiState,
    modifier: Modifier = Modifier,
    /** "여기로 안내" (REQ_BR_004). 요청 중이면 [navigating]으로 버튼을 막는다 */
    onNavigate: () -> Unit = {},
    navigating: Boolean = false,
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
                fontSize = 32.sp,
                // Figma name은 한 줄(48). 긴 이름이 두 줄이 되면 카드가 길어져 R2가 밀린다
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                    onClick = onNavigate,
                    // 점검 중이면 안내를 막는다 (Figma availability=fault · REQ_EX_004)
                    enabled = !navigating && state.chargerStatus != ChargerStatus.UNDER_INSPECTION,
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
        // Figma tag value: text_dark_tertiary 20 regular
        color = AlterTheme.colors.textTertiary,
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
    val chargerAvailableText = when (chargerStatus) {
        ChargerStatus.AVAILABLE -> stringResource(R.string.card_station_availability_possible, "$availableChargerCount / $totalChargerCount")
        ChargerStatus.UNDER_INSPECTION -> stringResource(R.string.card_station_availability_impossibility)
        ChargerStatus.UNKNOWN -> stringResource(R.string.common_value_unavailable)
    }

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
        // Figma value: 20 bold. 정보 없음은 text_dark_tertiary
        style = AlterTheme.typography.bodyXXSmall,
        color = if (chargerStatus == ChargerStatus.UNKNOWN) AlterTheme.colors.textTertiary else AlterTheme.colors.textPrimary,
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
        ChipTone.ACCENT -> AlterTheme.colors.blue500 // Figma chip_info tone=accent label = blue_500
        ChipTone.NEUTRAL -> AlterTheme.colors.contentNeutral
    }
}

@Composable
private fun ChargerStatus.statusColor(): Color {
    return when(this) {
        ChargerStatus.AVAILABLE -> AlterTheme.colors.blue500
        ChargerStatus.UNDER_INSPECTION -> AlterTheme.colors.negative
        ChargerStatus.UNKNOWN -> UnknownDotColor
    }
}

@Composable
private fun ChargerStatus.statusBackgroundColor(): Color {
    return when(this) {
        // Figma tag_seats bg는 세 상태 모두 16% 불투명도
        ChargerStatus.AVAILABLE, ChargerStatus.UNKNOWN -> AlterTheme.colors.blue50.copy(alpha = SeatsBgAlpha)
        ChargerStatus.UNDER_INSPECTION -> AlterTheme.colors.negative.copy(alpha = SeatsBgAlpha)
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

/** Figma tag_seats · bg 불투명도 */
private const val SeatsBgAlpha = 0.16f

/** Figma basic_400 (라이트 · 다크 같은 값) */
private val UnknownDotColor = Color(0xFFA4A8AE)
