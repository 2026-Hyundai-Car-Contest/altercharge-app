package com.altercharge.app.ui.card.driverecord

import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import org.junit.Assert.assertEquals
import org.junit.Test

class CardDriveRecordModeTest {

    @Test
    fun 폼팩터와_방향으로_mode를_고른다() {
        val cases = listOf(
            AppLayoutInfo(FormFactor.S17, isLandscape = true) to CardDriveRecordMode.LANDSCAPE,
            AppLayoutInfo(FormFactor.S17, isLandscape = false) to CardDriveRecordMode.PORTRAIT,
            // W-Standard는 S-17 모드를 쓴다
            AppLayoutInfo(FormFactor.W_STANDARD, isLandscape = true) to CardDriveRecordMode.LANDSCAPE,
            AppLayoutInfo(FormFactor.W_STANDARD, isLandscape = false) to CardDriveRecordMode.PORTRAIT,
            AppLayoutInfo(FormFactor.W_EXTENDED, isLandscape = true) to CardDriveRecordMode.LANDSCAPE_TALL,
            AppLayoutInfo(FormFactor.W_EXTENDED, isLandscape = false) to CardDriveRecordMode.PORTRAIT,
            AppLayoutInfo(FormFactor.WF, isLandscape = true) to CardDriveRecordMode.WF_LANDSCAPE,
            AppLayoutInfo(FormFactor.WF, isLandscape = false) to CardDriveRecordMode.WF_PORTRAIT,
        )
        cases.forEach { (info, expected) ->
            assertEquals(info.toString(), expected, cardDriveRecordModeFor(info))
        }
    }
}
