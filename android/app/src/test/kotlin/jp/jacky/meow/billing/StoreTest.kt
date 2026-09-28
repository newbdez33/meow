package jp.jacky.meow.billing

import android.app.Activity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoreTest {
    private val acceptSignedValid = PurchaseVerifier { _, signature -> signature == "valid" }
    private val activity = Activity()

    private fun purchase(
        product: String,
        state: PurchaseState = PurchaseState.PURCHASED,
        acknowledged: Boolean = false,
        signature: String = "valid",
    ) = PurchaseInfo(listOf(product), state, acknowledged, "token-$product", """{"productId":"$product"}""", signature)

    private fun ok(vararg purchases: PurchaseInfo) = PurchaseUpdate(UpdateResult.OK, purchases.toList())

    /**
     * A Store with its update collector already subscribed. runCurrent, not advanceUntilIdle: the
     * collector lives in backgroundScope, and advanceUntilIdle only runs background work while
     * foreground work is pending.
     */
    private fun TestScope.store(billing: FakeBillingGateway): Store {
        val store = Store(billing, acceptSignedValid, backgroundScope)
        runCurrent()
        return store
    }

    @Test
    fun freshInstallOffersBothTreatsAndShowsAds() = runTest {
        val billing = FakeBillingGateway(backgroundScope)
        val store = store(billing)
        store.load()
        assertEquals("$5.99", store.can.value?.displayPrice)
        assertEquals("$2.99", store.coffee.value?.displayPrice)
        assertFalse(store.isAdFree.value)
    }

    @Test
    fun buyingCoffeeRemovesAds() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { nextLaunch = ok(purchase(Store.COFFEE_PRODUCT_ID)) }
        val store = store(billing)
        store.load()
        val result = store.purchase(store.coffee.value!!, activity)
        assertEquals(PurchaseResult.Success, result)
        assertTrue(store.isAdFree.value)
        assertEquals(listOf("token-${Store.COFFEE_PRODUCT_ID}"), billing.acknowledged)
    }

    @Test
    fun buyingCanRemovesAds() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { nextLaunch = ok(purchase(Store.CAN_PRODUCT_ID)) }
        val store = store(billing)
        store.load()
        assertEquals(PurchaseResult.Success, store.purchase(store.can.value!!, activity))
        assertTrue(store.isAdFree.value)
    }

    @Test
    fun earlierPurchaseIsHonouredOnLaunch() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { owned!!.add(purchase(Store.CAN_PRODUCT_ID, acknowledged = true)) }
        val store = store(billing)
        store.load()
        assertTrue(store.isAdFree.value)
        assertTrue(billing.acknowledged.isEmpty())
    }

    @Test
    fun restoreFindsAPurchaseMadeElsewhere() = runTest {
        val billing = FakeBillingGateway(backgroundScope)
        val store = store(billing)
        store.load()
        assertFalse(store.isAdFree.value)
        billing.owned!!.add(purchase(Store.COFFEE_PRODUCT_ID, acknowledged = true))
        store.restore()
        assertTrue(store.isAdFree.value)
    }

    @Test
    fun pendingPurchaseDoesNotRemoveAds() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { nextLaunch = ok(purchase(Store.CAN_PRODUCT_ID, state = PurchaseState.PENDING)) }
        val store = store(billing)
        store.load()
        assertEquals(PurchaseResult.Pending, store.purchase(store.can.value!!, activity))
        assertFalse(store.isAdFree.value)
        assertTrue(billing.acknowledged.isEmpty())
    }

    @Test
    fun badSignatureDoesNotRemoveAds() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { owned!!.add(purchase(Store.CAN_PRODUCT_ID, signature = "forged")) }
        val store = store(billing)
        store.load()
        assertFalse(store.isAdFree.value)
        assertTrue(billing.acknowledged.isEmpty())
    }

    @Test
    fun unknownProductDoesNotRemoveAds() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { owned!!.add(purchase("jp.jacky.meow.yacht")) }
        val store = store(billing)
        store.load()
        assertFalse(store.isAdFree.value)
    }

    @Test
    fun unacknowledgedPurchaseIsAcknowledgedOnce() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { owned!!.add(purchase(Store.CAN_PRODUCT_ID)) }
        val store = store(billing)
        store.load()
        store.refresh()
        assertTrue(store.isAdFree.value)
        assertEquals(listOf("token-${Store.CAN_PRODUCT_ID}"), billing.acknowledged)
    }

    @Test
    fun cancelledPurchaseIsNotAnError() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { nextLaunch = PurchaseUpdate(UpdateResult.CANCELLED, emptyList()) }
        val store = store(billing)
        store.load()
        assertEquals(PurchaseResult.Cancelled, store.purchase(store.can.value!!, activity))
        assertFalse(store.isAdFree.value)
    }

    @Test
    fun aSheetThatFailsToOpenReportsFailure() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { nextLaunch = null }
        val store = store(billing)
        store.load()
        assertEquals(PurchaseResult.Failed, store.purchase(store.can.value!!, activity))
        assertEquals(1, billing.launches)
    }

    @Test
    fun aSheetThatFailsToOpenForAnOwnedTreatSucceeds() = runTest {
        // launchBillingFlow itself answers ITEM_ALREADY_OWNED (a purchase made on another device, cache lagging):
        // the sheet never opens, but the account owns the can, so the tap must end ad-free, not "didn't go through".
        val billing = FakeBillingGateway(backgroundScope).apply { nextLaunch = null }
        val store = store(billing)
        store.load()
        billing.owned!!.add(purchase(Store.CAN_PRODUCT_ID, acknowledged = true))
        assertEquals(PurchaseResult.Success, store.purchase(store.can.value!!, activity))
        assertTrue(store.isAdFree.value)
    }

    @Test
    fun anErrorUpdateRechecksTheEntitlement() = runTest {
        // ITEM_ALREADY_OWNED after a refund lag: Play says error, but the account does own the can.
        val billing = FakeBillingGateway(backgroundScope).apply {
            nextLaunch = PurchaseUpdate(UpdateResult.ERROR, emptyList())
            owned!!.add(purchase(Store.CAN_PRODUCT_ID, acknowledged = true))
        }
        val store = store(billing)
        assertEquals(PurchaseResult.Success, store.purchase(Treat(Store.CAN_PRODUCT_ID, "$5.99"), activity))
        assertTrue(store.isAdFree.value)
    }

    @Test
    fun unsolicitedUpdateTurnsAdsOff() = runTest {
        val billing = FakeBillingGateway(backgroundScope)
        val store = store(billing)
        store.load()
        billing.push(ok(purchase(Store.COFFEE_PRODUCT_ID)))
        runCurrent()
        assertTrue(store.isAdFree.value)
        assertEquals(listOf("token-${Store.COFFEE_PRODUCT_ID}"), billing.acknowledged)
    }

    @Test
    fun secondPurchaseWhileOneIsInFlightIsIgnored() = runTest {
        val hold = CompletableDeferred<Unit>()
        val billing = FakeBillingGateway(backgroundScope).apply {
            nextLaunch = ok(purchase(Store.CAN_PRODUCT_ID))
            holdLaunch = hold
        }
        val store = store(billing)
        store.load()
        val first = async { store.purchase(store.can.value!!, activity) }
        advanceUntilIdle()
        val second = store.purchase(store.can.value!!, activity)
        assertEquals(PurchaseResult.Failed, second)
        hold.complete(Unit)
        assertEquals(PurchaseResult.Success, first.await())
        assertEquals(1, billing.launches)
    }

    @Test
    fun playUnreachableKeepsPreviousEntitlement() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { owned!!.add(purchase(Store.CAN_PRODUCT_ID, acknowledged = true)) }
        val store = store(billing)
        store.load()
        assertTrue(store.isAdFree.value)
        billing.owned = null
        store.refresh()
        assertTrue(store.isAdFree.value)
        assertNotNull(store.can.value)
    }

    @Test
    fun noProductsMeansNothingOffered() = runTest {
        val billing = FakeBillingGateway(backgroundScope).apply { products = emptyList(); owned = null }
        val store = store(billing)
        store.load()
        assertNull(store.can.value)
        assertNull(store.coffee.value)
        assertFalse(store.isAdFree.value)
    }
}
