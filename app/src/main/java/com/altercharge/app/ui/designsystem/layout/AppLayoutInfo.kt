package com.altercharge.app.ui.designsystem.layout

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

/** AlterChargeLayout이 계산해 [LocalAppLayout]으로 내려 준다. 카드는 이 값으로 mode를 고른다 */
@Immutable
data class AppLayoutInfo(
    val formFactor: FormFactor,
    val isLandscape: Boolean,
)

/** 틀 밖(미리보기 등)에서는 기본값 S-17 가로 */
val LocalAppLayout = compositionLocalOf { AppLayoutInfo(FormFactor.S17, isLandscape = true) }
