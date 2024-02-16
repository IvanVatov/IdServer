package app.vatov.idserver.request.admin

import kotlinx.serialization.Serializable

@Serializable
data class DeleteTenantRequest(val tenantId: Int)