package com.altercharge.app.ui.card.info

import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import org.junit.Assert.assertEquals
import org.junit.Test

class CardInfoModeTest {

    @Test
    fun 폼팩터와_방향으로_mode를_고른다() {
        val cases = listOf(
            AppLayoutInfo(FormFactor.S17, isLandscape = true) to CardInfoMode.LANDSCAPE,
            AppLayoutInfo(FormFactor.S17, isLandscape = false) to CardInfoMode.PORTRAIT,
            // W-Standard · W-Extended는 S-17 모드를 쓴다
            AppLayoutInfo(FormFactor.W_STANDARD, isLandscape = true) to CardInfoMode.LANDSCAPE,
            AppLayoutInfo(FormFactor.W_STANDARD, isLandscape = false) to CardInfoMode.PORTRAIT,
            AppLayoutInfo(FormFactor.W_EXTENDED, isLandscape = true) to CardInfoMode.LANDSCAPE,
            AppLayoutInfo(FormFactor.W_EXTENDED, isLandscape = false) to CardInfoMode.PORTRAIT,
            AppLayoutInfo(FormFactor.WF, isLandscape = true) to CardInfoMode.WF_LANDSCAPE,
            AppLayoutInfo(FormFactor.WF, isLandscape = false) to CardInfoMode.WF_PORTRAIT,
        )
        cases.forEach { (info, expected) ->
            assertEquals(info.toString(), expected, cardInfoModeFor(info))
        }
    }
}
