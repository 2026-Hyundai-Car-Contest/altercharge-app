package com.altercharge.app.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import com.altercharge.app.ui.designsystem.component.IviButton
import com.altercharge.app.ui.designsystem.component.IviButtonHierarchy
import com.altercharge.app.ui.designsystem.component.IviButtonSize
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterMargin
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/** 버튼 전체 모음. 미리보기와 디버그 확인 화면에서 같이 쓴다. 디버그 빌드에만 들어간다. */
@Composable
fun IviButtonCatalog(modifier: Modifier = Modifier) {
    val label = "여기로 안내"
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AlterMargin.Large)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AlterMargin.Normal)) {
            IviButtonHierarchy.entries.forEach {
                IviButton(label, onClick = {}, hierarchy = it, size = IviButtonSize.Large)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AlterMargin.Normal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IviButtonSize.entries.forEach { IviButton(label, onClick = {}, size = it) }
            IviButton(label, onClick = {}, enabled = false)
            IviButton(label, onClick = {}, restricted = true)
            IviButton(label, onClick = {}, hierarchy = IviButtonHierarchy.Ghost, enabled = false)
            IviButton(null, onClick = {}, hierarchy = IviButtonHierarchy.Ghost, size = IviButtonSize.Small, contentDescription = "닫기", leadingIcon = rememberVectorPainter(Icons.Filled.Close))
        }
    }
}

@Preview(name = "IviButton · 라이트", widthDp = 1400)
@Composable
private fun IviButtonLightPreview() {
    AlterChargeTheme(darkTheme = false) {
        IviButtonCatalog(Modifier.background(AlterTheme.colors.surface).padding(AlterPadding.XLarge))
    }
}

@Preview(name = "IviButton · 다크", widthDp = 1400)
@Composable
private fun IviButtonDarkPreview() {
    AlterChargeTheme(darkTheme = true) {
        IviButtonCatalog(Modifier.background(AlterTheme.colors.surface).padding(AlterPadding.XLarge))
    }
}
