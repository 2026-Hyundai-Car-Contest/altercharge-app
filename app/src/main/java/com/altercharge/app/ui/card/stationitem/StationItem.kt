package com.altercharge.app.ui.card.stationitem

import android.text.Layout
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
import com.altercharge.app.ui.card.station.ChargingType
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
fun StationItem(
    state: CardItemUiState,
    modifier: Modifier = Modifier
) {

    // 전체 컨테이너
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AlterRadius.Small))
            .background(AlterTheme.colors.background)
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
                color = AlterTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // 충전소자리 태그
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .clip(RoundedCornerShape(AlterRadius.Small))
                    .background(AlterTheme.colors.surfaceHigh)
                    .padding(vertical = 2.dp, horizontal = 6.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_charger_slow),
                    contentDescription = null,
                    modifier = modifier
                        .size(16.dp)
                )
                Text(
                    text = state.occupiedChargerCount.toString(),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = AlterFontWeight.Strong,
                    color = AlterTheme.colors.textPrimary,
                    modifier = Modifier.padding(start = 3.dp)
                )

                Text(
                    text = state.availableChargerCount.toString(),
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

        // 충전소까지거리/충전소 도로명 주소
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .padding(top = 6.dp)
        ) {
            Text(
                text = "${state.distance}km",
                color = AlterTheme.colors.textPrimary,
                fontWeight = AlterFontWeight.Strong,
                fontSize = 24.sp,
                lineHeight = 36.sp
            )

            Box(
                modifier = modifier
                    .background(AlterTheme.colors.surfaceAccent)
                    .height(14.dp)
                    .width(1.5.dp)
            )

            Text(
                text = state.chargeStationAddress,
                fontSize = 16.sp,
                color = AlterTheme.colors.textTertiary.copy(alpha = 0.64f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(name = "Station Item(Light/KO)", locale = "ko")
@Composable
private fun StationItemListLightKoPreview() {
    AlterChargeTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .width(351.dp)
        ) {
            Column (
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(10.dp)
            ) {
                listOf<CardItemUiState>(
                    CardItemUiState(
                        stationName = "연희동 주민센터",
                        availableChargerCount = 4,
                        occupiedChargerCount = 2,
                        chargingType = ChargingType.AC,
                        distance = 1.2,
                        chargeStationAddress = "서울 마포구 동교로 263"
                    ),
                    CardItemUiState(
                        stationName = "연남동 공영주차장",
                        availableChargerCount = 6,
                        occupiedChargerCount = 3,
                        chargingType = ChargingType.ULTRA_FAST,
                        distance = 1.2,
                        chargeStationAddress = "서울 마포구 양화로"
                    )
                ).forEach { cardItemUiState ->
                    StationItem(
                        state = cardItemUiState
                    )
                }
            }
        }
    }
}

@Preview(name = "Station Item(Dark/KO)", locale = "ko")
@Composable
private fun StationItemListDarkKoPreview() {
    AlterChargeTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .width(351.dp)
        ) {
            Column (
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(10.dp)
            ) {
                listOf<CardItemUiState>(
                    CardItemUiState(
                        stationName = "연희동 주민센터",
                        availableChargerCount = 4,
                        occupiedChargerCount = 2,
                        chargingType = ChargingType.AC,
                        distance = 1.2,
                        chargeStationAddress = "서울 마포구 동교로 263"
                    ),
                    CardItemUiState(
                        stationName = "연남동 공영주차장",
                        availableChargerCount = 6,
                        occupiedChargerCount = 3,
                        chargingType = ChargingType.ULTRA_FAST,
                        distance = 1.2,
                        chargeStationAddress = "서울 마포구 양화로"
                    )
                ).forEach { cardItemUiState ->
                    StationItem(
                        state = cardItemUiState
                    )
                }

            }
        }
    }
}

@Preview(name = "Station Item(Light/EN)", locale = "en")
@Composable
private fun StationItemListLightEnPreview() {
    AlterChargeTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .width(351.dp)
        ) {
            Column (
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(10.dp)
            ) {
                listOf<CardItemUiState>(
                    CardItemUiState(
                        stationName = "Yeonhui-dong Community Ctr",
                        availableChargerCount = 2,
                        occupiedChargerCount = 1,
                        chargingType = ChargingType.AC,
                        distance = 1.2,
                        chargeStationAddress = "140 Yeonhui-ro, Seodaemun-gu"
                    ),
                    CardItemUiState(
                        stationName = "Yeonnam-dong Parking",
                        availableChargerCount = 6,
                        occupiedChargerCount = 3,
                        chargingType = ChargingType.ULTRA_FAST,
                        distance = 1.2,
                        chargeStationAddress = "26 Yeonnam-dong Parking-ro"
                    )
                ).forEach { cardItemUiState ->
                    StationItem(
                        state = cardItemUiState
                    )
                }
            }
        }
    }
}

@Preview(name = "Station Item(Dark/EN)", locale = "en")
@Composable
private fun StationItemListDarkEnPreview() {
    AlterChargeTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .width(351.dp)
        ) {
            Column (
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(10.dp)
            ) {
                listOf<CardItemUiState>(
                    CardItemUiState(
                        stationName = "Yeonhui-dong Community Ctr",
                        availableChargerCount = 2,
                        occupiedChargerCount = 1,
                        chargingType = ChargingType.AC,
                        distance = 1.2,
                        chargeStationAddress = "140 Yeonhui-ro, Seodaemun-gu"
                    ),
                    CardItemUiState(
                        stationName = "Yeonnam-dong Parking",
                        availableChargerCount = 6,
                        occupiedChargerCount = 3,
                        chargingType = ChargingType.ULTRA_FAST,
                        distance = 1.2,
                        chargeStationAddress = "26 Yeonnam-dong Parking-ro"
                    )
                ).forEach { cardItemUiState ->
                    StationItem(
                        state = cardItemUiState
                    )
                }
            }
        }
    }
}
