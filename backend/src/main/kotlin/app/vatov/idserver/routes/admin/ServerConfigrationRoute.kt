package app.vatov.idserver.routes.admin

import app.vatov.idserver.model.ServerConfiguration
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.serverConfiguration() {

    route("admin/configuration") {

        get {
            call.respond(ServerConfiguration())
        }
    }
}