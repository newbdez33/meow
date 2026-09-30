package jp.jacky.meow.billing

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Locale

/**
 * The debug screenshot mode's Play (MainActivity, behind BuildConfig.DEBUG): both treats with a
 * price in the device's language, nothing owned, and purchase sheets that never open. It plays the
 * part the meow.storekit configuration plays for the App Store screenshots.
 */
class PreviewBillingGateway : BillingGateway {
    override val purchaseUpdates: Flow<PurchaseUpdate> = emptyFlow()

    override suspend fun queryProducts(productIds: List<String>): List<Treat> {
        val (can, coffee) = when (Locale.getDefault().language) {
            "ja" -> "¥900" to "¥450"
            "zh" -> "US$5.99" to "US$2.99"
            else -> "$5.99" to "$2.99"
        }
        return listOf(Treat(Store.CAN_PRODUCT_ID, can), Treat(Store.COFFEE_PRODUCT_ID, coffee))
            .filter { it.productId in productIds }
    }

    override suspend fun queryPurchases(): List<PurchaseInfo> = emptyList()

    override suspend fun launchPurchase(activity: Activity, productId: String): Boolean = false

    override suspend fun acknowledge(purchaseToken: String): Boolean = false
}
