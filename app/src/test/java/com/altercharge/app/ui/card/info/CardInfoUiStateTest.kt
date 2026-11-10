package com.altercharge.app.ui.card.info

import com.altercharge.app.ui.card.common.ChargeVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

class CardInfoUiStateTest {

    private fun status(verdict: ChargeVerdict, days: Int) =
        CardInfoUiState(verdict, daysUntilCharge = days, placeName = "예시 아파트").toStatus()

    @Test
    fun 오늘() {
        assertEquals(CardInfoStatus.TODAY, status(ChargeVerdict.TODAY, 0))
    }

    @Test
    fun 하루_뒤는_내일() {
        assertEquals(CardInfoStatus.TOMORROW, status(ChargeVerdict.WITHIN_TWO_DAYS, 1))
    }

    @Test
    fun 이틀_뒤는_3일내() {
        assertEquals(CardInfoStatus.WITHIN_THREE_DAYS, status(ChargeVerdict.WITHIN_TWO_DAYS, 2))
    }

    @Test
    fun 사흘_이상은_3일내로_대신() {
        assertEquals(CardInfoStatus.WITHIN_THREE_DAYS, status(ChargeVerdict.THREE_DAYS_OR_MORE, 3))
    }
}
