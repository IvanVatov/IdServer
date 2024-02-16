package app.vatov.idserver.routes.oauth.grant

import app.vatov.idserver.Const
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.model.GrantType
import app.vatov.idserver.model.Tenant
import app.vatov.idserver.repository.RefreshTokenRepository
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.response.ErrorResponse
import app.vatov.idserver.response.TokenResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

suspend fun PipelineContext<*, ApplicationCall>.authorizationCodeGrantCase(
    tenant: Tenant,
    principal: ClientPrincipal,
    params: Parameters,
) {
    if (!principal.settings.grantTypes.contains(GrantType.AUTHORIZATION_CODE)) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.UNSUPPORTED_GRANT_TYPE
        )
        return
    }

    val redirectUrl = params[Const.OAuth.REDIRECT_URI]

    if (redirectUrl == null) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.BAD_REQUEST
        )
        return
    }

    val code = params[Const.OAuth.CODE]

    if (code == null) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.INVALID_GRAND
        )
        return
    }

    val authorizationInfoWrapper = principal.codes[code]

    if (authorizationInfoWrapper == null) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.INVALID_GRAND
        )
        return
    }

    principal.codes.remove(code)

    if (authorizationInfoWrapper.authorizationInfo.redirectUrl != redirectUrl) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.INVALID_GRAND
        )
        return
    }

    val scopes = authorizationInfoWrapper.authorizationInfo.scope.split(' ')

    scopes.forEach {
        if (!principal.settings.scope.contains(it)) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse.INVALID_SCOPE
            )
            return
        }
    }

    val user = UserRepository.getUserById(tenant.id, authorizationInfoWrapper.userId)

    if (user != null) {

        val token = tenant.makeToken(user, principal, scopes)

        val idToken = tenant.makeIdToken(user, principal, authorizationInfoWrapper.authorizationInfo.nonce)

        val refreshToken =
            if (scopes.contains(Const.OpenIdScope.OFFLINE_ACCESS)) {
                RefreshTokenRepository.create(
                    tenant.id,
                    user.id,
                    principal.clientId,
                    scopes,
                    authorizationInfoWrapper.userAgent
                )
            } else null

        call.respond(
            TokenResponse(
                accessToken = token,
                tokenType = Const.OAuth.BEARER,
                expiresIn = principal.settings.tokenExpiration,
                refreshToken = refreshToken,
                idToken = idToken
            )
        )
    } else {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.INVALID_GRAND
        )
    }
}