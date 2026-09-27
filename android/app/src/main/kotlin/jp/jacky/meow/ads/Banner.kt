package jp.jacky.meow.ads

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * A 320x50 banner (the iOS size), centred, that requests its ad once when it enters the tree and is
 * absent from the tree while [visible] is false, so the grid takes the full height.
 */
@Composable
fun Banner(adUnitId: String, visible: Boolean) {
    if (!visible) return
    val lifecycleOwner = LocalLifecycleOwner.current
    var adView by remember { mutableStateOf<AdView?>(null) }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView?.resume()
                Lifecycle.Event.ON_PAUSE -> adView?.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("banner"),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.width(320.dp).height(50.dp),
            factory = { context ->
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
                    adView = this
                }
            },
            onRelease = { it.destroy() },
        )
    }
}

private const val TAG = "MeowAds"
