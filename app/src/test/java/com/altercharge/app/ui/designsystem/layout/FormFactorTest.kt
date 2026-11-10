package com.altercharge.app.ui.designsystem.layout

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class FormFactorTest {

    @Test
    fun 시안_크기는_자기_폼팩터로_고른다() {
        val cases = listOf(
            Triple(1700, 1184, FormFactor.S17),
            Triple(840, 1184, FormFactor.S17),
            Triple(1640, 810, FormFactor.W_STANDARD),
            Triple(800, 810, FormFactor.W_STANDARD),
            Triple(1640, 1400, FormFactor.W_EXTENDED),
            Triple(800, 1400, FormFactor.W_EXTENDED),
            Triple(1256, 800, FormFactor.WF),
            Triple(648, 800, FormFactor.WF),
        )
        cases.forEach { (width, height, expected) ->
            assertEquals("$width x $height", expected, FormFactor.closestTo(width.dp, height.dp))
        }
    }

    @Test
    fun 시안과_조금_다른_크기는_가장_가까운_폼팩터로_고른다() {
        // Pleos 에뮬레이터 앱 영역 (S-17 가로와 조금 다르다)
        assertEquals(FormFactor.S17, FormFactor.closestTo(1682.dp, 1210.dp))
    }

    @Test
    fun 가로와_세로가_같으면_가로로_본다() {
        // 800 x 800: 가로 후보 중 WF(1256 x 800)가 가장 가깝다
        assertEquals(FormFactor.WF, FormFactor.closestTo(800.dp, 800.dp))
    }
}
