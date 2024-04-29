package app.vatov.idserver.ext

import app.vatov.idserver.IDServer
import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.model.Tenant
import app.vatov.idserver.model.UserPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.host
import io.ktor.util.pipeline.PipelineContext

@Throws(IdServerException::class)
fun readParamOrRespondError(
    params: Parameters,
    key: String
): String {
    return params[key] ?: throw IdServerException.BAD_REQUEST
}

@Throws(IdServerException::class)
fun PipelineContext<*, ApplicationCall>.getUserPrincipal(): UserPrincipal {

    return call.principal<UserPrincipal>() ?: throw IdServerException.BAD_REQUEST
}

@Throws(IdServerException::class)
fun PipelineContext<*, ApplicationCall>.getTenant(): Tenant {

    val host = this.call.request.host()

    return IDServer.getTenant(host)
}

@Throws(IdServerException::class)
fun PipelineContext<*, ApplicationCall>.getIntParam(key: String): Int {

    return getIntParamOrNull(key) ?: throw IdServerException(
        HttpStatusCode.BadRequest,
        "bad_request",
        "Parameter $key is required"
    )
}

fun PipelineContext<*, ApplicationCall>.getIntParamOrNull(key: String): Int? {

    return call.request.queryParameters[key]?.toIntOrNull()
}

@Throws(IdServerException::class)
fun PipelineContext<*, ApplicationCall>.getStringParam(key: String): String {

    return call.request.queryParameters[key] ?: throw IdServerException(
        HttpStatusCode.BadRequest,
        "bad_request",
        "Parameter $key is required"
    )
}