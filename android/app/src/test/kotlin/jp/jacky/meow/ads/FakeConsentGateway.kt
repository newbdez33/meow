package jp.jacky.meow.ads

import android.app.Activity
import kotlinx.coroutines.CompletableDeferred

/** Google's consent machinery as the tests want it. */
class FakeConsentGateway : ConsentGateway {
    var updateFails = false
    var formFails = false
    var privacyFormFails = false

    /** What the consent state becomes after a successful update. */
    var canRequestAdsAfterUpdate = true
    var privacyOptionsRequiredAfterUpdate = false

    /** What the consent state becomes after the privacy options form. */
    var canRequestAdsAfterPrivacyForm = true

    /** When set, updates do not return until it completes. */
    var holdUpdate: CompletableDeferred<Unit>? = null

    override var canRequestAds = false
    override var isPrivacyOptionsRequired = false

    var updates = 0
    var forms = 0
    var privacyForms = 0
    var initializations = 0

    override suspend fun requestConsentInfoUpdate(activity: Activity) {
        updates++
        holdUpdate?.await()
        if (updateFails) throw ConsentException("offline")
        canRequestAds = canRequestAdsAfterUpdate
        isPrivacyOptionsRequired = privacyOptionsRequiredAfterUpdate
    }

    override suspend fun loadAndShowConsentFormIfRequired(activity: Activity) {
        forms++
        if (formFails) throw ConsentException("form")
    }

    override suspend fun showPrivacyOptionsForm(activity: Activity) {
        privacyForms++
        if (privacyFormFails) throw ConsentException("form")
        canRequestAds = canRequestAdsAfterPrivacyForm
    }

    override suspend fun initializeSdk() {
        initializations++
    }
}
