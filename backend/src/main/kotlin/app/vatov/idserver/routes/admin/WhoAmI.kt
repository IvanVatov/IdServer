package app.vatov.idserver.routes.admin

import app.vatov.idserver.Const
import app.vatov.idserver.model.UserPrincipal
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.response.ErrorResponse
import app.vatov.idserver.routes.respondNotFound
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.adminWhoAmI() {

    route("admin/whoami") {

        get {

            val userId = call.principal<UserPrincipal>()?.id

            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse.BAD_REQUEST
                )
                return@get
            }

            val user = UserRepository.getUserById(Const.Administration.TENANT_ID, userId)

            if (user != null) {
                call.respond(user)
                return@get
            }

            respondNotFound()
        }
    }
}