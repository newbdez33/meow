package jp.jacky.meow.billing

import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import kotlin.io.encoding.Base64

/** Checks a purchase's signed JSON against the app's Play licensing key. */
fun interface PurchaseVerifier {
    fun verify(signedData: String, signature: String): Boolean
}

/**
 * Device-side verification with the Base64 RSA public key from Play Console > Monetization setup.
 * An empty key verifies nothing, so debug builds never count a purchase.
 */
class RsaPurchaseVerifier(private val base64PublicKey: String) : PurchaseVerifier {
    override fun verify(signedData: String, signature: String): Boolean {
        if (base64PublicKey.isBlank() || signedData.isBlank() || signature.isBlank()) return false
        return try {
            val key = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(Base64.decode(base64PublicKey)))
            val verifier = Signature.getInstance("SHA1withRSA")
            verifier.initVerify(key)
            verifier.update(signedData.toByteArray(Charsets.UTF_8))
            verifier.verify(Base64.decode(signature))
        } catch (e: GeneralSecurityException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
