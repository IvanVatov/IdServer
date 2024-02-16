package app.vatov.idserver.database


import app.vatov.idserver.ext.toInstant
import app.vatov.idserver.model.serializers.RefreshTokenInfo
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.time.Instant

object RefreshTokenTable {

    private val _LOG = LoggerFactory.getLogger(RefreshTokenTable::class.java)

    const val T_NAME = "refresh_token"

    const val C_USER_ID = "user_id"
    const val C_REFRESH_TOKEN = "refresh_token"
    const val C_CLIENT_ID = "client_id"
    const val C_SCOPE = "scope"
    const val C_CREATED_AT = "created_at"
    const val C_REFRESHED_AT = "refreshed_at"
    const val C_USER_AGENT = "user_agent"

    private const val Q_CREATE_TABLE = "CREATE TABLE $T_NAME? (" +
            "$C_USER_ID CHAR(22) NOT NULL, " +
            "$C_REFRESH_TOKEN CHAR(64) NOT NULL, " +
            "$C_CLIENT_ID VARCHAR(128) NOT NULL, " +
            "$C_SCOPE VARCHAR(8192) DEFAULT '', " +
            "$C_CREATED_AT BIGINT NOT NULL, " +
            "$C_REFRESHED_AT BIGINT NOT NULL, " +
            "$C_USER_AGENT VARCHAR(1024) DEFAULT 'Unknown', " +
            "PRIMARY KEY ($C_REFRESH_TOKEN), " +
            "CONSTRAINT FK_refresh_token?_user? FOREIGN KEY ($C_USER_ID) " +
            "REFERENCES ${UserTable.T_NAME}? (${UserTable.C_ID})" +
            ") COLLATE='utf8_general_ci';"

    fun createTable(con: Connection, tenantId: Int) {
        con.prepareStatement(Q_CREATE_TABLE).use { ps ->

            ps.setInt(1, tenantId)
            ps.setInt(2, tenantId)
            ps.setInt(3, tenantId)
            ps.setInt(4, tenantId)

            ps.execute()
        }
    }

    private const val Q_INSERT =
        "INSERT INTO $T_NAME? (" +
                "$C_USER_ID, $C_REFRESH_TOKEN, $C_CLIENT_ID, $C_SCOPE, $C_CREATED_AT, $C_REFRESHED_AT, $C_USER_AGENT) " +
                "VALUES (?, ?, ?, ?, ?, ? ,?)"

    fun insert(
        refreshTokenInfo: RefreshTokenInfo
    ): Boolean {
        var result: Boolean = false

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(Q_INSERT).use { ps ->
                    ps.setInt(1, refreshTokenInfo.tenantId)

                    val scopeString = refreshTokenInfo.scope.joinToString(" ")

                    val now = refreshTokenInfo.createdAt.epochSecond
                    ps.setString(2, refreshTokenInfo.userId)
                    ps.setString(3, refreshTokenInfo.refreshToken)
                    ps.setString(4, refreshTokenInfo.clientId)
                    ps.setString(5, scopeString)
                    ps.setLong(6, now)
                    ps.setLong(7, now)
                    ps.setString(8, scopeString)

                    result = ps.executeUpdate() == 1
                }
            }
        } catch (e: SQLException) {
            _LOG.error("insert", e)
        }

        return result
    }

    private const val Q_DELETE_ALL_FOR_USER = "DELETE $T_NAME? WHERE $C_USER_ID = ?"

    fun deleteAllForeUser(tenantId: Int, userId: String): Boolean {
        var result: Boolean = false

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(Q_DELETE_ALL_FOR_USER)
                    .use { ps ->

                        ps.setInt(1, tenantId)

                        ps.setString(2, userId)
                        result = ps.executeUpdate() == 1
                    }
            }

        } catch (e: SQLException) {
            _LOG.error("deleteAllForeUser", e)
        }

        return result
    }

    private const val Q_UPDATE =
        "UPDATE $T_NAME? SET " +
                "$C_REFRESH_TOKEN = ?, $C_REFRESHED_AT = ? " +
                "WHERE $C_REFRESH_TOKEN = ?"

    fun update(
        tenantId: Int,
        newRefreshToken: String,
        refreshTime: Instant,
        oldRefreshToken: String
    ): Boolean {
        var result: Boolean = false

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(Q_UPDATE).use { ps ->
                    ps.setInt(1, tenantId)
                    ps.setString(2, newRefreshToken)
                    ps.setLong(3, refreshTime.epochSecond)
                    ps.setString(4, oldRefreshToken)

                    result = ps.executeUpdate() == 1
                }
            }
        } catch (e: SQLException) {
            _LOG.error("update", e)
        }

        return result
    }


    private const val Q_GET_BY_REFRESH_TOKEN =
        "SELECT $C_USER_ID, $C_CLIENT_ID, $C_SCOPE, $C_CREATED_AT, $C_REFRESHED_AT, $C_USER_AGENT FROM $T_NAME? WHERE $C_REFRESH_TOKEN = ?"

    fun getByRefreshToken(
        tenantId: Int,
        refreshToken: String
    ): RefreshTokenInfo? {
        var result: RefreshTokenInfo? = null

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(Q_GET_BY_REFRESH_TOKEN).use { ps ->
                    ps.setInt(1, tenantId)

                    ps.setString(2, refreshToken)

                    ps.executeQuery().use { rs ->

                        if (rs.next()) {
                            result =
                                RefreshTokenInfo(
                                    tenantId,
                                    rs.getString(C_USER_ID),
                                    refreshToken,
                                    rs.getString(C_CLIENT_ID),
                                    rs.getString(C_SCOPE).split(" "),
                                    rs.getLong(C_CREATED_AT).toInstant(),
                                    rs.getLong(C_REFRESHED_AT).toInstant(),
                                    rs.getString(C_USER_AGENT)
                                )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            _LOG.error("getByRefreshToken", e)
        }

        return result
    }
}