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
private val LocalAlterTypography = staticCompositionLocalOf { AlterTypography() }

/**
 * 앱 전체 테마. 화면과 카드는 모두 이 테마 안에서 그린다.
 *
 * 카드에서는 AlterTheme.colors / AlterTheme.typography와 AlterRadius · AlterMargin · AlterPadding,
 * AlterShadows를 쓴다. 색이나 크기를 숫자로 직접 쓰지 않는다.
 *
 * TODO 기획 확인: 라이트/다크 전환 규칙. 지금은 시스템 설정을 따른다.
 */
@Composable
fun AlterChargeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkAlterColors else LightAlterColors
    // Material 컴포넌트를 쓸 때 어긋나지 않도록 기본 색만 맞춰 둔다.
    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = colors.blue500,
            background = colors.background,
            surface = colors.surface,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            outline = colors.surfaceAccent,
        )
    } else {
        lightColorScheme(
            primary = colors.blue500,
            background = colors.background,
            surface = colors.surface,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            outline = colors.surfaceAccent,
        )
    }
    CompositionLocalProvider(
        LocalAlterColors provides colors,
        LocalAlterTypography provides AlterTypography(),
    ) {
        MaterialTheme(colorScheme = materialColors, content = content)
    }
}

/** 카드와 화면에서 디자인 값을 꺼내는 곳 */
object AlterTheme {
    val colors: AlterColors
        @Composable @ReadOnlyComposable
        get() = LocalAlterColors.current

    val typography: AlterTypography
        @Composable @ReadOnlyComposable
        get() = LocalAlterTypography.current
}
