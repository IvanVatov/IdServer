package app.vatov.idserver.request.admin

import app.vatov.idserver.model.ClientSettings
import kotlinx.serialization.Serializable

@Serializable
data class UpdateClientRequest(
    val tenantId: Int,
    val clientId: String,
    val clientSecret: String,
    val clientSettings: ClientSettings
)
