package app.vatov.idserver.model

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class ClientPrincipal(
    val tenantId: Int,
    val clientId: String,
    val application: String,
    val clientSecret: String,
    val settings: ClientSettings = ClientSettings()
) : Principal {

    @Transient
    val codes = ConcurrentHashMap<String, AuthorizationInfoWrapper>()
}