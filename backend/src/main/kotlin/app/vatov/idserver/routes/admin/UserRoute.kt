package app.vatov.idserver.routes.admin

import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.ext.checkAuthorizedAdmin
import app.vatov.idserver.jsonInstance
import app.vatov.idserver.model.User
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.request.user.UserRegistrationRequest
import app.vatov.idserver.request.user.UserUpdateRequest
import app.vatov.idserver.request.user.validate
import app.vatov.idserver.routes.getIntParam
import app.vatov.idserver.routes.getIntParamOrNull
import app.vatov.idserver.routes.getStringParam
import app.vatov.idserver.routes.getUserPrincipal
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import java.time.Instant

fun Route.adminUsers() {

    route("admin/user/list") {

        get {

            val user = getUserPrincipal()

            val tenantId = getIntParam("tenantId")

            user.checkAuthorizedAdmin(tenantId)

            val size = getIntParamOrNull("size") ?: 100

            val skip = getIntParamOrNull("skip") ?: 0

            val users = UserRepository.getUsers(tenantId, size, skip)

            call.respond(users)
        }
    }

    route("admin/user") {

        get {
            val user = getUserPrincipal()

            val tenantId = getIntParam("tenantId")

            user.checkAuthorizedAdmin(tenantId)

            val userId = getStringParam("userId")

            val result = UserRepository.getUserById(tenantId, userId) ?: throw IdServerException.NOT_FOUND

            call.respond(result)
        }

        post {
            val user = getUserPrincipal()

            val tenantId = getIntParam("tenantId")

            user.checkAuthorizedAdmin(tenantId)

            val userRegistrationRequest = call.receive<UserRegistrationRequest>()

            val result = UserRepository.create(tenantId, userRegistrationRequest)

            call.respond(result)
        }

        post("patch") {
            val user = getUserPrincipal()

            val tenantId = getIntParam("tenantId")

            user.checkAuthorizedAdmin(tenantId)

            val userId = getStringParam("userId")

            val requestJsonObject = call.receive<JsonObject>()

            val removeKeys = ArrayList<String>()

            requestJsonObject.entries.forEach {
                if (it.value == JsonNull) {
                    removeKeys.add(it.key)
                }
            }

            val request = validate(
                jsonInstance.decodeFromJsonElement(
                    UserUpdateRequest.serializer(),
                    requestJsonObject
                )
            ) ?: return@post

            val oldUser = UserRepository.getUserById(tenantId, userId)
                ?: throw NullPointerException("User cannot be found")
            // TODO: Return not found without exception


            val updatedUser = User(
                oldUser.id,
                oldUser.account,
                oldUser.createdAt,
                name = if (removeKeys.contains("name"))
                    null
                else
                    request.name ?: oldUser.name,
                givenName = if (removeKeys.contains("given_name"))
                    null
                else
                    request.givenName ?: oldUser.givenName,
                familyName = if (removeKeys.contains("family_name"))
                    null
                else
                    request.familyName ?: oldUser.familyName,
                middleName = if (removeKeys.contains("middle_name"))
                    null
                else
                    request.middleName ?: oldUser.middleName,
                nickname = if (removeKeys.contains("nickname"))
                    null
                else
                    request.nickname ?: oldUser.nickname,
                preferredUsername = if (removeKeys.contains("preferred_username"))
                    null
                else
                    request.preferredUsername ?: oldUser.preferredUsername,
                profile = if (removeKeys.contains("profile"))
                    null
                else
                    request.profile ?: oldUser.profile,
                picture = if (removeKeys.contains("picture"))
                    null
                else
                    request.picture ?: oldUser.picture,
                website = if (removeKeys.contains("website"))
                    null
                else
                    request.website ?: oldUser.website,
                email = if (removeKeys.contains("email"))
                    null
                else
                    request.email ?: oldUser.email,
                emailVerified = if (request.email == null && oldUser.email == null)
                    null
                else
                    oldUser.emailVerified != null && request.email == null,
                gender = if (removeKeys.contains("gender"))
                    null
                else
                    request.gender ?: oldUser.gender,
                birthdate = if (removeKeys.contains("birthdate"))
                    null
                else
                    request.birthdate ?: oldUser.birthdate,
                zoneInfo = if (removeKeys.contains("zoneinfo"))
                    null
                else
                    request.zoneInfo ?: oldUser.zoneInfo,
                locale = if (removeKeys.contains("locale")) null else request.locale
                    ?: oldUser.locale,
                phoneNumber = if (removeKeys.contains("phone_number")) null else request.phoneNumber
                    ?: oldUser.phoneNumber,
                phoneNumberVerified = if (request.phoneNumber == null && oldUser.phoneNumber == null)
                    null
                else
                    oldUser.phoneNumberVerified != null && request.phoneNumber == null,
                address = if (removeKeys.contains("address")) null else request.address
                    ?: oldUser.address,
                Instant.now(),
                roles = if (removeKeys.contains("roles"))
                    null
                else
                    request.roles ?: oldUser.roles,
                userData = if (removeKeys.contains("user_data"))
                    null
                else
                    request.userData ?: oldUser.userData,
                serverData = if (removeKeys.contains("server_data"))
                    null
                else
                    request.serverData ?: oldUser.serverData
            )

            val result = UserRepository.updateUser(
                tenantId,
                updatedUser
            )

            if (result == 1) {
                call.respond(updatedUser)
            } else {
                throw IdServerException.BAD_REQUEST
            }
        }
    }
}