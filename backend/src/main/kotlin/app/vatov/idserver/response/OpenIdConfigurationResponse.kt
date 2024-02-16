package app.vatov.idserver.response

import app.vatov.idserver.Const
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
data class OpenIdConfigurationResponse(
    val issuer: String,
    @SerialName("authorization_endpoint")
    val authorizationEndpoint: String,
    @SerialName("token_endpoint")
    val tokenEndpoint: String,
    @SerialName("userinfo_endpoint")
    val userInfoEndpoint: String,
    @SerialName("jwks_uri")
    val jwksUri: String,
    @SerialName("scopes_supported")
    val scopesSupported: List<String> = listOf(
        "openid", "profile", "email", "address",
        "phone", "offline_access"
    ),
    @SerialName("response_types_supported")
    val responseTypesSupported: List<String> = listOf("code", "code token")
) {
    companion object {
        fun buildWithIssuer(issuer: String): OpenIdConfigurationResponse {
            val url = URL(issuer)

            return OpenIdConfigurationResponse(
                issuer = issuer,
                authorizationEndpoint = URL(url, Const.Endpoint.AUTHORIZE).toString(),
                tokenEndpoint = URL(url, Const.Endpoint.TOKEN).toString(),
                userInfoEndpoint = URL(url, "user/whoami").toString(),
                jwksUri = URL(url, Const.Endpoint.JWKS_JSON).toString()
            )
        }
    }
}