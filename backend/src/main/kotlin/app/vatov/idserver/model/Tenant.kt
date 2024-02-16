package app.vatov.idserver.model

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.algorithms.Algorithm.RSA256
import com.auth0.jwt.algorithms.Algorithm.RSA384
import com.auth0.jwt.algorithms.Algorithm.RSA512
import com.auth0.jwt.interfaces.JWTVerifier
import com.auth0.jwt.interfaces.RSAKeyProvider
import app.vatov.idserver.Configuration
import app.vatov.idserver.Const
import app.vatov.idserver.model.serializers.toIso8601
import app.vatov.idserver.util.decrypt
import app.vatov.idserver.util.encrypt
import app.vatov.idserver.util.randomBytes
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

@Serializable
data class Tenant(val id: Int, val name: String, val host: String, val aliases: List<String>) {

    inner class Provider : RSAKeyProvider {
        override fun getPublicKeyById(keyId: String): RSAPublicKey {
            return _currentRSAKeyPair.rsaKeyPair.publicKey
        }

        override fun getPrivateKey(): RSAPrivateKey {
            return _currentRSAKeyPair.rsaKeyPair.privateKey
        }

        override fun getPrivateKeyId(): String {
            return _currentRSAKeyPair.id
        }
    }

    @Transient
    private val _secretKey =
        SecretKeyFactory.getInstance("DES").generateSecret(DESKeySpec(randomBytes()))

    @Transient
    val issuer =
        URLBuilder(protocol = URLProtocol.HTTPS, host = this.host).build().toString()

    @Transient
    private lateinit var _currentRSAKeyPair: TenantRSAKeyPair

    @Transient
    private val _validRSAKeyPair = LinkedHashMap<String, TenantRSAKeyPair>()

    @Transient
    private val _clients = LinkedHashMap<String, ClientPrincipal>()

    @Transient
    private val _provider = Provider()

    @Transient
    private val _algorithm: Algorithm = getAlgorithm()

    @Transient
    val jwtVerifier: JWTVerifier = JWT.require(_algorithm).withIssuer(
        URLBuilder(protocol = URLProtocol.HTTPS, host = this.host).build().toString()
    ).build()

    fun makeToken(user: User, client: ClientPrincipal, scope: List<String>): String {

        val now = Instant.now()

        val builder = JWT.create()
            .withKeyId(_currentRSAKeyPair.id)
            .withIssuer(this.issuer)
            .withSubject(user.id)
            .withClaim(Const.OpenIdClaim.CLIENT_ID, client.clientId)
            .withClaim(Const.OpenIdClaim.APPLICATION, client.application)
            .withClaim(Const.OAuth.SCOPE, scope)

        if (scope.contains(Const.OpenIdScope.PROFILE)) {
            user.name?.let {
                builder.withClaim(Const.OpenIdClaim.NAME, it)
            }
            user.familyName?.let {
                builder.withClaim(Const.OpenIdClaim.FAMILY_NAME, it)
            }
            user.givenName?.let {
                builder.withClaim(Const.OpenIdClaim.GIVEN_NAME, it)
            }
            user.middleName?.let {
                builder.withClaim(Const.OpenIdClaim.MIDDLE_NAME, it)
            }
            user.nickname?.let {
                builder.withClaim(Const.OpenIdClaim.NICKNAME, it)
            }
            user.preferredUsername?.let {
                builder.withClaim(Const.OpenIdClaim.PREFERRED_USERNAME, it)
            }
            user.profile?.let {
                builder.withClaim(Const.OpenIdClaim.PROFILE, it)
            }
            user.picture?.let {
                builder.withClaim(Const.OpenIdClaim.PICTURE, it)
            }
            user.website?.let {
                builder.withClaim(Const.OpenIdClaim.WEBSITE, it)
            }
            user.gender?.let {
                builder.withClaim(Const.OpenIdClaim.GENDER, it)
            }
            user.birthdate?.let {
                builder.withClaim(Const.OpenIdClaim.BIRTH_DATE, it.toIso8601())
            }
            user.zoneInfo?.let {
                builder.withClaim(Const.OpenIdClaim.ZONE_INFO, it)
            }
            user.locale?.let {
                builder.withClaim(Const.OpenIdClaim.LOCALE, it)
            }
            builder.withClaim(Const.OpenIdClaim.UPDATED_AT, user.updatedAt.epochSecond)
        }



        if (scope.contains(Const.OpenIdScope.EMAIL)) {
            user.email?.let {
                builder.withClaim(Const.OpenIdClaim.EMAIL, it)
                builder.withClaim(Const.OpenIdClaim.EMAIL_VERIFIED, user.emailVerified ?: false)
            }
        }

        if (scope.contains(Const.OpenIdScope.ADDRESS)) {
            user.address?.let {
                builder.withClaim(Const.OpenIdClaim.ADDRESS, it)
            }
        }

        if (scope.contains(Const.OpenIdScope.PHONE)) {
            user.phoneNumber?.let {
                builder.withClaim(Const.OpenIdClaim.PHONE_NUMBER, it)
                builder.withClaim(
                    Const.OpenIdClaim.PHONE_NUMBER_VERIFIED,
                    user.phoneNumberVerified ?: false
                )
            }
        }

        builder.withClaim(Const.OpenIdClaim.EMAIL, user.email)
            .withClaim(Const.OpenIdClaim.NAME, user.name)
            .withClaim(Const.OpenIdClaim.PICTURE, user.picture)
            .withExpiresAt(now.plusSeconds(client.settings.tokenExpiration))
            .withNotBefore(now.minusSeconds(5))

        return builder.sign(_algorithm)
    }

    fun makeToken(client: ClientPrincipal, scope: List<String>): String =
        JWT.create()
            .withKeyId(_currentRSAKeyPair.id)
            .withIssuer(this.issuer)
            .withClaim(Const.OpenIdClaim.CLIENT_ID, client.clientId)
            .withClaim(Const.OpenIdClaim.APPLICATION, client.application)
            .withClaim(Const.OAuth.SCOPE, scope)
            .withExpiresAt(Instant.now().plusSeconds(client.settings.tokenExpiration))
            .withNotBefore(Instant.now().minusSeconds(5))
            .sign(_algorithm)

    fun makeIdToken(user: User, client: ClientPrincipal, nonce: String): String {

        val now = Instant.now()

        val builder = JWT.create()
            .withKeyId(_currentRSAKeyPair.id)
            .withIssuer(this.issuer)
            .withSubject(user.id)
            .withExpiresAt(now.plusSeconds(client.settings.tokenExpiration))
            .withIssuedAt(now)
            .withAudience(client.clientId)
            .withClaim(Const.OpenIdClaim.NONCE, nonce)
        return builder.sign(_algorithm)
    }


    fun addRSAKeyPair(tenantRSAKeyPair: TenantRSAKeyPair) {
        _validRSAKeyPair[_currentRSAKeyPair.id] = _currentRSAKeyPair
        _currentRSAKeyPair = tenantRSAKeyPair
    }

    fun setValidRSAKeyPairs(list: List<TenantRSAKeyPair>) {
        if (list.isEmpty()) {
            throw Exception("Valid RSA keys for tenantId: $id should not be empty")
        }

        _currentRSAKeyPair = list.last()

        if (list.size > 1) {
            for (i in 0 until list.size - 1) {
                val key = list[i]
                _validRSAKeyPair[key.id] = key
            }
        }
    }

    fun getValidPublicKeys(): List<PublicKeyInfo> {
        return _validRSAKeyPair.map {
            PublicKeyInfo(
                it.key,
                it.value.createdAt,
                it.value.rsaKeyPair.publicKey.encodeToString()
            )
        }.toList()
    }

    fun getCurrentPublicKey(): PublicKeyInfo {
        return PublicKeyInfo(
            _currentRSAKeyPair.id,
            _currentRSAKeyPair.createdAt,
            _currentRSAKeyPair.rsaKeyPair.publicKey.encodeToString()
        )
    }

    fun removeRSAKeyPair(keyId: String) {

        //TODO: check is not the last, remove and update
        _validRSAKeyPair.remove(keyId)
    }

    private fun getAlgorithm(): Algorithm {
        return when (Configuration.JWT_SIGNING_ALGORITHM) {
            "RS256" -> RSA256(_provider)
            "RS384" -> RSA384(_provider)
            "RS512" -> RSA512(_provider)
            else -> throw Exception("JWTSigningAlgorithm is not properly configured: ${Configuration.JWT_SIGNING_ALGORITHM}")
        }
    }

    fun getClient(clientId: String): ClientPrincipal? {
        return _clients[clientId]
    }

    fun setClients(listClients: List<ClientPrincipal>) {
        listClients.forEach {
            _clients[it.clientId] = it
        }
    }

    fun setClient(clientPrincipal: ClientPrincipal) {
        _clients[clientPrincipal.clientId] = clientPrincipal
    }

    fun removeClientId(clientId: String) {
        _clients.remove(clientId)
    }

    // region CODE

    fun encryptInfo(
        authorizationInfo: AuthorizationInfo
    ): String {
        return URLEncoder.encode(authorizationInfo.toString().encrypt(_secretKey), Charsets.UTF_8)
    }

    fun decryptInfoInfo(
        encryptedString: String
    ): AuthorizationInfo? {
        return try {
            AuthorizationInfo.fromString(
                URLDecoder.decode(encryptedString, Charsets.UTF_8).decrypt(_secretKey)
            )
        } catch (t: Throwable) {
            null
        }
    }

    // endregion

}