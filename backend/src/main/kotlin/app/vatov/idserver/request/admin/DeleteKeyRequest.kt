package app.vatov.idserver.request.admin

import kotlinx.serialization.Serializable

@Serializable
data class DeleteKeyRequest(val tenantId: Int, val keyId: String)
