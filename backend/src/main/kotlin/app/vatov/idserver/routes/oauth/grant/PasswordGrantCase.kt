package app.vatov.idserver.routes.oauth.grant

import app.vatov.idserver.Const
import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.model.GrantType
import app.vatov.idserver.model.Tenant
import app.vatov.idserver.repository.RefreshTokenRepository
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.response.TokenResponse
import app.vatov.idserver.ext.readParamOrRespondError
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

suspend fun PipelineContext<*, ApplicationCall>.passwordGrantCase(tenant: Tenant, principal: ClientPrincipal, params: Parameters, userAgent: String ) {

    if (!principal.settings.grantTypes.contains(GrantType.PASSWORD)) {
        throw IdServerException.UNSUPPORTED_GRANT_TYPE
    }

    val userName =
        readParamOrRespondError(params, Const.OAuth.USERNAME)
    val password =
        readParamOrRespondError(params, Const.OAuth.PASSWORD)

    val scopes = (params[Const.OAuth.SCOPE] ?: Const.EMPTY_STRING).split(' ')

    scopes.forEach {
        if (!principal.settings.scope.contains(it)) {
            throw IdServerException.INVALID_SCOPE
        }
    }

    val user = UserRepository.getByCredentials(tenant.id, userName, password)

    if (user != null) {

        val token = tenant.makeToken(user, principal, scopes)

        val refreshToken =
            if (scopes.contains(Const.OpenIdScope.OFFLINE_ACCESS)) {
                RefreshTokenRepository.create(
                    tenant.id,
                    user.id,
                    principal.clientId,
                    scopes,
                    userAgent
                )
            } else null

        call.respond(
            TokenResponse(
                accessToken = token,
                tokenType = Const.OAuth.BEARER,
                expiresIn = principal.settings.tokenExpiration,
                refreshToken = refreshToken,
            )
        )
    } else {
        throw IdServerException.INVALID_GRAND
    }
}
