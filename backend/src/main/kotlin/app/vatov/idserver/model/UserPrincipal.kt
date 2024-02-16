package app.vatov.idserver.model

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
class UserPrincipal(
    val id: String,
    val role: List<String>?
) : Principal