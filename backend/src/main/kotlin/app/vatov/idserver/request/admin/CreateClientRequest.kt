package app.vatov.idserver.request.admin

import kotlinx.serialization.Serializable

@Serializable
data class CreateClientRequest(
    val tenantId: Int,
    val clientId: String,
    val application: String
)
