package jp.jacky.meow.ads

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * One 320x50 banner (the iOS size) per screen: created and loaded once [enabled] turns true,
 * destroyed when it turns false or the screen goes away. It outlives the grid row that shows it, so
 * scrolling that row off and back on does not request another ad. Null while ads are off.
 */
@Composable
fun rememberBannerAdView(adUnitId: String, enabled: Boolean): AdView? {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val adView = remember(enabled) {
        if (!enabled) {
            null
        } else {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "Banner loaded")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.d(TAG, "Banner failed: ${error.message}")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView?.resume()
                Lifecycle.Event.ON_PAUSE -> adView?.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView?.destroy()
        }
    }
    return adView
}

/** The banner's row, centred; absent from the tree while [adView] is null, so the grid starts with the cats. */
@Composable
fun Banner(adView: AdView?) {
    if (adView == null) return
    Box(
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("banner"),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.width(320.dp).height(50.dp),
            // The same AdView moves between rows as the grid recomposes; a View can have one parent.
            factory = { (adView.parent as? ViewGroup)?.removeView(adView); adView },
            onRelease = { (it.parent as? ViewGroup)?.removeView(it) },
        )
    }
}

private const val TAG = "MeowAds"
