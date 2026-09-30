package jp.jacky.meow.billing

import android.app.Activity
import kotlinx.coroutines.flow.Flow

/** A treat as Play describes it: the product id and the localized price to show. */
data class Treat(val productId: String, val displayPrice: String)

enum class PurchaseState { PURCHASED, PENDING, UNSPECIFIED }

/** The part of a Play purchase the Store looks at. */
data class PurchaseInfo(
    val productIds: List<String>,
    val state: PurchaseState,
    val isAcknowledged: Boolean,
    val purchaseToken: String,
    val originalJson: String,
    val signature: String,
)

enum class UpdateResult { OK, CANCELLED, ERROR }

/** One PurchasesUpdatedListener call: the outcome of a purchase flow, or a change Play pushes on its own. */
data class PurchaseUpdate(val result: UpdateResult, val purchases: List<PurchaseInfo>)

/** The slice of Play Billing the Store uses. PlayBillingGateway is the real one; tests use a fake. */
interface BillingGateway {
    /** The requested one-time products Play knows about; empty when Play cannot be reached. */
    suspend fun queryProducts(productIds: List<String>): List<Treat>

    /** The account's current one-time purchases, or null when Play cannot be reached. */
    suspend fun queryPurchases(): List<PurchaseInfo>?

    /** Opens the Play purchase sheet. True when it opened; its result then arrives on [purchaseUpdates]. */
    suspend fun launchPurchase(activity: Activity, productId: String): Boolean

    suspend fun acknowledge(purchaseToken: String): Boolean

    val purchaseUpdates: Flow<PurchaseUpdate>
}
