package app.vatov.idserver.ext

import app.vatov.idserver.exception.IdServerException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

suspend inline fun ApplicationCall.respondException(exception: IdServerException) {
    response.status(exception.statusCode)
    respond(exception.errorResponse)
}