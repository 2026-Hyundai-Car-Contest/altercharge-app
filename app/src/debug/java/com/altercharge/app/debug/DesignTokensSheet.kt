package com.altercharge.app.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterShadows
import com.altercharge.app.ui.designsystem.theme.AlterTheme
import com.altercharge.app.ui.designsystem.theme.alterShadow

/** 디자인 값 확인용 모음. Figma Style Guide와 나란히 놓고 비교한다. 미리보기와 디버그 확인 화면에서 같이 쓴다. 디버그 빌드에만 들어간다. */
@Composable
fun DesignTokensSheet() {
    val colors = AlterTheme.colors
    val type = AlterTheme.typography
    Column(
        modifier = Modifier.background(colors.screenBackground).padding(AlterPadding.XLarge),
        verticalArrangement = Arrangement.spacedBy(AlterMargin.Large),
    ) {
        Swatches(
            listOf(
                colors.blue50, colors.blue100, colors.blue200, colors.blue300, colors.blue400,
                colors.blue500, colors.blue600, colors.blue700, colors.blue800, colors.blue900,
            ),
        )
        Swatches(
            listOf(
                colors.textPrimary, colors.textSecondary, colors.textTertiary, colors.textDisabled,
                colors.screenBackground, colors.surface, colors.surfaceHigh, colors.surfaceAccent,
                colors.positive, colors.negative, colors.danger, colors.warning,
            ),
        )
        Box(
            Modifier
                .size(240.dp, 120.dp)
                .alterShadow(AlterShadows.Level1, AlterRadius.XXLarge)
                .background(colors.surface, RoundedCornerShape(AlterRadius.XXLarge)),
        )
        TypeSample("Headline L 60", type.headlineLarge)
        TypeSample("Title S 32", type.titleSmall)
        TypeSample("Body M 28", type.bodyMedium)
        TypeSample("Label M 24", type.labelMedium)
        TypeSample("22%", type.numericHero)
    }
}

@Composable
private fun Swatches(colors: List<Color>) {
    Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.XSmall)) {
        colors.forEach {
            Box(Modifier.size(56.dp).background(it, RoundedCornerShape(AlterRadius.Small)))
        }
    }
}

@Composable
private fun TypeSample(text: String, style: TextStyle) {
    Text(text, style = style, color = AlterTheme.colors.textPrimary)
}

@Preview(name = "디자인 값 · 라이트", widthDp = 900)
@Composable
private fun DesignTokensLightPreview() {
    AlterChargeTheme(darkTheme = false) { DesignTokensSheet() }
}

@Preview(name = "디자인 값 · 다크", widthDp = 900)
@Composable
private fun DesignTokensDarkPreview() {
    AlterChargeTheme(darkTheme = true) { DesignTokensSheet() }
}
