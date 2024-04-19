package app.vatov.idserver.routes.admin

import app.vatov.idserver.model.ServerConfiguration
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.serverConfiguration() {

    get("configuration") {
        call.respond(ServerConfiguration())
    }
}