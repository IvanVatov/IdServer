package app.vatov.idserver.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    @SerialName("error_description")
    val description: String
)