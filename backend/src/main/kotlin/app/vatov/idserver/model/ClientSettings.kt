package app.vatov.idserver.model

import kotlinx.serialization.Serializable

@Serializable
data class ClientSettings(
    val grantTypes: List<GrantType> = emptyList(),
    val redirectUris: List<String> = emptyList(),
    val scope: List<String> = emptyList(),
    val audience: List<String> = emptyList(),
    val tokenExpiration: Long = 3600,
    val refreshTokenExpiration: Long = 604800,
    val refreshTokenAbsoluteExpiration: Long = 2592000
)
