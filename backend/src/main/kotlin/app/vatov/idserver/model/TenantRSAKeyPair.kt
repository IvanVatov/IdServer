package app.vatov.idserver.model

import app.vatov.idserver.Configuration
import kotlinx.serialization.Transient
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import java.time.Instant
import java.util.Base64


data class TenantRSAKeyPair(
    val id: String,
    val tenantId: Int,
    val publicKey: String,
    val privateKey: String,
    val createdAt: Instant
) {

    @Transient
    val rsaKeyPair: RSAKeyPair =
        RSAKeyPair(publicKey.decodeToRSAPublicKey(), privateKey.decodeToRSAPrivateKey())

    @Transient
    val exponent: String

    @Transient
    val modulus: String

    init {
        val keyFactory = KeyFactory.getInstance("RSA")
        val pubKeySpec: RSAPublicKeySpec =
            keyFactory.getKeySpec(rsaKeyPair.publicKey, RSAPublicKeySpec::class.java)

        modulus =
            Base64.getUrlEncoder().withoutPadding().encodeToString(pubKeySpec.modulus.toByteArray())
        exponent =
            Base64.getUrlEncoder().withoutPadding()
                .encodeToString(pubKeySpec.publicExponent.toByteArray())
    }

    fun toJsonWebKey(algorithm: String = Configuration.JWT_SIGNING_ALGORITHM): JsonWebKey {
        return JsonWebKey(
            kid = this.id,
            n = modulus,
            e = exponent,
            alg = algorithm
        )
    }
}
