package app.vatov.idserver.exception

import app.vatov.idserver.response.ErrorResponse
import io.ktor.http.HttpStatusCode

class IdServerException(
    val statusCode: HttpStatusCode,
    val error: String,
    message: String
) : Exception(message) {

    val errorResponse get() = ErrorResponse(error, message ?: "")

    companion object {
        val ACCOUNT_EXIST: IdServerException
            get() = IdServerException(
                HttpStatusCode.Conflict,
                "account_exist",
                "Account is already in use"
            )
        val INTERNAL_SERVER_ERROR: IdServerException
            get() = IdServerException(
                HttpStatusCode.InternalServerError,
                "internal_server_error",
                "Something bad happened on the server."
            )
        val NOT_FOUND: IdServerException
            get() = IdServerException(
                HttpStatusCode.NotFound,
                "not_found",
                "The resource that you are looking for do not exist."
            )

        val BAD_REQUEST: IdServerException
            get() = IdServerException(
                HttpStatusCode.BadRequest, "bad_request", "Invalid request."
            )
        val FORBIDDEN: IdServerException
            get() = IdServerException(
                HttpStatusCode.Forbidden,
                "forbidden",
                "You don't have permissions to access this resource."
            )

        val INVALID_CLIENT: IdServerException
            get() = IdServerException(
                HttpStatusCode.BadRequest, "invalid_client", "Invalid client"
            )
        val INVALID_GRAND: IdServerException
            get() = IdServerException(
                HttpStatusCode.BadRequest, "invalid_grant", "Invalid grand"
            )
        val UNAUTHORIZED_CLIENT: IdServerException
            get() = IdServerException(
                HttpStatusCode.BadRequest, "unauthorized_client", "Unauthorized client"
            )
        val UNSUPPORTED_GRANT_TYPE: IdServerException
            get() = IdServerException(
                HttpStatusCode.BadRequest, "unsupported_grant_type", "Unsupported grant type"
            )
        val INVALID_SCOPE: IdServerException
            get() = IdServerException(
                HttpStatusCode.BadRequest, "invalid_scope", "Invalid scope"
            )

        val INVALID_EMAIL: IdServerException
            get() = IdServerException(
                HttpStatusCode.BadRequest, "invalid_email", "Email address is not valid."
            )
    }
}