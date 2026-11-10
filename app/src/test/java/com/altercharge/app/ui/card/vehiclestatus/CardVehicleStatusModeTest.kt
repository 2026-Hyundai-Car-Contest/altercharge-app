package com.altercharge.app.ui.card.vehiclestatus

import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import org.junit.Assert.assertEquals
import org.junit.Test

class CardVehicleStatusModeTest {

    @Test
    fun 폼팩터와_방향으로_mode를_고른다() {
        val cases = listOf(
            AppLayoutInfo(FormFactor.S17, isLandscape = true) to CardVehicleStatusMode.LANDSCAPE,
            AppLayoutInfo(FormFactor.S17, isLandscape = false) to CardVehicleStatusMode.PORTRAIT,
            AppLayoutInfo(FormFactor.W_EXTENDED, isLandscape = true) to CardVehicleStatusMode.LANDSCAPE_TALL,
            AppLayoutInfo(FormFactor.W_EXTENDED, isLandscape = false) to CardVehicleStatusMode.PORTRAIT_TALL,
            AppLayoutInfo(FormFactor.WF, isLandscape = true) to CardVehicleStatusMode.WF_LANDSCAPE,
            AppLayoutInfo(FormFactor.WF, isLandscape = false) to CardVehicleStatusMode.WF_PORTRAIT,
            // W-Standard는 시안이 없어 S-17 모드로 대신한다
            AppLayoutInfo(FormFactor.W_STANDARD, isLandscape = true) to CardVehicleStatusMode.LANDSCAPE,
            AppLayoutInfo(FormFactor.W_STANDARD, isLandscape = false) to CardVehicleStatusMode.PORTRAIT,
        )
        cases.forEach { (info, expected) ->
            assertEquals(info.toString(), expected, cardVehicleStatusModeFor(info))
        }
    }
}
