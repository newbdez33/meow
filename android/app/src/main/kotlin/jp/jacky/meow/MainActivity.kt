package jp.jacky.meow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import jp.jacky.meow.billing.PreviewBillingGateway
import jp.jacky.meow.billing.RsaPurchaseVerifier
import jp.jacky.meow.billing.Store
import jp.jacky.meow.ui.MeowTheme

class MainActivity : ComponentActivity() {
    private lateinit var sounds: SoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sounds = MediaPlayerSoundPlayer(this)
        val app = application as MeowApplication
        // Debug-only screenshot mode, the Android form of the iOS "-MeowNoAds" launch argument:
        //   adb shell am start -n jp.jacky.meow/.MainActivity --ez meowNoAds true --es meowStore preview
        val noAds = BuildConfig.DEBUG && intent.getBooleanExtra("meowNoAds", false)
        val store = if (BuildConfig.DEBUG && intent.getStringExtra("meowStore") == "preview") {
            Store(PreviewBillingGateway(), RsaPurchaseVerifier(""), lifecycleScope)
        } else {
            app.store
        }
        setContent {
            MeowTheme {
                MeowApp(ads = app.ads, store = store, sounds = sounds, activity = this, adsEnabled = !noAds)
            }
        }
    }

    override fun onDestroy() {
        sounds.release()
        super.onDestroy()
    }
}
