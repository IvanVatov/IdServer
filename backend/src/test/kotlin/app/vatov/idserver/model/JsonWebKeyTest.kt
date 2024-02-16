package app.vatov.idserver.model

import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Instant
import java.util.Base64
import kotlin.test.assertEquals

internal class JsonWebKeyTest {

    @Test
    fun testKeyGenerationFromModulusAndExponent() {

        val rsaKeyPair = RSAKeyPair.generate()
        val tenantKey = TenantRSAKeyPair(
            "1",
            1,
            rsaKeyPair.publicKey.encodeToString(),
            rsaKeyPair.privateKey.encodeToString(),
            Instant.now()
        )

        val jsonWebKey = tenantKey.toJsonWebKey("RS256")

        val decoder = Base64.getUrlDecoder()

        val modulusBytes = decoder.decode(jsonWebKey.n)
        val exponentBytes = decoder.decode(jsonWebKey.e)


        val modulus = BigInteger(modulusBytes)
        val exponent = BigInteger(exponentBytes)


        val rsaPublicKeySpec = RSAPublicKeySpec(modulus, exponent)

        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(rsaPublicKeySpec) as RSAPublicKey

        assertEquals(
            tenantKey.publicKey,
            publicKey.encodeToString(),
            "Keys doesn't match"
        )
    }
}