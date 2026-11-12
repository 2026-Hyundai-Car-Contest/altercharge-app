package com.altercharge.app.ui.common.dropdown

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme


@Composable
fun SelectableDropdownItem(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AlterRadius.Medium)

    Surface (
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = shape,
        color = if (selected) {
            AlterTheme.colors.blue50
        } else {
            AlterTheme.colors.surface
        },
        border = if (selected) {
            BorderStroke(2.dp, AlterTheme.colors.blue500)
        } else {
            null
        }
    ) {
        Row (
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.Small)
        ) {
            // 체크박스
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(
                        width = 1.5.dp,
                        color = if (selected) {
                            Color.Transparent
                        } else {
                            AlterTheme.colors.contentNeutral
                        },
                        shape = RoundedCornerShape(AlterRadius.XSmall)
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (selected) {
                            AlterTheme.colors.blue500
                        } else {
                            Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "선택됨",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 텍스트
            Column (
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = AlterFontWeight.Strong,
                    color = AlterTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = description,
                    fontSize = 16.sp,
                    color = AlterTheme.colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(name = "SelectableDropDownItem(Light/KO)", locale = "ko")
@Composable
private fun SelectableDropDownItemPreview() {
    AlterChargeTheme() {
        DropDownWrapper(
            title = "연결 타입",
            subTitle = "여러 개 선택",
            onReset = {},
            onApply = {}
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectableDropdownItem(
                    title = "DC 콤보 (CCS1)",
                    description = "DC 급속 · 약 200~1000V · 현대 · 기아",
                    selected = false,
                    onClick = {}
                )
                SelectableDropdownItem(
                    title = "DC 콤보 (CCS1)",
                    description = "DC 급속 · 약 200~1000V · 현대 · 기아",
                    selected = false,
                    onClick = {}
                )
                SelectableDropdownItem(
                    title = "DC 콤보 (CCS1)",
                    description = "DC 급속 · 약 200~1000V · 현대 · 기아",
                    selected = true,
                    onClick = {}
                )
                SelectableDropdownItem(
                    title = "DC 콤보 (CCS1)",
                    description = "DC 급속 · 약 200~1000V · 현대 · 기아",
                    selected = false,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "SelectableDropDownItem(Dark/KO)", locale = "ko")
@Composable
private fun SelectableDropDownItemDarkPreview() {
    AlterChargeTheme(darkTheme = true) {
        DropDownWrapper(
            title = "연결 타입",
            subTitle = "여러 개 선택",
            onReset = {},
            onApply = {}
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectableDropdownItem(
                    title = "DC 콤보 (CCS1)",
                    description = "DC 급속 · 약 200~1000V · 현대 · 기아",
                    selected = false,
                    onClick = {}
                )
                SelectableDropdownItem(
                    title = "DC 콤보 (CCS1)",
                    description = "DC 급속 · 약 200~1000V · 현대 · 기아",
                    selected = false,
                    onClick = {}
                )
                SelectableDropdownItem(
                    title = "DC 콤보 (CCS1)",
                    description = "DC 급속 · 약 200~1000V · 현대 · 기아",
                    selected = true,
                    onClick = {}
                )
                SelectableDropdownItem(
                    title = "DC 콤보 (CCS1)",
                    description = "DC 급속 · 약 200~1000V · 현대 · 기아",
                    selected = false,
                    onClick = {}
                )
            }
        }
    }
}
