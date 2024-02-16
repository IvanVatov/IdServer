package app.vatov.idserver.response

import app.vatov.idserver.Const
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    @SerialName(Const.OAuth.ACCESS_TOKEN)
    val accessToken: String,

    @SerialName(Const.OAuth.TOKEN_TYPE)
    val tokenType: String,

    @SerialName(Const.OAuth.EXPIRES_IN)
    val expiresIn: Long,

    @SerialName(Const.OAuth.REFRESH_TOKEN)
    val refreshToken: String? = null,

    @SerialName(Const.OAuth.ID_TOKEN)
    val idToken: String? = null,
)