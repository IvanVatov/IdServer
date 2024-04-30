package app.vatov.idserver.routes.oauth

import app.vatov.idserver.ext.getTenant
import app.vatov.idserver.model.AuthorizationInfoWrapper
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.util.generateRandomString
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*
import java.net.URL
import kotlin.collections.set


fun Routing.login() {

    route(Regex("login(.+)?")) {

        get {

            val tenant = getTenant()

            val code = call.request.queryParameters.getOrFail("code")

            respondLoginFtl(tenantId = tenant.id, code = code)
        }

        post {
            val tenant = getTenant()

            val params = call.receiveParameters()

            val account = params["account"] ?: ""

            val password = params["password"] ?: ""

            val code = call.request.queryParameters.getOrFail("code")

            if (account.isBlank() || password.isBlank()) {
                respondLoginFtl(tenant.id, account, code, "Account and password fields cannot be blank")
                return@post
            }

            val authorizationInfo = tenant.decryptInfoInfo(code)

            val user = UserRepository.getByCredentials(tenant.id, account, password)

            if (authorizationInfo == null || user == null) {
                respondLoginFtl(tenant.id, account, code, "Invalid account or password")
                return@post
            }

            if (authorizationInfo.createdAt < System.currentTimeMillis() - 600000) { // 10 min
                respondLoginFtl(tenant.id, account, code, "Expired authorization request")
                return@post
            }

            val client = tenant.getClient(authorizationInfo.clientId)

            val authorizationCode = generateRandomString()

            val userAgent = call.request.userAgent() ?: "Unknown"

            client.codes[authorizationCode] = AuthorizationInfoWrapper(user.id, userAgent, authorizationInfo)

            val mUrl = Url(authorizationInfo.redirectUrl)

            val redirectUrl = URLBuilder(
                protocol = if (tenant.host == "127.0.0.1") URLProtocol.HTTP else URLProtocol.HTTPS,
                host = URL(authorizationInfo.redirectUrl).host,
                pathSegments = mUrl.pathSegments,
                parameters = Parameters.build {
                    append("code", authorizationCode)
                    append("state", authorizationInfo.state)
                }
            ).build()

            call.respondRedirect(redirectUrl)
        }
    }
}

private suspend fun PipelineContext<*, ApplicationCall>.respondLoginFtl(
    tenantId: Int,
    account: String = "",
    code: String,
    error: String = ""
) {
    call.respond(
        FreeMarkerContent(
            "${tenantId}/login.ftl",
            mapOf("account" to account, "code" to code, "error" to error)
        )
    )
}