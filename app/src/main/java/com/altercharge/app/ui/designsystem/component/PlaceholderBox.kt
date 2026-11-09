package com.altercharge.app.ui.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * 아직 구현 전인 카드 자리를 표시하는 임시 상자. 미리보기와 개발 중 화면 확인용이다.
 * 카드가 다 만들어지면 지운다.
 */
@Composable
fun PlaceholderBox(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, AlterTheme.colors.surfaceAccent, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(label, color = AlterTheme.colors.textTertiary)
    }
}
