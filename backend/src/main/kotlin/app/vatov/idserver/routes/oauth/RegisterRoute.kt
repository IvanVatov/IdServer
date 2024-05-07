package app.vatov.idserver.routes.oauth

import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.ext.getTenant
import app.vatov.idserver.model.AuthorizationInfoWrapper
import app.vatov.idserver.model.User
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.request.user.UserRegistrationRequest
import app.vatov.idserver.util.generateRandomString
import app.vatov.idserver.util.isEmailValid
import app.vatov.idserver.util.isPasswordValid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*
import java.net.URL
import kotlin.collections.set

fun Routing.userRegister() {

    route(Regex("register(.+)?")) {

        get {
            val tenant = getTenant()

            val code = call.request.queryParameters.getOrFail("code")

            respondRegisterFtl(tenantId = tenant.id, code = code)
        }

        post {

            val tenant = getTenant()

            val code = call.request.queryParameters.getOrFail("code")

            var nameError: String = ""
            var accountError: String = ""
            var passwordError: String = ""

            val params = call.receiveParameters()

            val name = params["name"] ?: ""

            if (name.isEmpty()) {
                nameError ="Invalid name"
            }

            val account = params["account"] ?: ""

            if (!isEmailValid(account)) {
                accountError = "Invalid email"
            }

            val password = params["password"] ?: ""

            if (!isPasswordValid(password)) {
                passwordError = "Password must contain at least one number, one alphabet character minimum 6 characters long"
            }

            val nickname = params["nickname"] ?: ""
            val preferredUsername = params["preferredUsername"] ?: ""
            val picture = params["picture"] ?: ""
            val email = params["email"] ?: ""


            var user: User? = null

            val authorizationInfo = tenant.decryptInfoInfo(code)

            if (authorizationInfo != null && nameError.isBlank() && accountError.isBlank() && passwordError.isBlank()) {

                val userRegistrationRequest = UserRegistrationRequest(
                    account,
                    password,
                    name.ifEmpty { null },
                    nickname.ifEmpty { null },
                    preferredUsername.ifEmpty { null },
                    picture.ifEmpty { null },
                    email.ifEmpty { null }
                )

                try {
                    user = UserRepository.create(tenant.id, userRegistrationRequest)
                } catch (e: IdServerException) {
                    accountError = if (e.error == "account_exist") {
                        "Account already exist"
                    } else {
                        "Something went wrong"
                    }
                }
            }

            if (user != null && authorizationInfo != null) {

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

                return@post
            }

            respondRegisterFtl(
                tenantId = tenant.id,
                code = code,
                account = account,
                name = name,
                accountError = accountError,
                nameError = nameError,
                passwordError = passwordError
            )
        }
    }
}

private suspend fun PipelineContext<*, ApplicationCall>.respondRegisterFtl(
    tenantId: Int,
    code: String,
    account: String = "",
    name: String = "",
    accountError: String = "",
    nameError: String = "",
    passwordError: String = ""
) {
    call.respond(
        FreeMarkerContent(
            "${tenantId}/register.ftl", mapOf(
                "code" to code,
                "account" to account,
                "name" to name,
                "accountError" to accountError,
                "nameError" to nameError,
                "passwordError" to passwordError
            )
        )
    )
}