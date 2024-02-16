package app.vatov.idserver.model

import app.vatov.idserver.model.serializers.GrantTypeSerializer
import kotlinx.serialization.Serializable

@Serializable(with = GrantTypeSerializer::class)
enum class GrantType {
    AUTHORIZATION_CODE,
    IMPLICIT,
    PASSWORD,
    CLIENT_CREDENTIALS,
    REFRESH_TOKEN
}