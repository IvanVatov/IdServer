package app.vatov.idserver.response

import app.vatov.idserver.model.JsonWebKey
import kotlinx.serialization.Serializable

@Serializable
data class JwskResponse(val keys: List<JsonWebKey>)
