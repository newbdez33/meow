package jp.jacky.meow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.jacky.meow.ui.MeowTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeowScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(state: MeowUiState, onPlay: (Int) -> Unit = {}) {
        rule.setContent {
            MeowTheme {
                MeowScreen(state = state, onPlay = onPlay, onShare = {}, onTip = {}, onPrivacyOptions = {})
            }
        }
    }

    @Test
    fun theGridReachesAll27CatsAndATapSelectsAndPlays() {
        val played = mutableListOf<Int>()
        show(MeowUiState(), onPlay = { played += it })

        rule.onNodeWithTag("grid").performScrollToNode(hasTestTag("cat-26"))
        rule.onNodeWithTag("cat-26").assertExists()

        rule.onNodeWithTag("grid").performScrollToNode(hasTestTag("cat-3"))
        rule.onNodeWithTag("cat-3").performClick()
        assertEquals(listOf(3), played)
        rule.onNodeWithTag("cat-3").assertIsSelected()

        rule.onNodeWithTag("cat-4").performClick()
        assertEquals(listOf(3, 4), played)
        rule.onNodeWithTag("cat-4").assertIsSelected()
        rule.onNodeWithTag("cat-3").assertIsNotSelected()
    }

    @Test
    fun theBannerLeadsTheGridAndScrollsWithIt() {
        rule.setContent {
            MeowTheme {
                MeowScreen(
                    state = MeowUiState(),
                    onPlay = {},
                    onShare = {},
                    onTip = {},
                    onPrivacyOptions = {},
                    banner = { Box(Modifier.fillMaxWidth().height(50.dp).testTag("banner")) },
                )
            }
        }

        val banner = rule.onNodeWithTag("banner").getBoundsInRoot()
        val firstCat = rule.onNodeWithTag("cat-0").getBoundsInRoot()
        assertTrue("banner $banner should sit above the first cat $firstCat", banner.bottom <= firstCat.top)

        rule.onNodeWithTag("grid").performScrollToNode(hasTestTag("cat-26"))
        rule.onNodeWithTag("banner").assertDoesNotExist()
    }

    @Test
    fun theCanShowsWhileAdsAreOn() {
        show(MeowUiState(isAdFree = false))
        rule.onNodeWithTag("tip").assertExists()
        rule.onNodeWithTag("share").assertExists()
    }

    @Test
    fun theCanIsGoneWhenAdFree() {
        show(MeowUiState(isAdFree = true))
        rule.onNodeWithTag("tip").assertDoesNotExist()
        rule.onNodeWithTag("share").assertExists()
    }

    @Test
    fun theHandAppearsOnlyWhenPrivacyOptionsAreRequired() {
        show(MeowUiState(isPrivacyOptionsRequired = true))
        rule.onNodeWithTag("privacy").assertExists()
    }

    @Test
    fun theHandIsAbsentByDefault() {
        show(MeowUiState())
        rule.onNodeWithTag("privacy").assertDoesNotExist()
    }
}
