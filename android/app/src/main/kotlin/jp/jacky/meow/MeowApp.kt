package jp.jacky.meow

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.jacky.meow.billing.Store
import jp.jacky.meow.billing.TipSheet
import kotlinx.coroutines.launch

/** The screen bound to the app's objects: loads the store at launch and re-reads it on every return to the foreground. */
@Composable
fun MeowApp(store: Store, sounds: SoundPlayer, activity: Activity) {
    val isAdFree by store.isAdFree.collectAsStateWithLifecycle()
    var showTip by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        store.load()
    }
    LifecycleResumeEffect(Unit) {
        scope.launch { store.refresh() }
        onPauseOrDispose { }
    }

    MeowScreen(
        state = MeowUiState(isAdFree = isAdFree),
        onPlay = sounds::play,
        onShare = { shareApp(activity) },
        onTip = { showTip = true },
        onPrivacyOptions = {},
    )
    if (showTip) {
        TipSheet(store = store, activity = activity, onDismiss = { showTip = false })
    }
}
