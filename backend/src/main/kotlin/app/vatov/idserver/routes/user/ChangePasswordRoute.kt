package app.vatov.idserver.routes.user

import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.request.user.UserChangePasswordRequest
import app.vatov.idserver.response.ResultResponse
import app.vatov.idserver.routes.getTenantOrRespondError
import app.vatov.idserver.routes.getUserOrRespondError
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.userChangePassword() {

    route("user/changePassword") {

        post {

            val tenant = getTenantOrRespondError() ?: return@post

            val user = getUserOrRespondError() ?: return@post

            val request = call.receive<UserChangePasswordRequest>()

            // TODO: validate password requirements


            val result = UserRepository.changeUserPassword(tenant.id, user.id, request.oldPassword, request.newPassword)

            call.respond(ResultResponse(result == 1))

            // TODO: Respond with something...
        }
    }
}