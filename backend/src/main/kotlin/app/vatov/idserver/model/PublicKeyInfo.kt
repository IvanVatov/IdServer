package app.vatov.idserver.model

import app.vatov.idserver.model.serializers.Iso8601InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class PublicKeyInfo(
    val id: String,
    @Serializable(with = Iso8601InstantSerializer::class)
    val createdAt: Instant,
    val publicKey: String
)
