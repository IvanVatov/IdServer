package app.vatov.idserver.database

import app.vatov.idserver.Configuration
import app.vatov.idserver.model.RSAKeyPair
import app.vatov.idserver.model.Tenant
import app.vatov.idserver.model.TenantRSAKeyPair
import app.vatov.idserver.model.encodeToString
import app.vatov.idserver.util.createShortUUID
import io.ktor.util.logging.*
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.time.Instant

object TenantTable {

    private val _LOG = LoggerFactory.getLogger(TenantTable::class.java)

    internal const val T_NAME = "tenant"

    internal const val C_ID = "id"
    internal const val C_NAME = "name"
    internal const val C_HOST = "host"
    internal const val C_ALIASES = "aliases"


    fun createTable(con: Connection) {
        con.prepareStatement(
            """CREATE TABLE `$T_NAME` (
                      `$C_ID` INT NOT NULL AUTO_INCREMENT,
                      `$C_NAME` VARCHAR(128) NOT NULL,
                      `$C_HOST` VARCHAR(128) NOT NULL,
                      `$C_ALIASES` VARCHAR(4096) DEFAULT '',
                      PRIMARY KEY (`$C_ID`),
                      UNIQUE INDEX `$C_HOST` (`$C_HOST`)
                   ) COLLATE='utf8_general_ci';"""
        ).use { ps ->
            ps.execute()
        }
    }


    fun createTenantTransaction(name: String, host: String): Tenant? {
        return createTenantTransaction(Database.getConnection(), name, host, null)
    }

    fun createTenantTransaction(connection: Connection, name: String, host: String, adminTenantId: Int? = null): Tenant? {

        var result: Tenant? = null

        var newId: Int? = null

        val con2 = Database.getConnection()

        connection.use { con ->

            con.autoCommit = false
            try {

                newId = insert(con, name, host, adminTenantId)

                val tenantId = newId
                    ?: throw Exception("tenantId wasn't returned from the database")


                val id = createShortUUID()
                val rsaKeyPair = RSAKeyPair.generate(Configuration.JWT_SIGNING_KEY_SIZE)

                val tenantRSAKeyPair = TenantRSAKeyPair(
                    id,
                    tenantId,
                    rsaKeyPair.publicKey.encodeToString(),
                    rsaKeyPair.privateKey.encodeToString(),
                    Instant.now()
                )

                TenantRSAKeyPairTable.insert(con, tenantRSAKeyPair)

                result = Tenant(tenantId, name, host, emptyList()).apply {
                    setValidRSAKeyPairs(listOf(tenantRSAKeyPair))
                }

                UserTable.createTable(con2, tenantId)

                RefreshTokenTable.createTable(con2, tenantId)

                ClientTable.createTable(con2, tenantId)

                con.commit()
            } catch (ex: Exception) {

                _LOG.error(ex)

                con.rollback()

                newId?.let {
                    try {
                        con2.prepareStatement("DROP TABLE IF EXISTS ${RefreshTokenTable.T_NAME}?")
                            .use { ps ->
                                ps.setInt(1, it)
                                ps.execute()
                            }
                        con2.prepareStatement("DROP TABLE IF EXISTS ${ClientTable.T_NAME}?")
                            .use { ps ->
                                ps.setInt(1, it)
                                ps.execute()
                            }
                        con2.prepareStatement("DROP TABLE IF EXISTS ${UserTable.T_NAME}?")
                            .use { ps ->
                                ps.setInt(1, it)
                                ps.execute()
                            }
                    } catch (e: Exception) {
                        _LOG.error("Revert createTenantTransaction exception", e)
                    }
                }

                result = null
            } finally {
                con.autoCommit = true
                con2.close()
            }
        }

        return result
    }


    fun insert(name: String, host: String): Int? {
        var result: Int? = null

        try {
            Database.getConnection().use { con ->

                result = insert(con, name, host, null)
            }
        } catch (e: SQLException) {
            _LOG.error("insert", e)
        }

        return result
    }


    fun insert(con: Connection, name: String, host: String, id: Int?): Int? {

        if (id == null)
            con.prepareStatement(
                "INSERT INTO $T_NAME ($C_NAME, $C_HOST) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            ).use { ps ->

                ps.setString(1, name)
                ps.setString(2, host)

                ps.executeUpdate()

                ps.generatedKeys.use { rs ->

                    if (rs.next()) {
                        return rs.getInt(1)
                    }
                }
            }
        else
            con.prepareStatement(
                "INSERT INTO $T_NAME ($C_ID, $C_NAME, $C_HOST) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            ).use { ps ->

                ps.setInt(1, id)
                ps.setString(2, name)
                ps.setString(3, host)

                ps.executeUpdate()

                ps.generatedKeys.use { rs ->

                    if (rs.next()) {
                        return rs.getInt(1)
                    }
                }
            }

        return null
    }

    fun deleteTenant(tenantId: Int): Boolean {
        var result = true

        try {
            Database.getConnection().use { con ->

                con.prepareStatement("DROP TABLE IF EXISTS ${RefreshTokenTable.T_NAME}?")
                    .use { ps ->
                        ps.setInt(1, tenantId)
                        ps.execute()
                    }
                con.prepareStatement("DROP TABLE IF EXISTS ${ClientTable.T_NAME}?")
                    .use { ps ->
                        ps.setInt(1, tenantId)
                        ps.execute()
                    }
                con.prepareStatement("DROP TABLE IF EXISTS ${UserTable.T_NAME}?")
                    .use { ps ->
                        ps.setInt(1, tenantId)
                        ps.execute()
                    }

                con.prepareStatement("DELETE FROM ${TenantRSAKeyPairTable.T_NAME} WHERE ${TenantRSAKeyPairTable.C_TENANT_ID} = ?")
                    .use { ps ->
                        ps.setInt(1, tenantId)
                        ps.execute()
                    }
                con.prepareStatement("DELETE FROM $T_NAME WHERE $C_ID = ?")
                    .use { ps ->
                        ps.setInt(1, tenantId)
                        ps.execute()
                    }
            }
        } catch (e: SQLException) {
            result = false
            _LOG.error("deleteTenant", e)
        }

        return result
    }

    fun getAll(): List<Tenant> {
        val result: ArrayList<Tenant> = ArrayList()

        try {
            Database.getConnection().use { con ->

                con.prepareStatement("SELECT * FROM $T_NAME").use { ps ->

                    ps.executeQuery().use { rs ->

                        while (rs.next()) {
                            val tenantId = rs.getInt(C_ID)

                            val validKeys = TenantRSAKeyPairTable.getByTenantId(con, tenantId)

                            val clients = ClientTable.getAllByTenantId(con, tenantId)

                            result.add(
                                Tenant(
                                    tenantId,
                                    rs.getString(C_NAME),
                                    rs.getString(C_HOST),
                                    rs.getString(C_ALIASES).split(Regex("\r?\n"))
                                ).apply {
                                    setValidRSAKeyPairs(validKeys)
                                    setClients(clients)
                                }
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            _LOG.error("getAll", e)
        }

        return result
    }

    fun getById(tenantId: Int): Tenant? {
        var result: Tenant? = null

        try {
            Database.getConnection().use { con ->

                con.prepareStatement("SELECT * FROM $T_NAME WHERE $C_ID = ?").use { ps ->

                    ps.setInt(1, tenantId)

                    ps.executeQuery().use { rs ->

                        if (rs.next()) {
                            val validKeys = TenantRSAKeyPairTable.getByTenantId(con, tenantId)

                            result =
                                Tenant(
                                    tenantId,
                                    rs.getString(C_NAME),
                                    rs.getString(C_HOST),
                                    rs.getString(C_ALIASES).split(Regex("\r?\n"))
                                ).apply {
                                    setValidRSAKeyPairs(validKeys)
                                }

                        }
                    }
                }
            }
        } catch (e: SQLException) {
            _LOG.error("getById", e)
        }

        return result
    }

}