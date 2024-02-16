package app.vatov.idserver.response

import app.vatov.idserver.model.PublicKeyInfo
import kotlinx.serialization.Serializable

@Serializable
data class CurrentKeyResponse(
    val current: PublicKeyInfo,
    val valid: List<PublicKeyInfo>
)