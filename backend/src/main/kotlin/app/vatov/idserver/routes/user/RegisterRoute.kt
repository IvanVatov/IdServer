package app.vatov.idserver.routes.user

import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.model.User
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.request.user.UserRegistrationRequest
import app.vatov.idserver.routes.getTenantOrRespondError
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.velocity.VelocityContent

fun Routing.userRegister() {

    route("user/register") {

        get {
            call.respond(
                VelocityContent(
                    "register.html", mapOf(
                        "account" to "",
                        "password" to ""
                    )
                )
            )
        }

        post {

            val tenant = getTenantOrRespondError() ?: return@post

            when (call.request.contentType()) {
                ContentType.Application.Json -> {

                    val userRegistrationRequest = call.receive<UserRegistrationRequest>()

                    val user = UserRepository.create(tenant.id, userRegistrationRequest)

                    call.respond(user)
                }

                ContentType.Application.FormUrlEncoded -> {

                    val map = HashMap<String, String>()

                    val params = call.receiveParameters()

                    val account = params["account"]
                    if (account == null) {
                        map["accountError"] = "Account is required"
                    } else {
                        map["account"] = account
                    }


                    val password = params["password"]
                    if (password == null) {
                        map["passwordError"] = "Invalid password"
                    } else {
                        map["password"] = password
                    }

                    val name = params["name"]
                    val nickname = params["nickname"]
                    val preferredUsername = params["preferredUsername"]
                    val picture = params["picture"]
                    val email = params["email"]


                    var user: User? = null

                    if (account != null && password != null) {
                        val userRegistrationRequest = UserRegistrationRequest(
                            account,
                            password,
                            name,
                            nickname,
                            preferredUsername,
                            picture,
                            email
                        )

                        try {
                            user = UserRepository.create(tenant.id, userRegistrationRequest)
                        } catch (e: IdServerException) {
                            map["accountError"] = e.message!!
                        }
                    }

                    if (user != null) {
                        // TODO return success response
                        call.respond(VelocityContent("registration_success.html", emptyMap()))
                        return@post
                    }

                    call.respond(VelocityContent("register.html", map))
                }
            }
        }
    }
}