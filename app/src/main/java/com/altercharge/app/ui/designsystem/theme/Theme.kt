package com.altercharge.app.ui.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 앱 전체 테마. 화면과 카드는 모두 이 테마 안에서 그린다.
 *
 * 지금은 뼈대다. 색 · 타이포 · 여백 · 그림자 값은 다음 작업에서 Figma Style Guide 값으로 채운다.
 * - Colors 103:46834 · Typography 103:46833 · Size, space & Radius 103:46835 · Effects 103:46836
 */
@Composable
fun AlterChargeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}
