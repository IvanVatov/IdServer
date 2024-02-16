package app.vatov.idserver.database

import app.vatov.idserver.jsonInstance
import app.vatov.idserver.model.ClientPrincipal
import app.vatov.idserver.model.ClientSettings
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

object ClientTable {

    private val _LOG = LoggerFactory.getLogger(ClientTable::class.java)

    internal const val T_NAME = "client"

    internal const val C_ID = "id"
    internal const val C_APPLICATION = "application"
    internal const val C_SECRET = "secret"
    internal const val C_SETTINGS = "settings"

    fun createTable(con: Connection, tenantId: Int) {
        con.prepareStatement(
            """CREATE TABLE $T_NAME? (
                        $C_ID VARCHAR(128) NOT NULL,
                        $C_APPLICATION VARCHAR(128) NOT NULL,
                        $C_SECRET VARCHAR(64) NOT NULL,
                        $C_SETTINGS VARCHAR(16384) NOT NULL DEFAULT '{}',
                        PRIMARY KEY ($C_ID),
                        UNIQUE INDEX $C_APPLICATION? ($C_APPLICATION)
                    ) COLLATE='utf8_general_ci';"""
        ).use { ps ->

            ps.setInt(1, tenantId)
            ps.setInt(2, tenantId)

            ps.execute()
        }
    }

    fun getByClientIdAndSecret(
        tenantId: Int,
        clientId: String,
        clientSecret: String
    ): ClientPrincipal? {
        var result: ClientPrincipal? = null

        try {
            Database.getConnection().use { con ->

                con.prepareStatement("SELECT $C_ID, $C_APPLICATION, $C_SECRET, $C_SETTINGS FROM $T_NAME? WHERE $C_ID = ? AND $C_SECRET = ?")
                    .use { ps ->

                        ps.setInt(1, tenantId)

                        ps.setString(2, clientId)
                        ps.setString(3, clientSecret)

                        ps.executeQuery().use { rs ->

                            if (rs.next()) {
                                result = ClientPrincipal(
                                    tenantId,
                                    rs.getString(C_ID),
                                    rs.getString(C_APPLICATION),
                                    rs.getString(C_SECRET),
                                    jsonInstance.decodeFromString(
                                        ClientSettings.serializer(),
                                        rs.getString(C_SETTINGS)
                                    ),
                                )
                            }
                        }
                    }
            }
        } catch (e: SQLException) {
            _LOG.error("getByClientByIdAndSecret", e)
        }

        return result
    }

    fun create(
        connection: Connection,
        tenantId: Int,
        id: String,
        application: String,
        secret: String,
        clientSettings: ClientSettings
    ): Boolean {
        var result: Boolean = false

        connection.use { con ->
            con.prepareStatement(
                "INSERT INTO $T_NAME? ($C_ID, $C_APPLICATION, $C_SECRET, $C_SETTINGS) VALUES (?, ?, ?, ?)"
            ).use { ps ->
                ps.setInt(1, tenantId)

                ps.setString(2, id)
                ps.setString(3, application)
                ps.setString(4, secret)
                ps.setString(
                    5,
                    jsonInstance.encodeToString(ClientSettings.serializer(), clientSettings)
                )

                result = ps.executeUpdate() == 1
            }
        }

        return result
    }

    fun create(
        tenantId: Int,
        id: String,
        application: String,
        secret: String
    ): Boolean {
        var result: Boolean = false

        try {
            Database.getConnection().use { con ->
                result = create(con, tenantId, id, application, secret, ClientSettings())
            }
        } catch (e: SQLException) {
            _LOG.error("create", e)
        }

        return result
    }

    fun delete(
        tenantId: Int, keyId: String
    ): Boolean {
        var result: Boolean = false

        try {
            Database.getConnection().use { con ->

                result = delete(con, tenantId, keyId)
            }
        } catch (e: SQLException) {
            _LOG.error("delete", e)
        }

        return result
    }

    fun delete(con: Connection, tenantId: Int, clientId: String): Boolean {
        con.prepareStatement(
            "DELETE FROM $T_NAME? WHERE $C_ID = ?"
        ).use { ps ->
            ps.setInt(1, tenantId)
            ps.setString(2, clientId)

            return ps.executeUpdate() == 1
        }
    }

    fun update(
        clientPrincipal: ClientPrincipal
    ): Boolean {
        var result: Boolean = false

        try {
            Database.getConnection().use { con ->

                con.prepareStatement("UPDATE $T_NAME? SET $C_APPLICATION = ?, $C_SECRET = ?, $C_SETTINGS = ? WHERE $C_ID = ?")
                    .use { ps ->

                        ps.setInt(1, clientPrincipal.tenantId)

                        ps.setString(2, clientPrincipal.application)
                        ps.setString(3, clientPrincipal.clientSecret)
                        ps.setString(
                            4,
                            jsonInstance.encodeToString(
                                ClientSettings.serializer(),
                                clientPrincipal.settings
                            )
                        )

                        ps.setString(5, clientPrincipal.clientId)

                        result = ps.executeUpdate() == 1
                    }
            }
        } catch (e: SQLException) {
            _LOG.error("update", e)
        }

        return result
    }


    fun getAllByTenantId(tenantId: Int): List<ClientPrincipal> {
        val result: ArrayList<ClientPrincipal> = ArrayList()

        try {
            Database.getConnection().use { con ->
                result.addAll(getAllByTenantId(con, tenantId))
            }
        } catch (e: SQLException) {
            _LOG.error("getAllByClientId", e)
        }

        return result
    }


    fun getAllByTenantId(con: Connection, tenantId: Int): List<ClientPrincipal> {
        val result: ArrayList<ClientPrincipal> = ArrayList()

        con.prepareStatement("SELECT $C_ID, $C_APPLICATION, $C_SECRET, $C_SETTINGS FROM $T_NAME?")
            .use { ps ->

                ps.setInt(1, tenantId)

                ps.executeQuery().use { rs ->

                    while (rs.next()) {
                        result.add(
                            ClientPrincipal(
                                tenantId,
                                rs.getString(C_ID),
                                rs.getString(C_APPLICATION),
                                rs.getString(C_SECRET),
                                jsonInstance.decodeFromString(
                                    ClientSettings.serializer(),
                                    rs.getString(C_SETTINGS)
                                ),
                            )
                        )
                    }
                }
            }

        return result
    }

    fun getClientIdByTenantId(
        tenantId: Int,
        clientId: String
    ): ClientPrincipal? {
        var result: ClientPrincipal? = null

        try {
            Database.getConnection().use { con ->

                con.prepareStatement("SELECT $C_ID, $C_APPLICATION, $C_SECRET, $C_SETTINGS FROM $T_NAME? WHERE $C_ID = ?")
                    .use { ps ->

                        ps.setInt(1, tenantId)

                        ps.setString(2, clientId)

                        ps.executeQuery().use { rs ->

                            if (rs.next()) {
                                result = ClientPrincipal(
                                    tenantId,
                                    rs.getString(C_ID),
                                    rs.getString(C_APPLICATION),
                                    rs.getString(C_SECRET),
                                    jsonInstance.decodeFromString(
                                        ClientSettings.serializer(),
                                        rs.getString(C_SETTINGS)
                                    ),
                                )
                            }
                        }
                    }
            }
        } catch (e: SQLException) {
            _LOG.error("getClientIdByTenantId", e)
        }

        return result
    }

}