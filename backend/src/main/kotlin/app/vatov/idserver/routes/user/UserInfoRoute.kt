package app.vatov.idserver.routes.user

import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.ext.getTenant
import app.vatov.idserver.ext.getUserPrincipal
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.userInfo() {

    route("profile") {

        get {

            val tenant = getTenant()

            val user = getUserPrincipal()

            val existingUser = UserRepository.getUserById(tenant.id, user.id)

            call.respond(existingUser.getScopedInfo(user.scope))
        }
    }
}