package app.vatov.idserver.routes.oauth

import app.vatov.idserver.repository.TenantRSAKeyPairRepository
import app.vatov.idserver.response.JwskResponse
import app.vatov.idserver.routes.getTenantOrRespondError
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Routing.jwksJson() {

    route("jwks.json") {

        get {
            val tenant = getTenantOrRespondError() ?: return@get

            val keys = TenantRSAKeyPairRepository.getAllForTenant(tenant.id).map { it.toJsonWebKey() }

            call.respond(JwskResponse(keys))
        }
    }
}
