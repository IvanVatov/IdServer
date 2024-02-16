package app.vatov.idserver.routes.user

import app.vatov.idserver.routes.getUserOrRespondError
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.userWhoAmI() {

    route("user/whoami") {

        get {

            val user = getUserOrRespondError() ?: return@get

            // TODO: Return full model

            call.respond(user)
        }
    }
}