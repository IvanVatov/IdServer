package app.vatov.idserver.routes.oauth

import app.vatov.idserver.Const
import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.model.AuthorizationInfo
import app.vatov.idserver.ext.getTenant
import app.vatov.idserver.ext.readParamOrRespondError
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.server.application.call
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Routing.authorize() {

    route(Const.Endpoint.AUTHORIZE) {

        get {

            val tenant = getTenant()

            val params = call.parameters

            // region RESPONSE TYPE

            val responseType =
                readParamOrRespondError(params, Const.OAuth.RESPONSE_TYPE)

            if (responseType != "code") {
                throw IdServerException.BAD_REQUEST
            }

            // endregion
            // region CLIENT

            val clientId = readParamOrRespondError(params, Const.OAuth.CLIENT_ID)

            val clientPrincipal = tenant.getClient(clientId)

            // endregion
            // region SCOPE

            val scope = params[Const.OAuth.SCOPE] ?: ""

            val scopes = scope.split(' ')
// TODO: Validate it           val scopes = URLDecoder.decode(scope, StandardCharsets.UTF_8).split(' ')

            scopes.forEach {
                if (!clientPrincipal.settings.scope.contains(it)) {
                    throw IdServerException.INVALID_SCOPE
                }
            }

            // endregion

            val redirectUrl =
                readParamOrRespondError(params, Const.OAuth.REDIRECT_URI)


            if (!clientPrincipal.settings.redirectUris.contains(redirectUrl)) {
                throw IdServerException.BAD_REQUEST // not sure about this error
            }

            val state = params[Const.OAuth.STATE] ?: ""

            val nonce = params[Const.OpenIdClaim.NONCE] ?: ""

            val register = params["register"]

            call.respondRedirect(
                URLBuilder(
                    protocol = if (tenant.host == "127.0.0.1") URLProtocol.HTTP else URLProtocol.HTTPS,
                    host = tenant.host,
                    pathSegments = if (register != null) listOf("register") else listOf("login"),
                    parameters = Parameters.build {
                        append(
                            "code",
                            tenant.encryptInfo(
                                AuthorizationInfo(
                                    tenantId = tenant.id,
                                    clientId = clientId,
                                    state = state,
                                    nonce = nonce,
                                    redirectUrl = redirectUrl,
                                    scope = scope
                                )
                            )
                        )
                    }).build()
            )

//            val config = OpenIdConfigurationResponse()
//
//            call.respond(config)
        }
    }
}
