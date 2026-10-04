package jp.jacky.meow

import android.app.Activity
import android.content.Intent
import jp.jacky.meow.widget.WidgetConfigurationActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.jacky.meow.ads.AdsController
import jp.jacky.meow.ads.Banner
import jp.jacky.meow.ads.rememberBannerAdView
import jp.jacky.meow.billing.Store
import jp.jacky.meow.billing.TipSheet
import kotlinx.coroutines.launch

/**
 * The screen bound to the app's objects (ContentView.swift's .task): loads the store, then gathers
 * consent and starts the ads unless the user already owns a treat; re-reads the store on every
 * return to the foreground. [adsEnabled] is false only in the debug screenshot mode.
 */
@Composable
fun MeowApp(ads: AdsController, store: Store, sounds: SoundPlayer, activity: Activity, adsEnabled: Boolean = true) {
    val isAdFree by store.isAdFree.collectAsStateWithLifecycle()
    val canRequestAds by ads.canRequestAds.collectAsStateWithLifecycle()
    val isPrivacyOptionsRequired by ads.isPrivacyOptionsRequired.collectAsStateWithLifecycle()
    var showTip by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bannerAdView = rememberBannerAdView(BuildConfig.BANNER_AD_UNIT_ID, enabled = canRequestAds && !isAdFree)

    LaunchedEffect(Unit) {
        store.load()
        if (adsEnabled && !store.isAdFree.value) ads.start(activity)
    }
    LifecycleResumeEffect(Unit) {
        scope.launch { store.refresh() }
        onPauseOrDispose { }
    }

    MeowScreen(
        state = MeowUiState(isAdFree = isAdFree, isPrivacyOptionsRequired = isPrivacyOptionsRequired),
        onPlay = sounds::play,
        onShare = { shareApp(activity) },
        onTip = { showTip = true },
        onPrivacyOptions = { scope.launch { ads.presentPrivacyOptions(activity) } },
        onWidgets = { activity.startActivity(Intent(activity, WidgetConfigurationActivity::class.java)) },
        banner = { Banner(bannerAdView) },
    )
    if (showTip) {
        TipSheet(store = store, activity = activity, onDismiss = { showTip = false })
    }
}
