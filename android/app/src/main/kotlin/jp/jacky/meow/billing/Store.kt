package jp.jacky.meow.billing

import android.app.Activity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PurchaseResult { Success, Cancelled, Pending, Failed }

/**
 * The two one-time treats that remove the ads (Store.swift). The entitlement is asked from Play at
 * every load and refresh and never stored by the app. A purchase counts only when it is one of the
 * two products, Play reports it purchased, and its signature verifies against the licensing key.
 */
class Store(
    private val billing: BillingGateway,
    private val verifier: PurchaseVerifier,
    scope: CoroutineScope,
) {
    private val _isAdFree = MutableStateFlow(false)
    /** True while a verified purchase of either treat exists for the signed-in Google account. */
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    private val _can = MutableStateFlow<Treat?>(null)
    /** The can once Play has answered; null while loading or unreachable. */
    val can: StateFlow<Treat?> = _can.asStateFlow()

    private val _coffee = MutableStateFlow<Treat?>(null)
    val coffee: StateFlow<Treat?> = _coffee.asStateFlow()

    private class Outcome(val result: UpdateResult, val counted: Boolean)
    private var inFlight: CompletableDeferred<Outcome>? = null

    init {
        scope.launch {
            billing.purchaseUpdates.collect { update ->
                val counted = update.result == UpdateResult.OK && countsAsAdFree(update.purchases)
                if (counted) _isAdFree.value = true
                inFlight?.complete(Outcome(update.result, counted))
            }
        }
    }

    /** Fetches both treats and the current entitlement. Call once at launch. */
    suspend fun load() {
        val products = billing.queryProducts(PRODUCT_IDS)
        _can.value = products.firstOrNull { it.productId == CAN_PRODUCT_ID }
        _coffee.value = products.firstOrNull { it.productId == COFFEE_PRODUCT_ID }
        refresh()
    }

    /** Re-reads the entitlement from Play. A failed query keeps the previous answer. */
    suspend fun refresh() {
        val purchases = billing.queryPurchases() ?: return
        _isAdFree.value = countsAsAdFree(purchases)
    }

    /** Play has no separate restore: purchases follow the Google account, so this re-reads them. */
    suspend fun restore() = refresh()

    /** Runs the Play purchase sheet for one treat and waits for its outcome. Cancelling is not an error. */
    suspend fun purchase(treat: Treat, activity: Activity): PurchaseResult {
        if (inFlight != null) return PurchaseResult.Failed
        val pending = CompletableDeferred<Outcome>()
        inFlight = pending
        try {
            // A sheet that does not open (ITEM_ALREADY_OWNED comes back from launchBillingFlow itself, before any
            // listener call) and an error update both mean: ask Play what the account really owns.
            if (!billing.launchPurchase(activity, treat.productId)) return afterRecheck()
            val outcome = pending.await()
            return when (outcome.result) {
                UpdateResult.OK -> if (outcome.counted) PurchaseResult.Success else PurchaseResult.Pending
                UpdateResult.CANCELLED -> PurchaseResult.Cancelled
                UpdateResult.ERROR -> afterRecheck()
            }
        } finally {
            inFlight = null
        }
    }

    /** Re-reads the entitlement; a purchase that already exists makes the tap a success. */
    private suspend fun afterRecheck(): PurchaseResult {
        refresh()
        return if (_isAdFree.value) PurchaseResult.Success else PurchaseResult.Failed
    }

    private suspend fun countsAsAdFree(purchases: List<PurchaseInfo>): Boolean {
        var adFree = false
        for (purchase in purchases) {
            if (purchase.productIds.none { it in PRODUCT_IDS }) continue
            if (purchase.state != PurchaseState.PURCHASED) continue
            if (!verifier.verify(purchase.originalJson, purchase.signature)) continue
            // Google refunds purchases that stay unacknowledged for three days.
            if (!purchase.isAcknowledged) billing.acknowledge(purchase.purchaseToken)
            adFree = true
        }
        return adFree
    }

    companion object {
        const val CAN_PRODUCT_ID = "jp.jacky.meow.can"
        const val COFFEE_PRODUCT_ID = "jp.jacky.meow.coffee"
        val PRODUCT_IDS = listOf(CAN_PRODUCT_ID, COFFEE_PRODUCT_ID)
    }
}
