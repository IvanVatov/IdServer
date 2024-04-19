package app.vatov.idserver.routes.admin

import app.vatov.idserver.Const
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.ext.getUserPrincipal
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.adminWhoAmI() {

    route("whoami") {

        get {

            val user = getUserPrincipal() ?: return@get

            //TODO: implement me!
            val result = UserRepository.getUserById(Const.Administration.TENANT_ID, user.id)

            call.respond(user)
        }
    }
}