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

/** Owns the app-wide objects: the store and the ads controller. */
class MeowApplication : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    lateinit var store: Store
        private set

    lateinit var ads: AdsController
        private set

    override fun onCreate() {
        super.onCreate()
        store = Store(PlayBillingGateway(this), RsaPurchaseVerifier(BuildConfig.PLAY_LICENSE_KEY), scope)
        ads = AdsController(UmpConsentGateway(this, BuildConfig.CONSENT_DEBUG_EEA))
    }
}
