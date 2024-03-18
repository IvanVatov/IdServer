package app.vatov.idserver.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    @SerialName("error_description")
    val description: String
) {
    companion object
    {
        val INTERNAL_SERVER_ERROR = ErrorResponse("internal_server_error", "Something bad happened on the server.")
        val NOT_FOUND = ErrorResponse("not_found", "The resource that you are looking for do not exist.")
        val BAD_REQUEST = ErrorResponse("bad_request", "Invalid request.")
        val UNAUTHORIZED = ErrorResponse("unauthorized", "You don't have access to this resource.")

        val INVALID_CLIENT = ErrorResponse("invalid_client", "Invalid client")
        val INVALID_GRAND = ErrorResponse("invalid_grant", "Invalid grand")
        val UNAUTHORIZED_CLIENT = ErrorResponse("unauthorized_client", "Unauthorized client")
        val UNSUPPORTED_GRANT_TYPE = ErrorResponse("unsupported_grant_type", "Unsupported grant type")
        val INVALID_SCOPE = ErrorResponse("invalid_scope", "Invalid scope")

        val INVALID_EMAIL = ErrorResponse("invalid_email", "Email address is not valid.")
    }
}
