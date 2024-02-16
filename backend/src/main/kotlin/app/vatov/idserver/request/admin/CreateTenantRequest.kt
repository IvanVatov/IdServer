package app.vatov.idserver.request.admin

import kotlinx.serialization.Serializable

@Serializable
data class CreateTenantRequest(
    val name: String, val host: String
)
