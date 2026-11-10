package com.altercharge.app.ui.card.info

import com.altercharge.app.domain.judgment.Judgment
import com.altercharge.app.ui.card.common.ChargeVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

class CardInfoJudgmentTest {

    private fun judgment(daysLeft: Int) =
        Judgment(averageDailyKm = 40, reserveKm = 40, daysLeft = daysLeft, chargeRecommended = daysLeft <= 1)

    private fun card(daysLeft: Int) = judgment(daysLeft).toCardInfoUiState(placeName = "예시 아파트")

    @Test
    fun 영일은_오늘() {
        val state = card(0)
        assertEquals(ChargeVerdict.TODAY, state.verdict)
        assertEquals(0, state.daysUntilCharge)
        assertEquals(CardInfoStatus.TODAY, state.toStatus())
    }

    @Test
    fun 하루는_내일() {
        val state = card(1)
        assertEquals(ChargeVerdict.WITHIN_TWO_DAYS, state.verdict)
        assertEquals(CardInfoStatus.TOMORROW, state.toStatus())
    }

    @Test
    fun 이틀은_3일내() {
        val state = card(2)
        assertEquals(ChargeVerdict.WITHIN_TWO_DAYS, state.verdict)
        assertEquals(CardInfoStatus.WITHIN_THREE_DAYS, state.toStatus())
    }

    @Test
    fun 사흘_이상은_3일이상_단계이고_화면은_3일내() {
        listOf(3, 10, 99).forEach { days ->
            val state = card(days)
            assertEquals("days=$days", ChargeVerdict.THREE_DAYS_OR_MORE, state.verdict)
            assertEquals("days=$days", days, state.daysUntilCharge)
            assertEquals("days=$days", CardInfoStatus.WITHIN_THREE_DAYS, state.toStatus())
        }
    }

    @Test
    fun 장소_이름은_그대로() {
        assertEquals("예시 아파트", card(0).placeName)
    }
}
