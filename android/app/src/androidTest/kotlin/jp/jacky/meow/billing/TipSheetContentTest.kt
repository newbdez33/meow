package jp.jacky.meow.billing

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.jacky.meow.ui.MeowTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TipSheetContentTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(state: TipUiState, onBuyCan: () -> Unit = {}, onCatTap: () -> Unit = {}) {
        rule.setContent {
            MeowTheme {
                TipSheetContent(state, catIndex = 0, onCatTap = onCatTap, onBuyCan = onBuyCan, onBuyCoffee = {}, onRestore = {})
            }
        }
    }

    @Test
    fun offersBothTreatsWithTheirPricesAndTheCanFirst() {
        var canTaps = 0
        show(TipUiState(canPrice = "¥900", coffeePrice = "¥450"), onBuyCan = { canTaps++ })
        rule.onNodeWithTag("buy-can").assertTextContains("¥900", substring = true).performClick()
        rule.onNodeWithTag("buy-coffee").assertTextContains("¥450", substring = true)
        rule.onNodeWithTag("thanks").assertDoesNotExist()
        rule.onNodeWithTag("unavailable").assertDoesNotExist()
        assertEquals(1, canTaps)
    }

    @Test
    fun thanksReplacesTheButtonsWhenAdFree() {
        show(TipUiState(isAdFree = true, canPrice = "¥900", coffeePrice = "¥450"))
        rule.onNodeWithTag("thanks").assertExists()
        rule.onNodeWithTag("buy-can").assertDoesNotExist()
        rule.onNodeWithTag("buy-coffee").assertDoesNotExist()
    }

    @Test
    fun showsUnavailableWhenPlayHasNoProducts() {
        show(TipUiState())
        rule.onNodeWithTag("unavailable").assertExists()
        rule.onNodeWithTag("buy-can").assertDoesNotExist()
    }

    @Test
    fun disablesTheButtonsWhilePurchasingAndShowsAFailure() {
        show(TipUiState(canPrice = "¥900", coffeePrice = "¥450", isPurchasing = true, didFail = true))
        rule.onNodeWithTag("buy-can").assertIsNotEnabled()
        rule.onNodeWithTag("buy-coffee").assertIsNotEnabled()
        rule.onNodeWithTag("failed").assertExists()
    }

    @Test
    fun tappingTheHeroCatAsksForTheNextOne() {
        var taps = 0
        show(TipUiState(canPrice = "¥900"), onCatTap = { taps++ })
        rule.onNodeWithTag("hero-cat").performClick()
        assertEquals(1, taps)
    }
}
