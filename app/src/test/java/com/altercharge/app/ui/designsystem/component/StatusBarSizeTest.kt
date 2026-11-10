package com.altercharge.app.ui.designsystem.component

import com.altercharge.app.ui.designsystem.layout.AppLayoutInfo
import com.altercharge.app.ui.designsystem.layout.FormFactor
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusBarSizeTest {

    @Test
    fun WF_세로만_COMPACT이고_나머지는_REGULAR다() {
        FormFactor.entries.forEach { formFactor ->
            listOf(true, false).forEach { isLandscape ->
                val expected = if (formFactor == FormFactor.WF && !isLandscape) StatusBarSize.COMPACT else StatusBarSize.REGULAR
                assertEquals(
                    "$formFactor landscape=$isLandscape",
                    expected,
                    statusBarSizeFor(AppLayoutInfo(formFactor, isLandscape)),
                )
            }
        }
    }
}
