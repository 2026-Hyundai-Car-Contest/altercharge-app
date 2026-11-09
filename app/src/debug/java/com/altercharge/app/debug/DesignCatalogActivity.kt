package com.altercharge.app.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.altercharge.app.ui.designsystem.component.PlaceholderBox
import com.altercharge.app.ui.designsystem.layout.AlterChargeLayout
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * 디자인 시스템 확인용 화면. 디버그 빌드에만 들어간다.
 *
 *   adb shell am start --display 0 -n com.altercharge.app/.debug.DesignCatalogActivity \
 *     --es page buttons --ez dark false
 *
 * page: tokens(디자인 값) · buttons(버튼) · layout(레이아웃 틀)
 */
class DesignCatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = intent.getStringExtra("page") ?: "tokens"
        val dark = intent.getBooleanExtra("dark", false)
        setContent {
            AlterChargeTheme(darkTheme = dark) {
                Box(Modifier.fillMaxSize().background(AlterTheme.colors.background)) {
                    when (page) {
                        "buttons" -> IviButtonCatalog(
                            Modifier.background(AlterTheme.colors.surface).padding(AlterPadding.XLarge),
                        )
                        "layout" -> AlterChargeLayout(
                            statusBar = { PlaceholderBox("status_bar", Modifier.height(56.dp)) },
                            l1 = { PlaceholderBox("L1 판정", Modifier.height(368.dp)) },
                            l2 = { PlaceholderBox("L2 주행 기록") },
                            c = { PlaceholderBox("C 차량 상태") },
                            r1 = { PlaceholderBox("R1 추천 충전소", Modifier.height(368.dp)) },
                            r2 = { PlaceholderBox("R2 충전소 목록") },
                        )
                        else -> DesignTokensSheet()
                    }
                }
            }
        }
    }
}
