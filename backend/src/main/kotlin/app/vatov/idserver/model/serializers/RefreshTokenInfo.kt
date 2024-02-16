package app.vatov.idserver.model.serializers

import java.time.Instant

data class RefreshTokenInfo(
    val tenantId: Int,
    val userId: String,
    val refreshToken: String,
    val clientId: String,
    val scope: List<String>,
    val createdAt: Instant = Instant.now(),
    val refreshedAt: Instant = Instant.now(),
    val userAgent: String
)
