package app.vatov.idserver.request.admin

import kotlinx.serialization.Serializable

@Serializable
data class RotateKeyRequest(val tenantId: Int)
