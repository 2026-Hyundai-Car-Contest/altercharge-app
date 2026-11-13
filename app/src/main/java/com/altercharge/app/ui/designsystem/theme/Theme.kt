package com.altercharge.app.ui.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalAlterColors = staticCompositionLocalOf { LightAlterColors }

/** 라이트/다크 구분이 없어 한 번만 만든다 */
private val DefaultAlterTypography = AlterTypography()

private val LocalAlterTypography = staticCompositionLocalOf { DefaultAlterTypography }

/** 기획 확정: 지금은 [SYSTEM]. 1차 이후 설정 화면이 생기면 이 값만 넘기면 된다 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** 앱 진입점에서는 이 함수를 쓴다 */
@Composable
fun AlterChargeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    AlterChargeTheme(darkTheme = darkTheme, content = content)
}

/**
 * 미리보기처럼 라이트/다크를 직접 정할 때 쓴다.
 * 카드에서는 AlterTheme · AlterRadius · AlterMargin · AlterPadding · AlterShadows를 쓰고 숫자를 직접 쓰지 않는다.
 */
@Composable
fun AlterChargeTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkAlterColors else LightAlterColors
    // Material 컴포넌트를 쓸 때 어긋나지 않도록 기본 색만 맞춰 둔다
    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = colors.blue500,
            background = colors.screenBackground,
            surface = colors.surface,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            outline = colors.surfaceAccent,
        )
    } else {
        lightColorScheme(
            primary = colors.blue500,
            background = colors.screenBackground,
            surface = colors.surface,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            outline = colors.surfaceAccent,
        )
    }
    CompositionLocalProvider(
        LocalAlterColors provides colors,
        LocalAlterTypography provides DefaultAlterTypography,
    ) {
        MaterialTheme(colorScheme = materialColors, content = content)
    }
}

object AlterTheme {
    val colors: AlterColors
        @Composable @ReadOnlyComposable
        get() = LocalAlterColors.current

    val typography: AlterTypography
        @Composable @ReadOnlyComposable
        get() = LocalAlterTypography.current
}
