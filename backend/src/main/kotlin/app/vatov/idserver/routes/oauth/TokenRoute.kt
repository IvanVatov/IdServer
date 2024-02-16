package app.vatov.idserver.routes.oauth

import app.vatov.idserver.Const
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.response.ErrorResponse.Companion.UNSUPPORTED_GRANT_TYPE
import app.vatov.idserver.routes.getTenantOrRespondError
import app.vatov.idserver.routes.oauth.grant.authorizationCodeGrantCase
import app.vatov.idserver.routes.oauth.grant.clientCredentialsGrantCase
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


fun Route.token() {

    route(Const.Endpoint.TOKEN) {

        post {

            val tenant = getTenantOrRespondError() ?: return@post

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

                Const.OAuth.CLIENT_CREDENTIALS -> {
                    clientCredentialsGrantCase(tenant, principal, params)
                    return@post
                }

                Const.OAuth.AUTHORIZATION_CODE -> {
                    authorizationCodeGrantCase(tenant, principal, params)
                    return@post
                }

                else -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        UNSUPPORTED_GRANT_TYPE
                    )
                    return@post
                }
            }
        }
    }
}