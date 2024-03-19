package app.vatov.idserver.routes.user

import app.vatov.idserver.jsonInstance
import app.vatov.idserver.model.User
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.request.user.UserUpdateRequest
import app.vatov.idserver.routes.getTenantOrRespondError
import app.vatov.idserver.routes.getUserOrRespondError
import app.vatov.idserver.routes.respondBadRequest
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.patch
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import okhttp3.internal.toImmutableMap
import java.time.Instant

fun Route.userUpdate() {

    route("user/update") {

        patch {

            val tenant = getTenantOrRespondError() ?: return@patch

            val userPrincipal = getUserOrRespondError() ?: return@patch

            val requestJsonObject = call.receive<JsonObject>()

            val removeKeys = ArrayList<String>()

            requestJsonObject.entries.forEach {
                if (it.value == JsonNull) {
                    removeKeys.add(it.key)
                }
            }

            val request = jsonInstance.decodeFromJsonElement(
                UserUpdateRequest.serializer(),
                requestJsonObject
            )

            val user = UserRepository.getUserById(tenant.id, userPrincipal.id)
                ?: throw NullPointerException("User cannot be found")


            // TODO: Validate fields eg: profile should be url and ect.

            var userData = (request.userData?.let {
                user.userData?.plus(it) ?: it
            } ?: user.userData)?.toImmutableMap()

            userData = userData?.toMutableMap()

            userData?.let {
                val iterator = userData.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (entry.value == null) {
                        iterator.remove()
                    }
                }
            }

            val updatedUser = User(
                user.id,
                user.account,
                user.createdAt,
                name = if (removeKeys.contains("name"))
                    null
                else
                    request.name ?: user.name,
                givenName = if (removeKeys.contains("given_name"))
                    null
                else
                    request.givenName ?: user.givenName,
                familyName = if (removeKeys.contains("family_name"))
                    null
                else
                    request.familyName ?: user.familyName,
                middleName = if (removeKeys.contains("middle_name"))
                    null
                else
                    request.middleName ?: user.middleName,
                nickname = if (removeKeys.contains("nickname"))
                    null
                else
                    request.nickname ?: user.nickname,
                preferredUsername = if (removeKeys.contains("preferred_username"))
                    null
                else
                    request.preferredUsername ?: user.preferredUsername,
                profile = if (removeKeys.contains("profile"))
                    null
                else
                    request.profile ?: user.profile,
                picture = if (removeKeys.contains("picture"))
                    null
                else
                    request.picture ?: user.picture,
                website = if (removeKeys.contains("website"))
                    null
                else
                    request.website ?: user.website,
                email = if (removeKeys.contains("email"))
                    null
                else
                    request.email ?: user.email,
                emailVerified = if (request.email == null && user.email == null)
                    null
                else
                    user.emailVerified != null && request.email == null,
                gender = if (removeKeys.contains("gender"))
                    null
                else
                    request.gender ?: user.gender,
                birthdate = if (removeKeys.contains("birthdate"))
                    null
                else
                    request.birthdate ?: user.birthdate,
                zoneInfo = if (removeKeys.contains("zoneinfo"))
                    null
                else
                    request.zoneInfo ?: user.zoneInfo,
                locale = if (removeKeys.contains("locale")) null else request.locale ?: user.locale,
                phoneNumber = if (removeKeys.contains("phone_number")) null else request.phoneNumber ?: user.phoneNumber,
                phoneNumberVerified = if (request.phoneNumber == null && user.phoneNumber == null)
                    null
                else
                    user.phoneNumberVerified != null && request.phoneNumber == null,
                address = if (removeKeys.contains("address")) null else request.address ?: user.address,
                Instant.now(),
                roles = if (removeKeys.contains("roles")) // TODO: roles should not be able to be changed from here
                    null
                else
                    request.roles ?: user.roles,
                userData
            )

            val result = UserRepository.updateUser(
                tenant.id,
                updatedUser
            )

            if (result == 1) {
                call.respond(updatedUser)
            } else {
                respondBadRequest()
            }
        }
    }
}

fun isValidJson(text: String): Boolean {
    return try {
        jsonInstance.decodeFromString<Map<String, Any>>(text)
        true
    } catch (e: Throwable) {
        false
    }
}