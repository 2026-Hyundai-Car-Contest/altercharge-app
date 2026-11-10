package com.altercharge.app.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterPadding
import com.altercharge.app.ui.designsystem.theme.AlterTheme

/**
 * 디자인 시스템 확인용 화면. 디버그 빌드에만 들어간다.
 *
 *   adb shell am start --display 0 -n com.altercharge.app/.debug.DesignCatalogActivity \
 *     --es page buttons --ez dark false
 *
 * page: tokens(디자인 값) · buttons(버튼) · statusbar(상단 바)
 *   · layout(레이아웃 틀 + L1 · L2 · C 실제 카드, R1 · R2는 자리 표시)
 *   · cv(C 카드 상황 01~12 가로) · cards(L1 · L2 · C 모드별)
 */
class DesignCatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = intent.getStringExtra("page") ?: "tokens"
        val dark = intent.getBooleanExtra("dark", false)
        setContent {
            AlterChargeTheme(darkTheme = dark) {
                Box(Modifier.fillMaxSize().background(AlterTheme.colors.screenBackground)) {
                    when (page) {
                        "buttons" -> IviButtonCatalog(
                            Modifier.background(AlterTheme.colors.surface).padding(AlterPadding.XLarge),
                        )
                        "statusbar" -> StatusBarCatalog(Modifier.padding(AlterPadding.XLarge))
                        "layout" -> LayoutCatalog()
                        "cv" -> VehicleStatusCatalog()
                        "cards" -> CardsCatalog()
                        else -> DesignTokensSheet()
                    }
                }
            }
        }
    }
}
