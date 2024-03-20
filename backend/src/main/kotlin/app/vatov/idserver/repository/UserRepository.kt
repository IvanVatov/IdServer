package app.vatov.idserver.repository

import app.vatov.idserver.Const
import app.vatov.idserver.database.UserTable
import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.model.User
import app.vatov.idserver.request.user.UserRegistrationRequest
import app.vatov.idserver.util.createShortUUID
import app.vatov.idserver.util.hashSHA256
import java.time.Instant

object UserRepository {

    fun create(
        tenantId: Int,
        request: UserRegistrationRequest
    ): User {

        val uuid = createShortUUID()

        val createdAt = Instant.now()

        val defaultRole = listOf(Const.OpenIdRole.USER)

        val result = UserTable.create(
            tenantId,
            uuid,
            request.account,
            hashSHA256(request.password),
            request.name,
            request.nickname,
            request.preferredUsername,
            request.picture,
            request.email,
            createdAt,
            defaultRole
        )

        if (result == 1) {
            return User(
                id = uuid,
                account = request.account,
                name = request.name,
                nickname = request.nickname,
                preferredUsername = request.preferredUsername,
                picture = request.picture,
                email = request.email,
                createdAt = createdAt,
                updatedAt = createdAt,
                roles = defaultRole
            )
        }
        throw Exception("Couldn't store the user account")
    }

    fun changeUserPassword(
        tenantId: Int,
        account: String,
        oldPassword: String,
        newPassword: String
    ): Int? {
        return UserTable.changePassword(
            tenantId,
            account,
            hashSHA256(oldPassword),
            hashSHA256(newPassword)
        )
    }

    fun getByCredentials(tenantId: Int, userName: String, password: String): User? {

        return UserTable.getUserByAccountAndPassword(tenantId, userName, hashSHA256(password))
    }

    @Throws(IdServerException::class)
    fun getUserById(tenantId: Int, userId: String): User {

        return UserTable.getByUserId(tenantId, userId) ?: throw IdServerException.NOT_FOUND
    }

    fun updateUser(tenantId: Int, user: User): Int? {
        return UserTable.updateUser(tenantId, user)
    }

    fun getUsers(
        tenantId: Int,
        size: Int,
        skip: Int,
        by: Int? = null,
        term: String? = null
    ): List<User> {


        return UserTable.getUsers(tenantId, size, skip, by, term)
    }
}