package app.vatov.idserver.routes.oauth

import app.vatov.idserver.model.AuthorizationInfoWrapper
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.ext.getTenant
import app.vatov.idserver.util.generateRandomString
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.userAgent
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.util.*
import io.ktor.server.velocity.VelocityContent
import java.net.URL


fun Routing.login() {

    route(Regex("login(.+)?")) {

        get {

            val tenant = getTenant()

            val params = call.request.queryParameters

            val code = params.getOrFail("code")

            val nonce = params["nonce"]

            val model = mutableMapOf("code" to code)

            // we can show what is requested to the user

            if (nonce != null) {
                model["nonce"] = nonce
            }

            call.respond(VelocityContent("${tenant.id}/login.html", model))
        }

        post {
            val tenant = getTenant()

            val params = call.receiveParameters()

            val userName = params["account"]

            val password = params["password"]

            val authorizationInfoEncrypted = call.request.queryParameters["code"]

            if (userName == null || password == null || authorizationInfoEncrypted == null) {
                // respond error template
                return@post
            }

            val authorizationInfo = tenant.decryptInfoInfo(authorizationInfoEncrypted)

            val user = UserRepository.getByCredentials(tenant.id, userName, password)

            if (authorizationInfo == null || user == null) {
                // respond error template
                return@post
            }

            val client = tenant.getClient(authorizationInfo.clientId)

            val code = generateRandomString()

            val userAgent = call.request.userAgent() ?: "Unknown"

            client.codes[code] = AuthorizationInfoWrapper(user.id, userAgent, authorizationInfo)

            if (authorizationInfo.createdAt < System.currentTimeMillis() - 600000 ) { // 10 min
                // respond error template
                return@post
            }

            val mUrl = Url(authorizationInfo.redirectUrl)

            val redirectUrl = URLBuilder(
                protocol = if (tenant.host == "127.0.0.1") URLProtocol.HTTP else URLProtocol.HTTPS,
                host = URL(authorizationInfo.redirectUrl).host,
                pathSegments = mUrl.pathSegments,
                parameters = Parameters.build {
                    append("code", code)
                    append("state", authorizationInfo.state)
                }
            ).build()

            call.respondRedirect(redirectUrl)
        }

    }
}