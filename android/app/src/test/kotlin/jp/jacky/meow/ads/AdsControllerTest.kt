package jp.jacky.meow.ads

import android.app.Activity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdsControllerTest {
    private val activity = Activity()

    @Test
    fun consentGrantedStartsTheSdkOnceAndAllowsAds() = runTest {
        val consent = FakeConsentGateway()
        val ads = AdsController(consent)
        ads.start(activity)
        ads.start(activity)
        assertTrue(ads.canRequestAds.value)
        assertEquals(1, consent.initializations)
        assertEquals(2, consent.forms)
    }

    @Test
    fun declinedConsentShowsNoBannerAndStartsNothing() = runTest {
        val consent = FakeConsentGateway().apply { canRequestAdsAfterUpdate = false }
        val ads = AdsController(consent)
        ads.start(activity)
        assertFalse(ads.canRequestAds.value)
        assertEquals(0, consent.initializations)
    }

    @Test
    fun failedUpdateKeepsThePreviousState() = runTest {
        // An earlier launch allowed ads; this launch is offline.
        val allowed = FakeConsentGateway().apply { canRequestAds = true; updateFails = true }
        val ads = AdsController(allowed)
        ads.start(activity)
        assertTrue(ads.canRequestAds.value)
        assertEquals(1, allowed.initializations)
        assertEquals(0, allowed.forms)

        val never = FakeConsentGateway().apply { canRequestAds = false; updateFails = true }
        val fresh = AdsController(never)
        fresh.start(activity)
        assertFalse(fresh.canRequestAds.value)
        assertEquals(0, never.initializations)
    }

    @Test
    fun aFailedFormStillPublishesTheConsentState() = runTest {
        val consent = FakeConsentGateway().apply { formFails = true }
        val ads = AdsController(consent)
        ads.start(activity)
        assertTrue(ads.canRequestAds.value)
    }

    @Test
    fun requiredPrivacyOptionsShowTheHand() = runTest {
        val consent = FakeConsentGateway().apply { privacyOptionsRequiredAfterUpdate = true }
        val ads = AdsController(consent)
        ads.start(activity)
        assertTrue(ads.isPrivacyOptionsRequired.value)
    }

    @Test
    fun secondStartWhileOneRunsIsIgnored() = runTest {
        val hold = CompletableDeferred<Unit>()
        val consent = FakeConsentGateway().apply { holdUpdate = hold }
        val ads = AdsController(consent)
        launch { ads.start(activity) }
        launch { ads.start(activity) }
        advanceUntilIdle()
        assertEquals(1, consent.updates)
        hold.complete(Unit)
        advanceUntilIdle()
        assertTrue(ads.canRequestAds.value)
        assertEquals(1, consent.initializations)
    }

    @Test
    fun thePrivacyOptionsFormCanTurnAdsOn() = runTest {
        val consent = FakeConsentGateway().apply {
            canRequestAdsAfterUpdate = false
            privacyOptionsRequiredAfterUpdate = true
            canRequestAdsAfterPrivacyForm = true
        }
        val ads = AdsController(consent)
        ads.start(activity)
        assertFalse(ads.canRequestAds.value)
        ads.presentPrivacyOptions(activity)
        assertTrue(ads.canRequestAds.value)
        assertEquals(1, consent.privacyForms)
        assertEquals(1, consent.initializations)
    }

    @Test
    fun aFailedPrivacyOptionsFormChangesNothing() = runTest {
        val consent = FakeConsentGateway().apply { privacyFormFails = true }
        val ads = AdsController(consent)
        ads.start(activity)
        ads.presentPrivacyOptions(activity)
        assertTrue(ads.canRequestAds.value)
        assertEquals(1, consent.initializations)
    }
}
