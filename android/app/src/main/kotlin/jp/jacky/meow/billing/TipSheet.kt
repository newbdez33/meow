package jp.jacky.meow.billing

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.jacky.meow.R
import jp.jacky.meow.ui.Coral
import kotlinx.coroutines.launch

/** Everything the sheet shows, so it can be tested without a Store. */
data class TipUiState(
    val isAdFree: Boolean = false,
    val canPrice: String? = null,
    val coffeePrice: String? = null,
    val isPurchasing: Boolean = false,
    val didFail: Boolean = false,
)

/** The five cats that greet the sheet (black, gray, orange, tabby, orange with a bowl), taking turns. */
object TipCats {
    val all = listOf(R.drawable.c02, R.drawable.c01, R.drawable.c04, R.drawable.c19, R.drawable.c21)
    private var last = -1

    /** A different cat than the last time the sheet opened. */
    fun next(): Int {
        last = (last + 1) % all.size
        return last
    }
}

/** The sheet's content (TipSheet.swift): hero, title, paragraph, the can, the coffee, restore. */
@Composable
fun TipSheetContent(
    state: TipUiState,
    catIndex: Int,
    onCatTap: () -> Unit,
    onBuyCan: () -> Unit,
    onBuyCoffee: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
            .padding(top = 8.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        TipHero(cat = TipCats.all[catIndex], onTap = onCatTap)
        Text(
            text = stringResource(R.string.tip_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.tip_body),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.isAdFree) {
            Text(
                text = stringResource(R.string.tip_thanks),
                style = MaterialTheme.typography.titleMedium,
                color = Coral,
                modifier = Modifier.testTag("thanks"),
            )
        } else {
            state.canPrice?.let { price ->
                Button(
                    onClick = onBuyCan,
                    enabled = !state.isPurchasing,
                    colors = ButtonDefaults.buttonColors(containerColor = Coral),
                    modifier = Modifier.fillMaxWidth().testTag("buy-can"),
                ) {
                    TreatLabel(
                        icon = R.drawable.tip_can,
                        title = stringResource(R.string.tip_can, price),
                        note = stringResource(R.string.tip_can_note),
                    )
                }
            }
            state.coffeePrice?.let { price ->
                OutlinedButton(
                    onClick = onBuyCoffee,
                    enabled = !state.isPurchasing,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Coral),
                    modifier = Modifier.fillMaxWidth().testTag("buy-coffee"),
                ) {
                    TreatLabel(icon = R.drawable.tip_mug, title = stringResource(R.string.tip_coffee, price), note = null)
                }
            }
            if (state.canPrice == null && state.coffeePrice == null) {
                Text(
                    text = stringResource(R.string.tip_unavailable),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("unavailable"),
                )
            }
            TextButton(onClick = onRestore, enabled = !state.isPurchasing) {
                Text(stringResource(R.string.tip_restore), color = Coral)
            }
        }
        if (state.didFail) {
            Text(
                text = stringResource(R.string.tip_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("failed"),
            )
        }
    }
}

/** The mug, a cat and the can, composed at runtime like the iOS TipHero. */
@Composable
private fun TipHero(cat: Int, onTap: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.BottomCenter) {
        Image(
            painter = painterResource(R.drawable.tip_mug),
            contentDescription = null,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp).size(64.dp),
        )
        Image(
            painter = painterResource(cat),
            contentDescription = null,
            modifier = Modifier.size(140.dp).clickable(onClick = onTap).testTag("hero-cat"),
        )
        Image(
            painter = painterResource(R.drawable.tip_can),
            contentDescription = null,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp).size(64.dp),
        )
    }
}

@Composable
private fun TreatLabel(icon: Int, title: String, note: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(32.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (note != null) Text(note, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** The sheet bound to the Store: opens as a Material bottom sheet, buys, restores, and reports failure. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipSheet(store: Store, activity: Activity, onDismiss: () -> Unit) {
    val isAdFree by store.isAdFree.collectAsStateWithLifecycle()
    val can by store.can.collectAsStateWithLifecycle()
    val coffee by store.coffee.collectAsStateWithLifecycle()
    var catIndex by rememberSaveable { mutableIntStateOf(TipCats.next()) }
    var isPurchasing by remember { mutableStateOf(false) }
    var didFail by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun buy(treat: Treat) {
        scope.launch {
            isPurchasing = true
            didFail = false
            didFail = store.purchase(treat, activity) == PurchaseResult.Failed
            isPurchasing = false
        }
    }

    // Fully expanded from the start, so both treats and the restore button are visible without scrolling.
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        TipSheetContent(
            state = TipUiState(
                isAdFree = isAdFree,
                canPrice = can?.displayPrice,
                coffeePrice = coffee?.displayPrice,
                isPurchasing = isPurchasing,
                didFail = didFail,
            ),
            catIndex = catIndex,
            onCatTap = { catIndex = (catIndex + 1) % TipCats.all.size },
            onBuyCan = { can?.let(::buy) },
            onBuyCoffee = { coffee?.let(::buy) },
            onRestore = { scope.launch { store.restore() } },
        )
    }
}
