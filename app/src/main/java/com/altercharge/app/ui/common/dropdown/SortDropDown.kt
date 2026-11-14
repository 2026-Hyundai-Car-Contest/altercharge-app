package com.altercharge.app.ui.common.dropdown

import androidx.compose.ui.draw.alpha
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
fun <T> SortDropDown(
    items: List<T>,
    selectedItem: T,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    /** 고를 수 없는 항목은 흐리게 두고 누르지 못하게 한다 (예: 방문 기록이 없을 때 방문순, REQ_SL_004 예외) */
    isEnabled: (T) -> Boolean = { true },
    /** 메뉴가 열리고 닫힐 때 알린다 (목록을 어둡게 덮는 데 쓴다) */
    onExpandedChange: (Boolean) -> Unit = {},
) {
    var isDropDownMenuExpanded by remember {
        mutableStateOf(false)
    }
    val setExpanded: (Boolean) -> Unit = {
        isDropDownMenuExpanded = it
        onExpandedChange(it)
    }
    Box(
        modifier = Modifier.wrapContentSize(
            Alignment.TopStart
        )
    ) {
        // 정렬은 늘 한 가지가 골라져 있어 선택 칩 모양이다 (Figma chip_filter selected=true)
        FilterChip(
            label = itemLabel(selectedItem),
            selected = true,
            expanded = isDropDownMenuExpanded,
            onClick = { setExpanded(true) },
        )

        // Figma dropdown_sort: 칩 아래 8에 열리고, 항목을 누르면 바로 적용하고 닫는다 (디자이너 QA 04 · 05)
        FilterDropdownPopup(
            expanded = isDropDownMenuExpanded,
            onDismissRequest = { setExpanded(false) },
        ) {
            Column(
                modifier = Modifier
                    .width(216.dp)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    SortDropDownItem(
                        text = itemLabel(item),
                        isSelected = item == selectedItem,
                        enabled = isEnabled(item),
                        onClick = {
                            onItemSelected(item)
                            setExpanded(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SortDropDownItem(
    text: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(AlterRadius.Medium))
            .background(
                if (isSelected) {
                    AlterTheme.colors.blue50
                } else {
                    AlterTheme.colors.containerBackgroundSubtle
                }
            )
            .border(
                width = 2.dp,
                color = if (isSelected) {
                    AlterTheme.colors.blue500
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(AlterRadius.Medium)
            )
            .alpha(if (enabled) 1f else 0.35f)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp)
    ) {
        // 색을 정하지 않으면 Material 기본(검정)이라 다크 모드에서 글자 · 체크가 배경에 묻힌다
        Text(
            text = text,
            color = AlterTheme.colors.textPrimary,
        )

        if (isSelected) {
            Image(
                painter = painterResource(R.drawable.ic_status_check),
                contentDescription = null,
                colorFilter = ColorFilter.tint(AlterTheme.colors.textPrimary),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(20.dp))
        }
    }
}

/** 라벨은 문자열 리소스라 화면에서 stringResource(labelRes)로 읽는다 (영문 화면 대응) */
enum class StationSortType(
    @StringRes val labelRes: Int
) {
    DISTANCE(R.string.station_list_sort_distance),
    VISIT(R.string.station_list_sort_visit)
}
