package jp.jacky.meow.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/** Play Billing Library 9 behind the BillingGateway interface. One client for the process. */
class PlayBillingGateway(context: Context) : BillingGateway {
    private val updates = MutableSharedFlow<PurchaseUpdate>(extraBufferCapacity = 16)
    override val purchaseUpdates: Flow<PurchaseUpdate> = updates

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener { result, purchases ->
            updates.tryEmit(PurchaseUpdate(result.responseCode.toUpdateResult(), purchases.orEmpty().map { it.toInfo() }))
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    /** Product details by id, kept because launching a purchase needs the object, not the id. */
    private val details = mutableMapOf<String, ProductDetails>()
    private val connecting = Mutex()

    private suspend fun connected(): Boolean = connecting.withLock {
        if (client.isReady) {
            true
        } else {
            suspendCancellableCoroutine { continuation ->
                client.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (continuation.isActive) continuation.resume(result.responseCode == BillingResponseCode.OK)
                    }

                    override fun onBillingServiceDisconnected() {
                        // The client reconnects by itself (enableAutoServiceReconnection).
                    }
                })
            }
        }
    }

    override suspend fun queryProducts(productIds: List<String>): List<Treat> {
        if (!connected()) return emptyList()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                productIds.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(ProductType.INAPP)
                        .build()
                }
            )
            .build()
        return suspendCancellableCoroutine { continuation ->
            client.queryProductDetailsAsync(params) { result, query ->
                val found = if (result.responseCode == BillingResponseCode.OK) query.productDetailsList else emptyList()
                found.forEach { details[it.productId] = it }
                continuation.resume(found.mapNotNull { product -> product.offer()?.let { Treat(product.productId, it.formattedPrice) } })
            }
        }
    }

    override suspend fun queryPurchases(): List<PurchaseInfo>? {
        if (!connected()) return null
        val params = QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()
        return suspendCancellableCoroutine { continuation ->
            client.queryPurchasesAsync(params) { result, purchases ->
                continuation.resume(if (result.responseCode == BillingResponseCode.OK) purchases.map { it.toInfo() } else null)
            }
        }
    }

    override suspend fun launchPurchase(activity: Activity, productId: String): Boolean {
        if (!connected()) return false
        val product = details[productId] ?: return false
        val item = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product)
        product.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken?.let { item.setOfferToken(it) }
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(item.build())).build()
        return client.launchBillingFlow(activity, params).responseCode == BillingResponseCode.OK
    }

    override suspend fun acknowledge(purchaseToken: String): Boolean {
        if (!connected()) return false
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        return suspendCancellableCoroutine { continuation ->
            client.acknowledgePurchase(params) { result -> continuation.resume(result.responseCode == BillingResponseCode.OK) }
        }
    }

    /** A one-time product with one offer has oneTimePurchaseOfferDetails; with several it has the list. */
    private fun ProductDetails.offer(): ProductDetails.OneTimePurchaseOfferDetails? =
        oneTimePurchaseOfferDetailsList?.firstOrNull() ?: oneTimePurchaseOfferDetails

    private fun Purchase.toInfo() = PurchaseInfo(
        productIds = products,
        state = when (purchaseState) {
            Purchase.PurchaseState.PURCHASED -> PurchaseState.PURCHASED
            Purchase.PurchaseState.PENDING -> PurchaseState.PENDING
            else -> PurchaseState.UNSPECIFIED
        },
        isAcknowledged = isAcknowledged,
        purchaseToken = purchaseToken,
        originalJson = originalJson,
        signature = signature,
    )

    private fun Int.toUpdateResult() = when (this) {
        BillingResponseCode.OK -> UpdateResult.OK
        BillingResponseCode.USER_CANCELED -> UpdateResult.CANCELLED
        else -> UpdateResult.ERROR
    }
}
