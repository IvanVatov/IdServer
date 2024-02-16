package app.vatov.idserver.request.user

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationRequest(
    val account: String,
    val password: String,
    val name: String? = null,
    val nickname: String? = null,
    val preferredUsername: String? = null,
    val picture: String? = null,
    val email: String? = null
)