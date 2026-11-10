package com.altercharge.app.ui.card.station

import android.R
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private object CardStationDimens {
    val CardRadius = 24.dp
}
@Composable
fun CardStation(
    state: CardStationUiState,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(CardStationDimens.CardRadius)

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
                    tone = state.chipTone
                )

                ChargerAvailability()
            }

            // 충전소 이름
            Text(
                text = "연희 사러가 마트 급속",
                color = AlterTheme.colors.textPrimary,
                fontWeight = AlterFontWeight.Strong,
                fontSize = 32.sp
            )

            ChargingSpec()
        }

        Column() {
            Divider()
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                ) {
                Column() {
                    Text(
                        text = "1.8km",
                        fontSize = 32.sp,
                        fontWeight = AlterFontWeight.Strong,
                        color = AlterTheme.colors.textPrimary
                    )
                    Text(
                        text = "방문 3회",
                        color = AlterTheme.colors.textTertiary
                    )
                }

                IviButton(
                    hierarchy = IviButtonHierarchy.Contrast,
                    size = IviButtonSize.Small,
                    text = "여기로 안내",
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
    modifier: Modifier = Modifier
) {
Row(
    modifier = modifier
) {
    Text(
        text = "100kW",
        color = AlterTheme.colors.textPrimary,
        fontSize = 20.sp,
        modifier = modifier
            .clip(RoundedCornerShape(AlterRadius.Small))
            .background(AlterTheme.colors.surfaceHigh)
            .padding(vertical = AlterPadding.XXSmall, horizontal = AlterPadding.Small)
    )
}
}

@Composable
fun ChargerAvailability(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = modifier
                .size(10.dp)
                .background(AlterTheme.colors.blue500, shape = CircleShape)
        )
        Text(
            text = "3/4기",
            color = AlterTheme.colors.textPrimary,
            fontWeight = AlterFontWeight.Strong,
            modifier = modifier
                .padding(start = AlterMargin.XSmall)
                .padding(end = 14.dp)
        )

        // TODO: - 이미지 에셋 추가 시 ic_info 추가
        Box(
            modifier = modifier
                .size(10.dp)
                .background(AlterTheme.colors.textSecondary)
        )
    }
}

@Composable
fun CardStationChip(
    tone: CardStationUiState.ChipTone,
    modifier: Modifier = Modifier
) {
    Text(
        text = "추천",
        fontSize = 16.sp,
        color = AlterTheme.colors.blue700,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AlterTheme.colors.blue50)
            .padding(vertical = 4.dp, horizontal = 12.dp)
    )
}

val samplePreviewUiState = CardStationUiState(
    chipTone = CardStationUiState.ChipTone.ACCENT
)

@Preview(name = "R1 - 충전소 카드(Light)", widthDp = 383, heightDp = 368)
@Composable
private fun CardStationPreview() {
    AlterChargeTheme() {
        CardStation(state = samplePreviewUiState, modifier = Modifier)
    }
}

@Preview(name = "R1 - 충전소 카드(Dark)", widthDp = 383, heightDp = 368)
@Composable
private fun CardStationPreviewDark() {
    AlterChargeTheme(darkTheme = true) {
        CardStation(state = samplePreviewUiState, modifier = Modifier)
    }
}
