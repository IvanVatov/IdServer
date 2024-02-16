package app.vatov.idserver.database

import app.vatov.idserver.exception.IdServerException
import app.vatov.idserver.ext.toInstant
import app.vatov.idserver.jsonInstance
import app.vatov.idserver.model.User
import app.vatov.idserver.model.serializers.MetadataSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.Types
import java.time.Instant

object UserTable {

    private val _LOG = LoggerFactory.getLogger(UserTable::class.java)

    internal const val T_NAME = "user"

    internal const val C_ID = "id"
    internal const val C_ACCOUNT = "account"
    internal const val C_PASSWORD_HASH = "password_hash"
    internal const val C_CREATED_AT = "created_at"

    // OpenID Connect standard claims
    internal const val C_NAME = "name"
    internal const val C_GIVEN_NAME = "given_name"
    internal const val C_FAMILY_NAME = "family_name"
    internal const val C_MIDDLE_NAME = "middle_name"
    internal const val C_NICKNAME = "nickname"
    internal const val C_PREFERRED_USERNAME = "preferred_username"
    internal const val C_PROFILE = "profile"
    internal const val C_PICTURE = "picture"
    internal const val C_WEBSITE = "website"
    internal const val C_EMAIL = "email"
    internal const val C_EMAIL_VERIFIED = "email_verified"
    internal const val C_GENDER = "gender"
    internal const val C_BIRTHDATE = "birthdate"
    internal const val C_ZONE_INFO = "zoneinfo"
    internal const val C_LOCALE = "locale"
    internal const val C_PHONE_NUMBER = "phone_number"
    internal const val C_PHONE_NUMBER_VERIFIED = "phone_number_verified"
    internal const val C_ADDRESS = "address"
    internal const val C_UPDATED_AT = "updated_at"

    //
    internal const val C_ROLE = "role"
    internal const val C_USER_DATA = "user_data"
    internal const val C_SERVER_DATA = "server_data"


    private const val CREATE_TABLE = "CREATE TABLE $T_NAME? (" +
            "$C_ID CHAR(22) NOT NULL, " +
            "$C_ACCOUNT VARCHAR(128) NOT NULL, " +
            "$C_PASSWORD_HASH CHAR(64) NOT NULL, " +
            "$C_CREATED_AT BIGINT NOT NULL, " +
            "$C_NAME VARCHAR(64), " +
            "$C_GIVEN_NAME VARCHAR(64), " +
            "$C_FAMILY_NAME VARCHAR(64), " +
            "$C_MIDDLE_NAME VARCHAR(64), " +
            "$C_NICKNAME VARCHAR(64), " +
            "$C_PREFERRED_USERNAME VARCHAR(64), " +
            "$C_PROFILE VARCHAR(256), " +
            "$C_PICTURE VARCHAR(256), " +
            "$C_WEBSITE VARCHAR(256), " +
            "$C_EMAIL VARCHAR(128), " +
            "$C_EMAIL_VERIFIED TINYINT(1), " +
            "$C_GENDER VARCHAR(64), " +
            "$C_BIRTHDATE BIGINT, " +
            "$C_ZONE_INFO VARCHAR(64), " +
            "$C_LOCALE CHAR(8), " +
            "$C_PHONE_NUMBER VARCHAR(64), " +
            "$C_PHONE_NUMBER_VERIFIED TINYINT(1), " +
            "$C_ADDRESS VARCHAR(2048), " +
            "$C_UPDATED_AT BIGINT NOT NULL, " +
            "$C_ROLE TEXT(4096), " +
            "$C_USER_DATA TEXT(16384), " +
            "$C_SERVER_DATA TEXT(16384), " +
            "PRIMARY KEY ($C_ID), " +
            "UNIQUE INDEX $C_ACCOUNT? ($C_ACCOUNT), " +
            "UNIQUE INDEX $C_PREFERRED_USERNAME? ($C_PREFERRED_USERNAME)" +
            ") COLLATE = 'utf8_general_ci';"

    fun createTable(con: Connection, tenantId: Int) {
        con.prepareStatement(CREATE_TABLE).use { ps ->
            ps.setInt(1, tenantId)
            ps.setInt(2, tenantId)
            ps.setInt(3, tenantId)
            ps.execute()
        }
    }

    private const val CREATE = "INSERT INTO $T_NAME? (" +
            "$C_ID, $C_ACCOUNT, $C_PASSWORD_HASH, $C_CREATED_AT, $C_UPDATED_AT, " +
            "$C_NAME, $C_NICKNAME, $C_PREFERRED_USERNAME, $C_PICTURE, $C_EMAIL, $C_ROLE" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

    fun create(
        tenantId: Int,
        id: String,
        account: String,
        passwordHash: String,
        name: String?,
        nickname: String?,
        preferredUsername: String?,
        picture: String?,
        email: String?,
        createdAt: Instant,
        role: List<String>?
    ): Int? {
        var result: Int? = null

        try {
            result = create(
                Database.getConnection(),
                tenantId,
                id,
                account,
                passwordHash,
                name,
                nickname,
                preferredUsername,
                picture,
                email,
                createdAt,
                role
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            throw IdServerException.ACCOUNT_EXIST
        } catch (e: SQLException) {
            _LOG.error("create", e)
        }

        return result
    }

    fun create(
        connection: Connection,
        tenantId: Int,
        id: String,
        account: String,
        passwordHash: String,
        name: String?,
        nickname: String?,
        preferredUsername: String?,
        picture: String?,
        email: String?,
        createdAt: Instant,
        role: List<String>?
    ): Int {

        connection.use { con ->

            con.prepareStatement(CREATE).use { ps ->

                ps.setInt(1, tenantId)

                ps.setString(2, id)
                ps.setString(3, account)
                ps.setString(4, passwordHash)
                ps.setLong(5, createdAt.epochSecond)
                ps.setLong(6, createdAt.epochSecond)
                ps.setString(7, name)
                ps.setString(8, nickname)
                ps.setString(9, preferredUsername)
                ps.setString(10, picture)
                ps.setString(11, email)
                if (role != null) {
                    ps.setString(
                        12,
                        jsonInstance.encodeToString(ListSerializer(String.serializer()), role)
                    )
                } else {
                    ps.setString(12, null)
                }

                return ps.executeUpdate()
            }
        }
    }


    private const val SET_VERIFIED = "UPDATE $T_NAME? SET $C_EMAIL_VERIFIED = 1 WHERE $C_ID = ?"

    fun setVerified(tenantId: Int, id: String): Int? {
        var result: Int? = null

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(SET_VERIFIED)
                    .use { ps ->
                        ps.setInt(1, tenantId)

                        ps.setString(2, id)
                        result = ps.executeUpdate()
                    }
            }

        } catch (e: SQLException) {
            _LOG.error("setVerified", e)
        }

        return result
    }

    private const val CHANGE_PASSWORD =
        "UPDATE $T_NAME? SET $C_PASSWORD_HASH = ? WHERE $C_ID = ? AND $C_PASSWORD_HASH = ?"

    fun changePassword(
        tenantId: Int,
        id: String,
        oldPasswordHash: String,
        newPasswordHash: String
    ): Int? {
        var result: Int? = null

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(CHANGE_PASSWORD)
                    .use { ps ->
                        ps.setInt(1, tenantId)
                        ps.setString(2, newPasswordHash)

                        ps.setString(3, id)
                        ps.setString(4, oldPasswordHash)
                        result = ps.executeUpdate()
                    }
            }

        } catch (e: SQLException) {
            _LOG.error("setVerified", e)
        }

        return result
    }

    private const val SELECT_USER_BY_ACCOUNT_AND_PASSWORD = "SELECT " +
            "$C_ID, $C_ACCOUNT, $C_CREATED_AT, $C_NAME, $C_GIVEN_NAME, $C_FAMILY_NAME, " +
            "$C_MIDDLE_NAME, $C_NICKNAME, $C_PREFERRED_USERNAME, $C_PROFILE, $C_PICTURE, " +
            "$C_WEBSITE, $C_EMAIL, $C_EMAIL_VERIFIED, $C_GENDER, $C_BIRTHDATE, $C_ZONE_INFO, " +
            "$C_LOCALE, $C_PHONE_NUMBER, $C_PHONE_NUMBER_VERIFIED, $C_ADDRESS, $C_UPDATED_AT, " +
            "$C_ROLE, $C_USER_DATA, $C_SERVER_DATA " +
            "FROM $T_NAME? WHERE $C_ACCOUNT = ? AND $C_PASSWORD_HASH = ? LIMIT 1"

    fun getUserByAccountAndPassword(
        tenantId: Int,
        account: String,
        passwordHash: String
    ): User? {
        var result: User? = null

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(SELECT_USER_BY_ACCOUNT_AND_PASSWORD).use { ps ->

                    ps.setInt(1, tenantId)

                    ps.setString(2, account)
                    ps.setString(3, passwordHash)

                    ps.executeQuery().use { rs ->

                        if (rs.next()) {
                            result = User(
                                rs.getString(C_ID),
                                rs.getString(C_ACCOUNT),
                                rs.getLong(C_CREATED_AT).toInstant(),
                                rs.getString(C_NAME),
                                rs.getString(C_GIVEN_NAME),
                                rs.getString(C_FAMILY_NAME),
                                rs.getString(C_MIDDLE_NAME),
                                rs.getString(C_NICKNAME),
                                rs.getString(C_PREFERRED_USERNAME),
                                rs.getString(C_PROFILE),
                                rs.getString(C_PICTURE),
                                rs.getString(C_WEBSITE),
                                rs.getString(C_EMAIL),
                                rs.getBoolean(C_EMAIL_VERIFIED),
                                rs.getString(C_GENDER),
                                rs.getObject(C_BIRTHDATE)?.let { it as Long }?.toInstant(),
                                rs.getString(C_ZONE_INFO),
                                rs.getString(C_LOCALE),
                                rs.getString(C_PHONE_NUMBER),
                                rs.getBoolean(C_PHONE_NUMBER_VERIFIED),
                                rs.getString(C_ADDRESS),
                                rs.getLong(C_UPDATED_AT).toInstant(),
                                rs.getString(C_ROLE)?.let {
                                    jsonInstance.decodeFromString(
                                        ListSerializer(String.serializer()),
                                        it
                                    )
                                },
                                rs.getString(C_USER_DATA)?.let {
                                    jsonInstance.decodeFromString(
                                        MapSerializer(
                                            String.serializer(),
                                            MetadataSerializer
                                        ), it
                                    )
                                },
                                rs.getString(C_SERVER_DATA)?.let {
                                    jsonInstance.decodeFromString(
                                        MapSerializer(
                                            String.serializer(),
                                            MetadataSerializer
                                        ), it
                                    )
                                }
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            _LOG.error("getUserPrincipleByEmailPassword", e)
        }

        return result
    }

    private const val SELECT_USER_BY_ID = "SELECT " +
            "$C_ID, $C_ACCOUNT, $C_CREATED_AT, $C_NAME, $C_GIVEN_NAME, $C_FAMILY_NAME, " +
            "$C_MIDDLE_NAME, $C_NICKNAME, $C_PREFERRED_USERNAME, $C_PROFILE, $C_PICTURE, " +
            "$C_WEBSITE, $C_EMAIL, $C_EMAIL_VERIFIED, $C_GENDER, $C_BIRTHDATE, $C_ZONE_INFO, " +
            "$C_LOCALE, $C_PHONE_NUMBER, $C_PHONE_NUMBER_VERIFIED, $C_ADDRESS, $C_UPDATED_AT, " +
            "$C_ROLE, $C_USER_DATA, $C_SERVER_DATA " +
            "FROM $T_NAME? WHERE $C_ID = ? LIMIT 1"

    fun getByUserId(tenantId: Int, userId: String, includeServerData: Boolean = false): User? {
        var result: User? = null

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(SELECT_USER_BY_ID).use { ps ->
                    ps.setInt(1, tenantId)

                    ps.setString(2, userId)

                    ps.executeQuery().use { rs ->

                        if (rs.next()) {

                            val email = rs.getString(C_EMAIL)
                            val phoneNumber = rs.getString(C_PHONE_NUMBER)

                            result = User(
                                rs.getString(C_ID),
                                rs.getString(C_ACCOUNT),
                                rs.getLong(C_CREATED_AT).toInstant(),
                                rs.getString(C_NAME),
                                rs.getString(C_GIVEN_NAME),
                                rs.getString(C_FAMILY_NAME),
                                rs.getString(C_MIDDLE_NAME),
                                rs.getString(C_NICKNAME),
                                rs.getString(C_PREFERRED_USERNAME),
                                rs.getString(C_PROFILE),
                                rs.getString(C_PICTURE),
                                rs.getString(C_WEBSITE),
                                email,
                                if (email != null) rs.getBoolean(C_EMAIL_VERIFIED) else null,
                                rs.getString(C_GENDER),
                                rs.getObject(C_BIRTHDATE)?.let { it as Long }?.toInstant(),
                                rs.getString(C_ZONE_INFO),
                                rs.getString(C_LOCALE),
                                phoneNumber,
                                if (phoneNumber != null) rs.getBoolean(C_PHONE_NUMBER_VERIFIED) else null,
                                rs.getString(C_ADDRESS),
                                rs.getLong(C_UPDATED_AT).toInstant(),
                                rs.getString(C_ROLE)?.let {
                                    jsonInstance.decodeFromString(
                                        ListSerializer(String.serializer()),
                                        it
                                    )
                                },
                                rs.getString(C_USER_DATA)?.let {
                                    jsonInstance.decodeFromString(
                                        MapSerializer(
                                            String.serializer(),
                                            MetadataSerializer
                                        ), it
                                    )
                                },
                                if (includeServerData) {
                                    rs.getString(C_SERVER_DATA)?.let {
                                        jsonInstance.decodeFromString(
                                            MapSerializer(
                                                String.serializer(),
                                                MetadataSerializer
                                            ), it
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            _LOG.error("getByUserPrincipleId", e)
        }

        return result
    }

    private const val UPDATE_USER =
        "UPDATE $T_NAME? SET $C_PASSWORD_HASH = ? WHERE $C_ID = ?"

    fun updateUser(tenantId: Int, user: User): Int? {
        var result: Int? = null

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(
                    "UPDATE $T_NAME? SET " +
                            "$C_NAME = ?, $C_GIVEN_NAME = ?, $C_FAMILY_NAME = ?, $C_MIDDLE_NAME = ?, " +
                            "$C_NICKNAME = ?, $C_PREFERRED_USERNAME = ?, $C_PROFILE = ?, $C_PICTURE = ?, " +
                            "$C_WEBSITE = ?, $C_EMAIL = ?, $C_EMAIL_VERIFIED = ?, " +
                            "$C_GENDER = ?, $C_BIRTHDATE = ?, $C_ZONE_INFO = ?, $C_LOCALE = ?, " +
                            "$C_PHONE_NUMBER = ?, $C_PHONE_NUMBER_VERIFIED = ?, $C_ADDRESS = ?, " +
                            "$C_UPDATED_AT = ?, $C_ROLE = ?, $C_USER_DATA = ? " +
                            "WHERE $C_ID = ?"
                ).use { ps ->
                    ps.setInt(1, tenantId)

                    ps.setString(2, user.name)
                    ps.setString(3, user.givenName)
                    ps.setString(4, user.familyName)
                    ps.setString(5, user.middleName)
                    ps.setString(6, user.nickname)
                    ps.setString(7, user.preferredUsername)
                    ps.setString(8, user.profile)
                    ps.setString(9, user.picture)
                    ps.setString(10, user.website)
                    ps.setString(11, user.email)
                    if (user.emailVerified != null) {
                        ps.setBoolean(12, user.emailVerified)
                    } else {
                        ps.setNull(12, Types.TINYINT)
                    }
                    ps.setString(13, user.gender)
                    if (user.birthdate != null) {
                        ps.setLong(14, user.birthdate.epochSecond)
                    } else {
                        ps.setNull(14, Types.BIGINT)
                    }
                    ps.setString(15, user.zoneInfo)
                    ps.setString(16, user.locale)
                    ps.setString(17, user.phoneNumber)
                    if (user.phoneNumberVerified != null) {
                        ps.setBoolean(18, user.phoneNumberVerified)
                    } else {
                        ps.setNull(18, Types.TINYINT)
                    }
                    ps.setString(19, user.address)
                    ps.setLong(20, user.updatedAt.epochSecond)
                    if (user.role != null) {
                        ps.setString(
                            21,
                            jsonInstance.encodeToString(
                                ListSerializer(String.serializer()),
                                user.role
                            )
                        )
                    } else {
                        ps.setString(21, null)
                    }
                    if (user.userData != null) {
                        ps.setString(
                            22,
                            jsonInstance.encodeToString(
                                MapSerializer(
                                    String.serializer(),
                                    MetadataSerializer
                                ), user.userData
                            )
                        )
                    } else {
                        ps.setString(22, null)
                    }

                    ps.setString(23, user.id)
                    result = ps.executeUpdate()
                }
            }

        } catch (e: SQLException) {
            _LOG.error("updateUser", e)
        }

        return result
    }


    private const val SELECT_100_USERS_BY_CREATE_DATE = "SELECT " +
            "$C_ID, $C_ACCOUNT, $C_CREATED_AT, $C_NAME, $C_GIVEN_NAME, $C_FAMILY_NAME, " +
            "$C_MIDDLE_NAME, $C_NICKNAME, $C_PREFERRED_USERNAME, $C_PROFILE, $C_PICTURE, " +
            "$C_WEBSITE, $C_EMAIL, $C_EMAIL_VERIFIED, $C_GENDER, $C_BIRTHDATE, $C_ZONE_INFO, " +
            "$C_LOCALE, $C_PHONE_NUMBER, $C_PHONE_NUMBER_VERIFIED, $C_ADDRESS, $C_UPDATED_AT, " +
            "$C_ROLE, $C_USER_DATA, $C_SERVER_DATA " +
            "FROM $T_NAME? ORDER BY $C_CREATED_AT LIMIT ? OFFSET ?"

    fun getUsers(tenantId: Int, size: Int, skip: Int, by: Int?, term: String?): List<User> {

        val result = ArrayList<User>()

        try {
            Database.getConnection().use { con ->

                con.prepareStatement(SELECT_100_USERS_BY_CREATE_DATE).use { ps ->
                    ps.setInt(1, tenantId)

                    ps.setInt(2, size)

                    ps.setInt(3, skip)

                    ps.executeQuery().use { rs ->

                        while (rs.next()) {

                            val email = rs.getString(C_EMAIL)
                            val phoneNumber = rs.getString(C_PHONE_NUMBER)

                            result.add(
                                User(
                                    rs.getString(C_ID),
                                    rs.getString(C_ACCOUNT),
                                    rs.getLong(C_CREATED_AT).toInstant(),
                                    rs.getString(C_NAME),
                                    rs.getString(C_GIVEN_NAME),
                                    rs.getString(C_FAMILY_NAME),
                                    rs.getString(C_MIDDLE_NAME),
                                    rs.getString(C_NICKNAME),
                                    rs.getString(C_PREFERRED_USERNAME),
                                    rs.getString(C_PROFILE),
                                    rs.getString(C_PICTURE),
                                    rs.getString(C_WEBSITE),
                                    email,
                                    if (email != null) rs.getBoolean(C_EMAIL_VERIFIED) else null,
                                    rs.getString(C_GENDER),
                                    rs.getObject(C_BIRTHDATE)?.let { it as Long }?.toInstant(),
                                    rs.getString(C_ZONE_INFO),
                                    rs.getString(C_LOCALE),
                                    phoneNumber,
                                    if (phoneNumber != null) rs.getBoolean(C_PHONE_NUMBER_VERIFIED) else null,
                                    rs.getString(C_ADDRESS),
                                    rs.getLong(C_UPDATED_AT).toInstant(),
                                    rs.getString(C_ROLE)?.let {
                                        jsonInstance.decodeFromString(
                                            ListSerializer(String.serializer()),
                                            it
                                        )
                                    },
                                    rs.getString(C_USER_DATA)?.let {
                                        jsonInstance.decodeFromString(
                                            MapSerializer(
                                                String.serializer(),
                                                MetadataSerializer
                                            ), it
                                        )
                                    }
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            _LOG.error("getByUserPrincipleId", e)
        }

        return result
    }
}