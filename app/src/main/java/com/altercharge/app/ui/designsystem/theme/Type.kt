package com.altercharge.app.ui.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.altercharge.app.R

/** Asta Sans (42dot, SIL OFL 1.1, assets/licenses/AstaSans-OFL.txt). 시안이 쓰는 굵기만 넣는다 */
val AstaSans: FontFamily = FontFamily(
    Font(R.font.asta_sans_regular, FontWeight.W400),
    Font(R.font.asta_sans_bold, FontWeight.W700),
    Font(R.font.asta_sans_extrabold, FontWeight.W800),
)

/** Figma font_style */
object AlterFontWeight {
    val Normal = FontWeight.W400
    val Strong = FontWeight.W700
    val ExtraStrong = FontWeight.W800
}

/**
 * Compose 기본값은 첫 줄 위 · 끝 줄 아래 여백을 잘라 시안보다 짧아져서 Figma처럼 줄 높이 전체를 쓴다.
 * 카드에서 TextStyle을 직접 만들 때도 이 값을 넣는다.
 */
val AlterLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

val AlterLetterSpacing = (-0.03).em

private fun style(sizeSp: Int, weight: FontWeight) = TextStyle(
    fontFamily = AstaSans,
    fontWeight = weight,
    fontSize = sizeSp.sp,
    lineHeight = (sizeSp * 1.5f).sp,
    letterSpacing = AlterLetterSpacing,
    lineHeightStyle = AlterLineHeightStyle,
)

/** Figma Typography (103:46833). 시안이 다른 굵기를 쓰면 copy(fontWeight = …)로 바꾼다 */
@Immutable
data class AlterTypography(
    val headlineLarge: TextStyle = style(60, AlterFontWeight.ExtraStrong),
    val headlineMedium: TextStyle = style(56, AlterFontWeight.ExtraStrong),
    val headlineSmall: TextStyle = style(48, AlterFontWeight.ExtraStrong),
    val titleLarge: TextStyle = style(40, AlterFontWeight.ExtraStrong),
    val titleMedium: TextStyle = style(36, AlterFontWeight.ExtraStrong),
    val titleSmall: TextStyle = style(32, AlterFontWeight.Strong),
    val bodyLarge: TextStyle = style(30, AlterFontWeight.Strong),
    val bodyMedium: TextStyle = style(28, AlterFontWeight.Strong),
    val bodySmall: TextStyle = style(26, AlterFontWeight.Strong),
    val bodyXSmall: TextStyle = style(24, AlterFontWeight.Strong),
    /** Style Guide 표에는 없고 카드 시안에서 쓴다 */
    val bodyXXSmall: TextStyle = style(20, AlterFontWeight.Strong),
    val labelLarge: TextStyle = style(28, AlterFontWeight.Normal),
    val labelMedium: TextStyle = style(24, AlterFontWeight.Normal),
    val labelSmall: TextStyle = style(20, AlterFontWeight.Normal),
    val labelXSmall: TextStyle = style(16, AlterFontWeight.Normal),
    val numericLarge: TextStyle = style(60, AlterFontWeight.Strong),
    val numericMedium: TextStyle = style(40, AlterFontWeight.Strong),
    val numericSmall: TextStyle = style(28, AlterFontWeight.Strong),
    /** driving/speed_kmh와 같은 112/168 */
    val numericHero: TextStyle = style(112, AlterFontWeight.Strong),
)
