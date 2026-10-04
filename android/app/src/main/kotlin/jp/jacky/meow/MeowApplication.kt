package jp.jacky.meow

import android.app.Application
import jp.jacky.meow.ads.AdsController
import jp.jacky.meow.ads.UmpConsentGateway
import jp.jacky.meow.billing.PlayBillingGateway
import jp.jacky.meow.billing.RsaPurchaseVerifier
import jp.jacky.meow.billing.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Owns shared playback and lazily creates the store and ads controller. */
class MeowApplication : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val audio by lazy { AudioPlayback(this) }
    val store by lazy { Store(PlayBillingGateway(this), RsaPurchaseVerifier(BuildConfig.PLAY_LICENSE_KEY), scope) }
    val ads by lazy { AdsController(UmpConsentGateway(this, BuildConfig.CONSENT_DEBUG_EEA)) }
}
