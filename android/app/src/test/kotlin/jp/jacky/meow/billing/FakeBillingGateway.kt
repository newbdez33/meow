package jp.jacky.meow.billing

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/** Play as the tests want it: products, owned purchases, and what the next purchase flow reports. */
class FakeBillingGateway(private val scope: CoroutineScope) : BillingGateway {
    var products: List<Treat> = listOf(Treat(Store.CAN_PRODUCT_ID, "$5.99"), Treat(Store.COFFEE_PRODUCT_ID, "$2.99"))

    /** What queryPurchases answers; null means Play is unreachable. */
    var owned: MutableList<PurchaseInfo>? = mutableListOf()

    /** What the next launched flow reports; null means the sheet fails to open. */
    var nextLaunch: PurchaseUpdate? = null

    /** When set, launched flows report only after this completes. */
    var holdLaunch: kotlinx.coroutines.CompletableDeferred<Unit>? = null

    val acknowledged = mutableListOf<String>()
    var launches = 0

    private val updates = MutableSharedFlow<PurchaseUpdate>()
    override val purchaseUpdates: Flow<PurchaseUpdate> = updates

    override suspend fun queryProducts(productIds: List<String>): List<Treat> = products.filter { it.productId in productIds }

    override suspend fun queryPurchases(): List<PurchaseInfo>? = owned?.toList()

    override suspend fun launchPurchase(activity: Activity, productId: String): Boolean {
        launches++
        val update = nextLaunch ?: return false
        scope.launch {
            holdLaunch?.await()
            if (update.result == UpdateResult.OK) owned?.addAll(update.purchases)
            updates.emit(update)
        }
        return true
    }

    override suspend fun acknowledge(purchaseToken: String): Boolean {
        acknowledged += purchaseToken
        owned?.replaceAll { if (it.purchaseToken == purchaseToken) it.copy(isAcknowledged = true) else it }
        return true
    }

    /** A change Play pushes on its own, outside any purchase flow. */
    suspend fun push(update: PurchaseUpdate) {
        if (update.result == UpdateResult.OK) owned?.addAll(update.purchases)
        updates.emit(update)
    }
}
