package app.vatov.idserver.repository

import app.vatov.idserver.database.RefreshTokenTable
import app.vatov.idserver.database.UserTable
import app.vatov.idserver.model.User
import app.vatov.idserver.model.serializers.RefreshTokenInfo
import app.vatov.idserver.util.createShortUUID
import app.vatov.idserver.util.hashSHA256
import java.time.Instant

object RefreshTokenRepository {

    fun create(
        tenantId: Int,
        userId: String,
        clientId: String,
        scope: List<String>,
        userAgent: String
    ): String {

        val refreshToken = createRefreshToken(userId)

        val result = RefreshTokenTable.insert(
            RefreshTokenInfo(tenantId, userId, refreshToken, clientId, scope, userAgent = userAgent)
        )

        if (result) {
            return refreshToken
        }
        throw Exception("Couldn't store refresh token")
    }

    fun update(
        tenantId: Int,
        userId: String,
        oldRefreshToken: String
    ): String {

        val refreshToken = createRefreshToken(userId)

        if (RefreshTokenTable.update(tenantId, refreshToken, Instant.now(), oldRefreshToken)) {
            return refreshToken
        }

        throw Exception("Couldn't store refresh token")
    }

    fun getUserAndRefreshTokenInfoByRefreshToken(
        tenantId: Int,
        refreshToken: String
    ): Pair<User, RefreshTokenInfo>? {

        val refreshTokenInfo =
            RefreshTokenTable.getByRefreshToken(tenantId, refreshToken)
                ?: return null

        val userPrincipal =
            UserTable.getByUserId(tenantId, refreshTokenInfo.userId) ?: return null

        return Pair(userPrincipal, refreshTokenInfo)
    }

    private fun createRefreshToken(userId: String): String {
        return hashSHA256("$userId:${createShortUUID()}")
    }
}