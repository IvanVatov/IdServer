package app.vatov.idserver.request.admin

import kotlinx.serialization.Serializable

@Serializable
data class DeleteClientRequest(
    val tenantId: Int,
    val clientId: String
)
