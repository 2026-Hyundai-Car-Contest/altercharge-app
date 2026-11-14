package com.altercharge.app.ui.card.stationitem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.res.pluralStringResource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.data.remote.model.ConnectorType
import com.altercharge.app.ui.card.station.ChargingType
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * R2 목록 한 줄. 이름 · 가용 대수 · 거리(방문순이면 방문 횟수) · 연결 타입 · 주소를 보여 준다 (REQ_SL_001 · REQ_SL_004).
 * 가용 대수는 15분이 지나면 "정보 없음"으로 바뀐다 (REQ_CM_009).
 */
@Composable
fun StationItem(
    state: CardItemUiState,
    modifier: Modifier = Modifier,
    /** 목록에서 고른 충전소 (REQ_SL_006). Figma state=selected: blue_50 바탕 + blue_500 2px 테두리 */
    selected: Boolean = false,
) {
    // 전체 컨테이너
    Column(
        modifier = modifier
            .fillMaxWidth()
            // Figma item 그림자: y 2 · blur 8 · 검정 5%
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(AlterRadius.Small), ambientColor = ItemShadow, spotColor = ItemShadow)
            .clip(RoundedCornerShape(AlterRadius.Small))
            // Figma item/enabled/container = basic_00(라이트 흰색 · 다크 #131417)
            .background(if (selected) AlterTheme.colors.blue50 else AlterTheme.colors.surface)
            .then(
                if (selected) Modifier.border(2.dp, AlterTheme.colors.blue500, RoundedCornerShape(AlterRadius.Small))
                else Modifier
            )
            .padding(vertical = 10.dp, horizontal = 16.dp)
    ) {
        // 충전소명(좌측), 충전소자리(우측)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 충전소명
            Text(
                text = state.stationName,
                fontWeight = AlterFontWeight.Strong,
                // Figma name: text_dark_secondary
                color = AlterTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // 충전소자리 태그: 가용/전체
            if (state.availabilityUnknown) {
                Text(
                    text = stringResource(R.string.common_value_unavailable),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = AlterTheme.colors.textTertiary,
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(AlterRadius.Small))
                        .background(AlterTheme.colors.surfaceHigh)
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Image(
                        painter = painterResource(state.chargingType.image()),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = state.availableChargerCount.toString(),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = AlterFontWeight.Strong,
                        color = AlterTheme.colors.textPrimary,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                    // Figma seats: "2" · "/4" · "대" (사선과 전체 수는 붙여 쓴다)
                    Text(
                        text = "/${state.totalChargerCount}",
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = AlterTheme.colors.textTertiary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.card_item_avaliable_unit_text, "대"),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = AlterTheme.colors.textTertiary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // 거리(또는 방문 횟수), 연결 타입, 충전소 도로명 주소
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Text(
                text = if (state.showVisitCount) {
                    pluralStringResource(R.plurals.card_item_visit_count, state.visitCount, state.visitCount)
                } else {
                    "${state.distance}km"
                },
                color = AlterTheme.colors.textPrimary,
                fontWeight = AlterFontWeight.Strong,
                fontSize = 24.sp,
                lineHeight = 36.sp
            )

            Box(
                modifier = Modifier
                    .background(AlterTheme.colors.surfaceAccent)
                    .height(14.dp)
                    .width(1.5.dp)
            )

            // Figma location_row: 거리 | 연결 타입 | 주소. 연결 타입은 배경 없는 글자(text_dark_secondary 16)
            connectorLabelText(state.connectorTypes)?.let { label ->
                Text(
                    text = label,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = AlterTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .background(AlterTheme.colors.surfaceAccent)
                        .height(14.dp)
                        .width(1.5.dp)
                )
            }

            Text(
                text = state.chargeStationAddress,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = AlterTheme.colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// extension

@Composable
private fun ChargingType.image(): Int {
    return when (this) {
        ChargingType.ULTRA_FAST -> R.drawable.ic_charger_ultra
        ChargingType.DC -> R.drawable.ic_charger_fast
        ChargingType.AC -> R.drawable.ic_charger_slow
    }
}

private val PreviewStations = listOf(
    CardItemUiState(
        stationId = "NT101224",
        stationName = "연희동 주민센터",
        availableChargerCount = 4,
        totalChargerCount = 6,
        chargingType = ChargingType.AC,
        distance = 1.2,
        chargeStationAddress = "서울 마포구 동교로 263",
        connectorTypes = listOf(ConnectorType.AC_SLOW),
        visitCount = 3,
    ),
    CardItemUiState(
        stationId = "CV001816",
        stationName = "연남동 공영주차장",
        availableChargerCount = 0,
        totalChargerCount = 3,
        chargingType = ChargingType.ULTRA_FAST,
        distance = 1.8,
        chargeStationAddress = "서울 마포구 양화로",
        connectorTypes = listOf(ConnectorType.DC_COMBO, ConnectorType.NACS),
        visitCount = 0,
        showVisitCount = true,
    ),
)

@Preview(name = "Station Item(Light/KO)", locale = "ko")
@Composable
private fun StationItemListLightKoPreview() {
    AlterChargeTheme(darkTheme = false) {
        Box(modifier = Modifier.width(351.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                PreviewStations.forEach { StationItem(state = it) }
            }
        }
    }
}

@Preview(name = "Station Item(Dark/KO)", locale = "ko")
@Composable
private fun StationItemListDarkKoPreview() {
    AlterChargeTheme(darkTheme = true) {
        Box(modifier = Modifier.width(351.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                PreviewStations.forEach { StationItem(state = it) }
            }
        }
    }
}

/** Figma station_item 그림자 색: #141417 5% */
private val ItemShadow = Color(0x0D141417)
