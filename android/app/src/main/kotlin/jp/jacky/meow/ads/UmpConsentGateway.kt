package jp.jacky.meow.ads

import android.app.Activity
import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** The User Messaging Platform and Mobile Ads SDKs behind the ConsentGateway interface. */
class UmpConsentGateway(
    private val app: Application,
    /** Debug builds pretend to be in the EEA so the consent form can be seen on any emulator. */
    private val debugGeographyEea: Boolean,
) : ConsentGateway {
    private val information: ConsentInformation
        get() = UserMessagingPlatform.getConsentInformation(app)

    override suspend fun requestConsentInfoUpdate(activity: Activity) {
        val params = ConsentRequestParameters.Builder().apply {
            if (debugGeographyEea) {
                setConsentDebugSettings(
                    ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        .build()
                )
            }
        }.build()
        suspendCancellableCoroutine<Unit> { continuation ->
            information.requestConsentInfoUpdate(
                activity,
                params,
                { continuation.resume(Unit) },
                { error -> continuation.resumeWithException(ConsentException(error.message)) },
            )
        }
    }

    override suspend fun loadAndShowConsentFormIfRequired(activity: Activity) {
        suspendCancellableCoroutine<Unit> { continuation ->
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                if (error == null) continuation.resume(Unit) else continuation.resumeWithException(ConsentException(error.message))
            }
        }
    }

    override suspend fun showPrivacyOptionsForm(activity: Activity) {
        suspendCancellableCoroutine<Unit> { continuation ->
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
                if (error == null) continuation.resume(Unit) else continuation.resumeWithException(ConsentException(error.message))
            }
        }
    }

    override val canRequestAds: Boolean
        get() = information.canRequestAds()

    override val isPrivacyOptionsRequired: Boolean
        get() = information.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /** Google asks for initialization off the main thread. */
    override suspend fun initializeSdk() {
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<Unit> { continuation ->
                MobileAds.initialize(app) { continuation.resume(Unit) }
            }
        }
    }
}
