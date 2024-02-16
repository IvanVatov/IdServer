package app.vatov.idserver.routes.oauth.grant

import app.vatov.idserver.Const
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.model.GrantType
import app.vatov.idserver.model.Tenant
import app.vatov.idserver.repository.RefreshTokenRepository
import app.vatov.idserver.response.ErrorResponse
import app.vatov.idserver.response.TokenResponse
import app.vatov.idserver.routes.readParamOrRespondError
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import java.time.Instant

suspend fun PipelineContext<*, ApplicationCall>.refreshTokenGrantCase(
    tenant: Tenant, principal: ClientPrincipal, params: Parameters
) {

    if (!principal.settings.grantTypes.contains(GrantType.REFRESH_TOKEN)) {
        call.respond(
            HttpStatusCode.BadRequest, ErrorResponse.UNSUPPORTED_GRANT_TYPE
        )
        return
    }

    val refreshToken = readParamOrRespondError(params, Const.OAuth.REFRESH_TOKEN) ?: return

    val userRefreshTokenInfoPair =
        RefreshTokenRepository.getUserAndRefreshTokenInfoByRefreshToken(
            tenant.id, refreshToken
        )

    if (userRefreshTokenInfoPair == null) {
        call.respond(
            HttpStatusCode.BadRequest, ErrorResponse.INVALID_GRAND
        )
        return
    }

    val user = userRefreshTokenInfoPair.first
    val refreshTokenInfo = userRefreshTokenInfoPair.second

    if (refreshTokenInfo.clientId != principal.clientId) {
        call.respond(
            HttpStatusCode.BadRequest, ErrorResponse.INVALID_SCOPE
        )
        return
    }

    if (Instant.now()
            .isAfter(refreshTokenInfo.createdAt.plusSeconds(principal.settings.refreshTokenAbsoluteExpiration))
    ) {
        call.respond(
            HttpStatusCode.BadRequest, ErrorResponse.INVALID_GRAND
        )
        return
    }

    if (Instant.now()
            .isAfter(refreshTokenInfo.createdAt.plusSeconds(principal.settings.refreshTokenExpiration))
    ) {
        call.respond(
            HttpStatusCode.BadRequest, ErrorResponse.INVALID_GRAND
        )
        return
    }

    refreshTokenInfo.scope.forEach {
        if (!principal.settings.scope.contains(it)) {
            call.respond(
                HttpStatusCode.BadRequest, ErrorResponse.INVALID_SCOPE
            )
            return
        }
    }

    val token = tenant.makeToken(user, principal, refreshTokenInfo.scope)

    val newRefreshToken = RefreshTokenRepository.update(
        tenant.id,
        user.id,
        refreshTokenInfo.refreshToken
    )

    call.respond(
        TokenResponse(
            accessToken = token,
            tokenType = Const.OAuth.BEARER,
            expiresIn = principal.settings.tokenExpiration,
            refreshToken = newRefreshToken,
        )
    )
}