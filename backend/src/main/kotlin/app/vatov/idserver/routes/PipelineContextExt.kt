package app.vatov.idserver.routes

import app.vatov.idserver.IDServer
import app.vatov.idserver.model.Tenant
import app.vatov.idserver.model.UserPrincipal
import app.vatov.idserver.response.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.host
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

suspend fun PipelineContext<*, ApplicationCall>.readParamOrRespondError(
    params: Parameters,
    key: String
): String? {
    val param = params[key]
    if (param != null) {
        return param
    }

    call.respond(
        HttpStatusCode.BadRequest,
        ErrorResponse.BAD_REQUEST
    )

    return null
}

suspend fun PipelineContext<*, ApplicationCall>.getUserOrRespondError(): UserPrincipal? {

    val user = call.principal<UserPrincipal>()

    if (user == null) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.BAD_REQUEST
        )
    }

    return user
}

suspend fun PipelineContext<*, ApplicationCall>.getTenantOrRespondError(): Tenant? {

    val host = this.call.request.host()

    val tenant = IDServer.getTenant(host)

    return if (tenant == null) {

        this.call.respond(
            HttpStatusCode.NotFound,
            String()
        )
        null
    } else tenant
}

suspend fun PipelineContext<*, ApplicationCall>.respondNotFound() {
    call.respond(
        HttpStatusCode.NotFound,
        ErrorResponse.NOT_FOUND
    )
}

suspend fun PipelineContext<*, ApplicationCall>.respondBadRequest() {
    call.respond(
        HttpStatusCode.BadRequest,
        ErrorResponse.BAD_REQUEST
    )
}

suspend fun PipelineContext<*, ApplicationCall>.respondForbidden() {
    call.respond(
        HttpStatusCode.Forbidden,
        ErrorResponse.FORBIDDEN
    )
}

suspend fun PipelineContext<*, ApplicationCall>.getIntParam(key: String): Int? {

    val value = getIntParamOrNull(key)

    if (value == null) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.BAD_REQUEST.copy(description = "Parameter $key is required")
        )
    }

    return value
}

fun PipelineContext<*, ApplicationCall>.getIntParamOrNull(key: String): Int? {

    return call.request.queryParameters[key]?.toIntOrNull()
}

suspend fun PipelineContext<*, ApplicationCall>.getStringParam(key: String): String? {

    val value = call.request.queryParameters[key]

    if (value == null) {
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse.BAD_REQUEST.copy(description = "Parameter $key is required")
        )
    }

    return value
}