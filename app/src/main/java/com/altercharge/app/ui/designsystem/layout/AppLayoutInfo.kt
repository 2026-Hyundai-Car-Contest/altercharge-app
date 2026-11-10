package com.altercharge.app.ui.designsystem.layout

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

/**
 * 지금 앱 영역의 폼팩터와 방향. AlterChargeLayout이 계산해 슬롯에 [LocalAppLayout]으로 내려 준다.
 * 카드는 이 값으로 자기 mode를 고른다 (예: cardInfoModeFor(LocalAppLayout.current)).
 */
@Immutable
data class AppLayoutInfo(
    val formFactor: FormFactor,
    val isLandscape: Boolean,
)

/**
 * AlterChargeLayout 슬롯 안에서 읽는 레이아웃 정보.
 * 틀 밖(미리보기 등)에서는 기본값 S-17 가로를 돌려준다.
 */
val LocalAppLayout = compositionLocalOf { AppLayoutInfo(FormFactor.S17, isLandscape = true) }
