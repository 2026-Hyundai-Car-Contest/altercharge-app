package com.altercharge.app.ui.common.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterFontWeight
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
fun DropDownWrapper(
    title: String,
    subTitle: String,
    onReset: () -> Unit,
    onApply: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(AlterRadius.Large))
            .background(AlterTheme.colors.background)
            .padding(AlterMargin.XSmall)
            .width(351.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = AlterFontWeight.Strong,
                color = AlterTheme.colors.textPrimary
            )
            // Figma filter_dropdown 헤더: 보조 문구는 남은 폭을 채우고 오른쪽 끝에 붙는다
            Text(
                text = subTitle,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(AlterMargin.Small),
                fontSize = 16.sp,
                color = AlterTheme.colors.textTertiary.copy(0.64f)
            )
        }

        content()

        FilterActionButtons(
            onReset = onReset,
            onApply = onApply
        )
    }
}


@Composable
fun FilterActionButtons(
    onReset: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(AlterRadius.Small))
                .clickable { onReset() },
            shape = RoundedCornerShape(AlterRadius.Small),
            color = AlterTheme.colors.surfaceHigh
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.strings_common_dropdown_wrapper_reset_button),
                    fontSize = 26.sp,
                    fontWeight = AlterFontWeight.Strong,
                    color = AlterTheme.colors.textPrimary
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clickable() { onApply() },
            shape = RoundedCornerShape(AlterRadius.Small),
            color = AlterTheme.colors.blue500
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.strings_common_dropdown_wrapper_apply_button),
                    fontSize = 26.sp,
                    fontWeight = AlterFontWeight.Strong,
                    color = Color.White
                )
            }
        }
    }
}


@Preview(name = "DropDownWrapper(Light/KO)", locale = "ko")
@Composable
private fun DropDownWrapperLightKoPreview() {
    AlterChargeTheme() {
        DropDownWrapper(
            title = "연결타입",
            subTitle = "여러 개 선택",
            onReset = {},
            onApply = {}
        ) {
            Text(
                text = "\"Some Component\"",
                Modifier.background(Color.Red)
                    .height(300.dp)
            )
        }
    }
}

@Preview(name = "DropDownWrapper(Dark/KO)", locale = "ko")
@Composable
private fun DropDownWrapperDarkKoPreview() {
    AlterChargeTheme(darkTheme = true) {
        DropDownWrapper(
            title = "연결타입",
            subTitle = "여러 개 선택",
            onReset = {},
            onApply = {}
        ) {
            Text(
                text = "\"Some Component\"",
                Modifier.background(Color.Red)
                    .height(300.dp)
            )
        }
    }
}

@Preview(name = "DropDownWrapper(Light/EN)", locale = "en")
@Composable
private fun DropDownWrapperLightEnPreview() {
    AlterChargeTheme() {
        DropDownWrapper(
            title = "연결타입",
            subTitle = "여러 개 선택",
            onReset = {},
            onApply = {}
        ) {
            Text(
                text = "\"Some Component\"",
                Modifier.background(Color.Red)
                    .height(300.dp)
            )
        }
    }
}

@Preview(name = "DropDownWrapper(Dark/EN)", locale = "en")
@Composable
private fun DropDownWrapperDarkEnPreview() {
    AlterChargeTheme(darkTheme = true) {
        DropDownWrapper(
            title = "연결타입",
            subTitle = "여러 개 선택",
            onReset = {},
            onApply = {}
        ) {
            Text(
                text = "\"Some Component\"",
                Modifier.background(Color.Red)
                    .height(300.dp)
            )
        }
    }
}
