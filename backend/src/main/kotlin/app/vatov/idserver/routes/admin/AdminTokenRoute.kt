package app.vatov.idserver.routes.admin

import app.vatov.idserver.Const
import app.vatov.idserver.IDServer
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.response.ErrorResponse
import app.vatov.idserver.routes.oauth.grant.passwordGrantCase
import app.vatov.idserver.routes.oauth.grant.refreshTokenGrantCase
import app.vatov.idserver.routes.respondBadRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.userAgent
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.adminToken() {

    route("admin/${Const.Endpoint.TOKEN}") {

        post {

            val tenant = IDServer.getTenant(Const.Administration.TENANT_ID) ?: return@post

            val params = call.receiveParameters()

            val userAgent = call.request.userAgent() ?: "Unknown"

            val grantType = params[Const.OAuth.GRANT_TYPE]

            if (grantType == null) {
                respondBadRequest()
                return@post
            }

            val principal = call.principal<ClientPrincipal>() ?: return@post

            when (params[Const.OAuth.GRANT_TYPE]) {

                Const.OAuth.PASSWORD -> {
                    passwordGrantCase(tenant, principal, params, userAgent)
                    return@post
                }

                Const.OAuth.REFRESH_TOKEN -> {
                    refreshTokenGrantCase(tenant, principal, params)
                    return@post
                }

                else -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse.UNSUPPORTED_GRANT_TYPE
                    )
                    return@post
                }
            }
        }
    }
}