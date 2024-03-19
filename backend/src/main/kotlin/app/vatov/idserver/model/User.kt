package app.vatov.idserver.model

import app.vatov.idserver.Const
import app.vatov.idserver.model.serializers.Iso8601InstantSerializer
import app.vatov.idserver.model.serializers.MetadataSerializer
import app.vatov.idserver.model.serializers.toIso8601
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
    val roles: List<String>?,
    @SerialName("user_data")
    val userData: Map<String, @Serializable(with = MetadataSerializer::class) Any?>? = null,
    @SerialName("server_data")
    val serverData: Map<String, @Serializable(with = MetadataSerializer::class) Any?>? = null,
) {
    fun getScopedInfo(scope: List<String>) : JsonObject {
        val map = LinkedHashMap<String, JsonElement>()

        map[Const.OpenIdClaim.SUB] = JsonPrimitive(id)

        if (scope.contains(Const.OpenIdScope.PROFILE)) {
            name?.let {
                map[Const.OpenIdClaim.NAME] = JsonPrimitive(it)
            }
            familyName?.let {
                map[Const.OpenIdClaim.FAMILY_NAME] = JsonPrimitive(it)
            }
            givenName?.let {
                map[Const.OpenIdClaim.GIVEN_NAME] = JsonPrimitive(it)
            }
            middleName?.let {
                map[Const.OpenIdClaim.MIDDLE_NAME] = JsonPrimitive(it)
            }
            nickname?.let {
                map[Const.OpenIdClaim.NICKNAME] = JsonPrimitive(it)
            }
            preferredUsername?.let {
                map[Const.OpenIdClaim.PREFERRED_USERNAME] = JsonPrimitive(it)
            }
            profile?.let {
                map[Const.OpenIdClaim.PROFILE] = JsonPrimitive(it)
            }
            picture?.let {
                map[Const.OpenIdClaim.PICTURE] = JsonPrimitive(it)
            }
            website?.let {
                map[Const.OpenIdClaim.WEBSITE] = JsonPrimitive(it)
            }
            gender?.let {
                map[Const.OpenIdClaim.GENDER] = JsonPrimitive(it)
            }
            birthdate?.let {
                map[Const.OpenIdClaim.BIRTH_DATE] = JsonPrimitive(it.toIso8601())
            }
            zoneInfo?.let {
                map[Const.OpenIdClaim.ZONE_INFO] = JsonPrimitive(it)
            }
            locale?.let {
                map[Const.OpenIdClaim.LOCALE] = JsonPrimitive(it)
            }
            map[Const.OpenIdClaim.UPDATED_AT] = JsonPrimitive(updatedAt.epochSecond)
        }

        if (scope.contains(Const.OpenIdScope.EMAIL)) {
            email?.let {
                map[Const.OpenIdClaim.EMAIL] = JsonPrimitive(it)
                map[Const.OpenIdClaim.EMAIL_VERIFIED] = JsonPrimitive(emailVerified ?: false)
            }
        }

        if (scope.contains(Const.OpenIdScope.ADDRESS)) {
            address?.let {
                map[Const.OpenIdClaim.ADDRESS] = JsonPrimitive(it)
            }
        }

        if (scope.contains(Const.OpenIdScope.PHONE)) {
            phoneNumber?.let {
                map[Const.OpenIdClaim.PHONE_NUMBER] = JsonPrimitive(it)
                map[Const.OpenIdClaim.PHONE_NUMBER_VERIFIED] =
                    JsonPrimitive(phoneNumberVerified ?: false)
            }
        }


        return JsonObject(map)

    }
}