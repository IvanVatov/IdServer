package app.vatov.idserver.routes.admin

import app.vatov.idserver.jsonInstance
import app.vatov.idserver.model.User
import app.vatov.idserver.model.UserPrincipal
import app.vatov.idserver.repository.UserRepository
import app.vatov.idserver.request.user.UserRegistrationRequest
import app.vatov.idserver.request.user.UserUpdateRequest
import app.vatov.idserver.request.user.validate
import app.vatov.idserver.response.ErrorResponse
import app.vatov.idserver.routes.getIntParam
import app.vatov.idserver.routes.getIntParamOrNull
import app.vatov.idserver.routes.getStringParam
import app.vatov.idserver.routes.respondBadRequest
import app.vatov.idserver.routes.respondNotFound
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
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

            val userPrincipleId = call.principal<UserPrincipal>()?.id

            if (userPrincipleId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse.BAD_REQUEST
                )
                return@get
            }

            // todo check for access


            val tenantId = getIntParam("tenantId") ?: return@get

            val size = getIntParamOrNull("size") ?: 100

            val skip = getIntParamOrNull("skip") ?: 0

            val users = UserRepository.getUsers(tenantId, size, skip)

            call.respond(users)
        }
    }

    route("admin/user") {

        get {
            val userPrincipleId = call.principal<UserPrincipal>()?.id

            if (userPrincipleId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse.BAD_REQUEST
                )
                return@get
            }

            // todo check for access


            val tenantId = getIntParam("tenantId") ?: return@get

            val userId = getStringParam("userId") ?: return@get

            val user = UserRepository.getUserById(tenantId, userId)

            if (user == null) {
                respondNotFound()
                return@get
            }

            call.respond(user)
        }

        post {
            val tenantId = getIntParam("tenantId") ?: return@post

            val userRegistrationRequest = call.receive<UserRegistrationRequest>()

            val user = UserRepository.create(tenantId, userRegistrationRequest)

            call.respond(user)
        }

        post("patch") {

            val tenantId = getIntParam("tenantId") ?: return@post

            val userId = getStringParam("userId") ?: return@post

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

            val user = UserRepository.getUserById(tenantId, userId)
                ?: throw NullPointerException("User cannot be found")
            // TODO: Return not found without exception


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
                phoneNumber = if (removeKeys.contains("phone_number")) null else request.phoneNumber
                    ?: user.phoneNumber,
                phoneNumberVerified = if (request.phoneNumber == null && user.phoneNumber == null)
                    null
                else
                    user.phoneNumberVerified != null && request.phoneNumber == null,
                address = if (removeKeys.contains("address")) null else request.address
                    ?: user.address,
                Instant.now(),
                role = if (removeKeys.contains("role"))
                    null
                else
                    request.role ?: user.role,
                userData = if (removeKeys.contains("user_data"))
                    null
                else
                    request.userData ?: user.userData,
                serverData = if (removeKeys.contains("server_data"))
                    null
                else
                    request.serverData ?: user.serverData
            )

            val result = UserRepository.updateUser(
                tenantId,
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