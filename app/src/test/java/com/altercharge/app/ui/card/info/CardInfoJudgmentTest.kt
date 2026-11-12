package com.altercharge.app.ui.card.info

import com.altercharge.app.domain.judgment.Judgment
import com.altercharge.app.domain.judgment.JudgmentResult
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
    fun 이틀은_N일내() {
        val state = card(2)
        assertEquals(ChargeVerdict.WITHIN_TWO_DAYS, state.verdict)
        assertEquals(CardInfoStatus.WITHIN_N_DAYS, state.toStatus())
    }

    @Test
    fun 사흘_이상은_3일이상_단계이고_화면은_N일내() {
        listOf(3, 10, 99).forEach { days ->
            val state = card(days)
            assertEquals("days=$days", ChargeVerdict.THREE_DAYS_OR_MORE, state.verdict)
            assertEquals("days=$days", days, state.daysUntilCharge)
            assertEquals("days=$days", CardInfoStatus.WITHIN_N_DAYS, state.toStatus())
        }
    }

    @Test
    fun 장소_이름은_그대로() {
        assertEquals("예시 아파트", card(0).placeName)
    }

    // ---- 기록 수집 중 ----

    @Test
    fun 수집_중_결과는_수집_중_카드로_바뀐다() {
        val state = JudgmentResult.Collecting(recordedDays = 3, chargeRecommended = false).toCardInfoUiState("예시 아파트")
        assertEquals(3, state.recordedDays)
        assertEquals(CardInfoStatus.COLLECTING, state.toStatus())
    }

    @Test
    fun 준비된_결과는_기존_판정_카드() {
        val ready = JudgmentResult.Ready(judgment(daysLeft = 1)).toCardInfoUiState("예시 아파트")
        assertEquals(null, ready.recordedDays)
        assertEquals(CardInfoStatus.TOMORROW, ready.toStatus())
    }

    @Test
    fun 수집_일수는_0에서_13으로_맞춘다() {
        assertEquals(0, CardInfoUiState.collecting(-3).recordedDays)
        assertEquals(13, CardInfoUiState.collecting(20).recordedDays)
    }

    @Test
    fun 진행_막대_폭은_시안_값과_같다() {
        assertEquals(8, collectingFillWidthDp(0)) // 첫날 최소 8
        assertEquals(16, collectingFillWidthDp(1))
        assertEquals(47, collectingFillWidthDp(3))
        assertEquals(204, collectingFillWidthDp(13))
    }
}
