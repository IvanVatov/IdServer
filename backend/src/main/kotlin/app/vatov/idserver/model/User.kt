package app.vatov.idserver.model

import app.vatov.idserver.model.serializers.Iso8601InstantSerializer
import app.vatov.idserver.model.serializers.MetadataSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class User(
    val id: String,
    val account: String,
    @SerialName("created_at")
    @Serializable(with = Iso8601InstantSerializer::class)
    val createdAt: Instant,
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
    @SerialName("email_verified")
    val emailVerified: Boolean? = null,
    val gender: String? = null,
    @Serializable(with = Iso8601InstantSerializer::class)
    val birthdate: Instant? = null,
    @SerialName("zoneinfo")
    val zoneInfo: String? = null,
    val locale: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("phone_number_verified")
    val phoneNumberVerified: Boolean? = null,
    val address: String? = null,
    @SerialName("updated_at")
    @Serializable(with = Iso8601InstantSerializer::class)
    val updatedAt: Instant,
    val role: List<String>?,
    @SerialName("user_data")
    val userData: Map<String, @Serializable(with = MetadataSerializer::class) Any?>? = null,
    @SerialName("server_data")
    val serverData: Map<String, @Serializable(with = MetadataSerializer::class) Any?>? = null,
)