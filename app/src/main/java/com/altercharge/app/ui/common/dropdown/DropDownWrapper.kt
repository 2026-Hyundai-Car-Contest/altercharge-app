package com.altercharge.app.ui.common.dropdown

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
    // 폭 · 최대 높이는 팝업이 정한다(패널 폭). 높이가 모자라면 옵션만 스크롤하고 초기화 · 적용은 아래에 고정한다
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AlterRadius.Large))
            .background(AlterTheme.colors.background)
            .padding(AlterMargin.XSmall)
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
                // "100kW ~ 350kW 이상"처럼 길어져도 두 줄로 꺾여 아래 내용을 밀지 않게 한다
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp),
                // 패널 폭 안에서 가장 긴 문구("100kW ~ 350kW 이상")가 잘리지 않는 크기
                fontSize = 14.sp,
                color = AlterTheme.colors.textTertiary.copy(0.64f)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
        ) {
            content()
        }

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
