package com.altercharge.app.ui.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.altercharge.app.R

/**
 * 서체. Figma 기준 Asta Sans (42dot, SIL OFL 1.1). 라이선스 전문은 assets/licenses/AstaSans-OFL.txt.
 * 시안이 쓰는 굵기 400 · 700 · 800만 넣는다.
 */
val AstaSans: FontFamily = FontFamily(
    Font(R.font.asta_sans_regular, FontWeight.W400),
    Font(R.font.asta_sans_bold, FontWeight.W700),
    Font(R.font.asta_sans_extrabold, FontWeight.W800),
)

/** Figma font_style: normal 400 · strong 700 · extra_strong 800 */
object AlterFontWeight {
    val Normal = FontWeight.W400
    val Strong = FontWeight.W700
    val ExtraStrong = FontWeight.W800
}

/** 모든 글자 공통: 줄 높이 = 크기 × 1.5, 자간 −3% */
private fun style(sizeSp: Int, weight: FontWeight) = TextStyle(
    fontFamily = AstaSans,
    fontWeight = weight,
    fontSize = sizeSp.sp,
    lineHeight = (sizeSp * 1.5f).sp,
    letterSpacing = (-0.03).em,
)

/**
 * 글자 스타일. Figma Typography (103:46833, 변수 pleos Typography) 값 그대로.
 * 굵기는 Figma 기본 굵기다. 시안이 다른 굵기를 쓰면 copy(fontWeight = AlterFontWeight.Strong)로 바꾼다.
 */
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
    /** body/xxsmall: Style Guide 표에는 없고 카드 시안에서 쓴다 (20/30) */
    val bodyXXSmall: TextStyle = style(20, AlterFontWeight.Strong),
    val labelLarge: TextStyle = style(28, AlterFontWeight.Normal),
    val labelMedium: TextStyle = style(24, AlterFontWeight.Normal),
    val labelSmall: TextStyle = style(20, AlterFontWeight.Normal),
    val labelXSmall: TextStyle = style(16, AlterFontWeight.Normal),
    val numericLarge: TextStyle = style(60, AlterFontWeight.Strong),
    val numericMedium: TextStyle = style(40, AlterFontWeight.Strong),
    val numericSmall: TextStyle = style(28, AlterFontWeight.Strong),
    /** 큰 숫자 (C 카드 배터리 %, driving/speed_kmh와 같은 112/168) */
    val numericHero: TextStyle = style(112, AlterFontWeight.Strong),
)
