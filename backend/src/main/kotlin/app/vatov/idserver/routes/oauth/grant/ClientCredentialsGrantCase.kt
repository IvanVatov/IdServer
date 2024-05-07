package app.vatov.idserver.routes.oauth.grant

import app.vatov.idserver.Const
import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.model.GrantType
import app.vatov.idserver.model.Tenant
import app.vatov.idserver.response.TokenResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.clientCredentialsGrantCase(
    tenant: Tenant,
    principal: ClientPrincipal,
    params: Parameters
) {

    if (!principal.settings.grantTypes.contains(GrantType.PASSWORD)) {
        throw IdServerException.UNSUPPORTED_GRANT_TYPE
    }

    val scopes = (params[Const.OAuth.SCOPE] ?: "").split(' ')

    scopes.forEach {
        if (!principal.settings.scope.contains(it)) {
            throw IdServerException.INVALID_SCOPE
        }
    }

    val token = tenant.makeToken(principal, scopes)

    call.respond(
        TokenResponse(
            accessToken = token,
            tokenType = Const.OAuth.BEARER,
            expiresIn = principal.settings.tokenExpiration,
        )
    )

}