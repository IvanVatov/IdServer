package app.vatov.idserver.routes.admin

import app.vatov.idserver.Const
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.routes.getUserOrRespondError
import app.vatov.idserver.routes.respondNotFound
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.adminWhoAmI() {

    route("admin/whoami") {

        get {

            val user = getUserOrRespondError() ?: return@get

            val result = UserRepository.getUserById(Const.Administration.TENANT_ID, user.id)

            if (result == null) {
                respondNotFound()
                return@get
            }

            call.respond(user)
        }
    }
}