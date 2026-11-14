package com.altercharge.app.ui.card.station

import com.altercharge.app.ui.card.info.lastCheckedText
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.ui.card.stationitem.connectorLabel
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
    /** 좌표가 없어 경로를 찾을 수 없으면 false로 넘겨 안내를 막는다 (REQ_BR_004 예외) */
    canNavigate: Boolean = true,
) {
    val shape = RoundedCornerShape(AlterRadius.XXLarge)

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .clip(shape)
            .background(AlterTheme.colors.containerBackground)
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

            // Figma title_block: 이름과 마지막 확인 시각 사이 4
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = state.stationName,
                    color = AlterTheme.colors.textPrimary,
                    fontWeight = AlterFontWeight.Strong,
                    fontSize = 32.sp,
                    // Figma name은 한 줄(48). 긴 이름이 두 줄이 되면 카드가 길어져 R2가 밀린다
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Figma title_block · last_checked: text_dark_tertiary 20 regular
                state.lastChecked?.let { lastChecked ->
                    Text(
                        text = lastCheckedText(lastChecked),
                        style = AlterTheme.typography.labelSmall,
                        color = AlterTheme.colors.textTertiary,
                    )
                }
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
            }

            // Figma R1 시안 1032:80386 address_line (REQ_ST_007): 칩 아래 "커넥터 · 주소" 한 줄, 넘치면 말줄임.
            // 커넥터가 여럿이면 "DC콤보, AC완속". 둘 다 없으면(상세를 못 받음) 줄을 숨긴다
            stationAddressLine(state.connectorTypes, state.stationAddress)?.let { line ->
                Text(
                    text = line,
                    style = AlterTheme.typography.labelXSmall,
                    color = AlterTheme.colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 카드 높이는 부르는 쪽이 고정한다. SpaceBetween으로 아래 줄을 바닥에 붙인다
        Column {
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
                    enabled = !navigating && canNavigate && state.chargerStatus != ChargerStatus.UNDER_INSPECTION,
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

/** R1 address_line: "DC콤보, AC완속 · 주소". 커넥터 · 주소 중 있는 것만 잇고, 둘 다 없으면 null */
@Composable
private fun stationAddressLine(connectorTypes: List<ConnectorType>, address: String?): String? {
    val connectors = connectorTypes.distinct().filter { it != ConnectorType.UNKNOWN }.map { connectorLabel(it) }
    val parts = listOfNotNull(
        connectors.takeIf { it.isNotEmpty() }?.joinToString(", "),
        address?.takeIf { it.isNotBlank() },
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
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
