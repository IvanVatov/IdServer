package app.vatov.idserver.routes.oauth

import app.vatov.idserver.Const
import app.vatov.idserver.response.OpenIdConfigurationResponse
import app.vatov.idserver.routes.getTenant
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Routing.openIdConfiguration() {

    route(Const.Endpoint.OPEN_ID_CONFIGURATION) {

        get {

            val tenant = getTenant() ?: return@get

            val config = OpenIdConfigurationResponse.buildWithIssuer(tenant.issuer)

            call.respond(config)
        }
    }
}
