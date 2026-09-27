package jp.jacky.meow.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.Signature
import kotlin.io.encoding.Base64

class RsaPurchaseVerifierTest {
    private val keys = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    private val publicKey = Base64.encode(keys.public.encoded)
    private val json = """{"orderId":"GPA.1234","packageName":"jp.jacky.meow","productId":"jp.jacky.meow.can","purchaseState":0}"""

    private fun sign(data: String): String {
        val signer = Signature.getInstance("SHA1withRSA")
        signer.initSign(keys.private)
        signer.update(data.toByteArray(Charsets.UTF_8))
        return Base64.encode(signer.sign())
    }

    @Test
    fun acceptsASignatureMadeWithTheMatchingKey() {
        assertTrue(RsaPurchaseVerifier(publicKey).verify(json, sign(json)))
    }

    @Test
    fun rejectsATamperedPayload() {
        assertFalse(RsaPurchaseVerifier(publicKey).verify(json.replace("can", "coffee"), sign(json)))
    }

    @Test
    fun rejectsEverythingWithoutAKey() {
        assertFalse(RsaPurchaseVerifier("").verify(json, sign(json)))
    }

    @Test
    fun rejectsGarbageInsteadOfThrowing() {
        assertFalse(RsaPurchaseVerifier(publicKey).verify(json, "not base64 at all!!"))
        assertFalse(RsaPurchaseVerifier("bm90IGEga2V5").verify(json, sign(json)))
        assertFalse(RsaPurchaseVerifier(publicKey).verify("", sign(json)))
    }
}
