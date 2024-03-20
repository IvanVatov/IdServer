package app.vatov.idserver.request.user

import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.model.serializers.Iso8601InstantSerializer
import app.vatov.idserver.model.serializers.MetadataSerializer
import app.vatov.idserver.util.isEmailValid
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
class UserUpdateRequest(
    val name: String? = null,
    @SerialName("given_name")
    val givenName: String? = null,
    @SerialName("family_name")
    val familyName: String? = null,
    @SerialName("middle_name")
    val middleName: String? = null,
    val nickname: String? = null,
    @SerialName("preferred_username")
    val preferredUsername: String? = null,
    val profile: String? = null,
    val picture: String? = null,
    val website: String? = null,
    val email: String? = null,
    val gender: String? = null,
    @Serializable(with = Iso8601InstantSerializer::class)
    val birthdate: Instant? = null,
    @SerialName("zoneinfo")
    val zoneInfo: String? = null,
    val locale: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    val address: String? = null,
    val roles: List<String>? = null,
    @SerialName("user_data")
    val userData: Map<String, @Serializable(with = MetadataSerializer::class) Any?>? = null,
    @SerialName("server_data")
    val serverData: Map<String, @Serializable(with = MetadataSerializer::class) Any?>? = null
)

suspend fun PipelineContext<*, ApplicationCall>.validate(userUpdateRequest: UserUpdateRequest): UserUpdateRequest? {

    if (userUpdateRequest.email != null && !isEmailValid(userUpdateRequest.email)) {
        throw IdServerException.INVALID_EMAIL
    }

    return userUpdateRequest
}