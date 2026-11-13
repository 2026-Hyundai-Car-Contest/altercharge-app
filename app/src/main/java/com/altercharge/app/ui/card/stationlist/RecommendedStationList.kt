package com.altercharge.app.ui.card.stationlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.ui.common.dropdown.SortDropDown
import com.altercharge.app.ui.common.searchbar.SearchBar
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
fun RecommendedStationList(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(AlterRadius.XXLarge))
            .background(AlterTheme.colors.blue100)
            .padding(16.dp)
    ) {
        Text(
            text = "전기차 충전소",
            fontWeight = AlterFontWeight.Strong,
            fontSize = 24.sp,
            textAlign = TextAlign.Left
        )

        SearchBar(
            onValueChange = {}
        ) { }

        Row() {
            Text("필터자리")
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState) // 세로 스크롤 부여
            ) {
                repeat(3) { index ->
                    if (index == 1) {
                        SortDropDown(
                            items = listOf()
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "RecommendedStationList")
@Composable
private fun RecommendedStationListPreview() {
    AlterChargeTheme() {
        RecommendedStationList()
    }
}
