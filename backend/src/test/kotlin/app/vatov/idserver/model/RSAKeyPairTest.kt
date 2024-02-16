package app.vatov.idserver.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class RSAKeyPairTest {

    @Test
    fun test() {
        val rsaKeyPair = RSAKeyPair.generate()

        val stringEncodedPrivateKey = rsaKeyPair.privateKey.encodeToString()

        val decodedFromStringPrivateKey = stringEncodedPrivateKey.decodeToRSAPrivateKey()

        val stringEncodedPublicKey = rsaKeyPair.publicKey.encodeToString()

        val decodedFromStringPublicKey = stringEncodedPublicKey.decodeToRSAPublicKey()

        assertEquals(
            rsaKeyPair.privateKey,
            decodedFromStringPrivateKey,
            "Private key is not the same"
        )

        assertEquals(rsaKeyPair.publicKey, decodedFromStringPublicKey, "Public key is not the same")
    }
}