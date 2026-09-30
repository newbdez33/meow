package jp.jacky.meow.ads

import android.app.Activity

class ConsentException(message: String?) : Exception(message)

/** The slice of the UMP SDK and the Mobile Ads SDK the AdsController uses. UmpConsentGateway is the real one. */
interface ConsentGateway {
    /** Refreshes the consent state from Google. Throws ConsentException when Google cannot be reached. */
    suspend fun requestConsentInfoUpdate(activity: Activity)

    /** Shows the consent form when regulations require one and returns once it is dismissed. */
    suspend fun loadAndShowConsentFormIfRequired(activity: Activity)

    /** Shows the form that lets the user change an earlier choice. */
    suspend fun showPrivacyOptionsForm(activity: Activity)

    val canRequestAds: Boolean

    val isPrivacyOptionsRequired: Boolean

    /** Starts the Mobile Ads SDK. The controller calls it at most once per process. */
    suspend fun initializeSdk()
}
