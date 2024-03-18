package app.vatov.idserver.routes.user

import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.routes.getTenantOrRespondError
import app.vatov.idserver.routes.getUserOrRespondError
import app.vatov.idserver.routes.respondNotFound
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.userWhoAmI() {

    route("user/whoami") {

        get {

            val tenant = getTenantOrRespondError() ?: return@get

            val user = getUserOrRespondError() ?: return@get

            val existingUser = UserRepository.getUserById(tenant.id, user.id)

            if (existingUser != null) {
                call.respond(existingUser.getScopedInfo(user.scope))
            } else {
                respondNotFound()
            }
        }
    }
}