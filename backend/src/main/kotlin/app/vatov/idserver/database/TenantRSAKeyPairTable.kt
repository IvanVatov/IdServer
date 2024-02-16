package app.vatov.idserver.database


import app.vatov.idserver.ext.toInstant
import app.vatov.idserver.model.TenantRSAKeyPair
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

object TenantRSAKeyPairTable {

    private val _LOG = LoggerFactory.getLogger(TenantRSAKeyPairTable::class.java)

    internal const val T_NAME = "tenant_rsa_key_pair"


    internal const val C_ID = "id"
    internal const val C_TENANT_ID = "tenant_id"
    internal const val C_PUBLIC_KEY = "public_key"
    internal const val C_PRIVATE_KEY = "private_key"
    internal const val C_CREATED_AT = "created_at"


    fun createTable(con: Connection) {
        con.prepareStatement(
            """CREATE TABLE `$T_NAME` (
                        `$C_ID` CHAR(22) NOT NULL,
                        `$C_TENANT_ID` INT NOT NULL,
                        `$C_PUBLIC_KEY` VARCHAR(8192) NOT NULL,
                        `$C_PRIVATE_KEY` VARCHAR(8192) NOT NULL,
                        `$C_CREATED_AT` BIGINT NOT NULL,
                        PRIMARY KEY (`$C_ID`)
                    ) COLLATE='utf8_general_ci';"""
        ).use { ps ->
            ps.execute()
        }
    }

    fun insert(
        tenantRSAKeyPair: TenantRSAKeyPair
    ): Boolean {
        var result: Boolean = false

        try {
            Database.getConnection().use { con ->

                result = insert(con, tenantRSAKeyPair)
            }
        } catch (e: SQLException) {
            _LOG.error("insert", e)
        }

        return result
    }

    fun insert(con: Connection, tenantRSAKeyPair: TenantRSAKeyPair): Boolean {
        con.prepareStatement(
            "INSERT INTO $T_NAME ($C_ID, $C_TENANT_ID, $C_PUBLIC_KEY, $C_PRIVATE_KEY, $C_CREATED_AT) VALUES (?, ?, ?, ?, ?)"
        ).use { ps ->

            ps.setString(1, tenantRSAKeyPair.id)
            ps.setInt(2, tenantRSAKeyPair.tenantId)
            ps.setString(3, tenantRSAKeyPair.publicKey)
            ps.setString(4, tenantRSAKeyPair.privateKey)
            ps.setLong(5, tenantRSAKeyPair.createdAt.epochSecond)

            return ps.executeUpdate() == 1
        }
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

    fun delete(con: Connection, tenantId: Int, keyId: String): Boolean {
        con.prepareStatement(
            "DELETE FROM $T_NAME WHERE $C_TENANT_ID = ? AND $C_ID = ?"
        ).use { ps ->
            ps.setInt(1, tenantId)
            ps.setString(2, keyId)

            return ps.executeUpdate() == 1
        }
    }


    fun getByTenantId(tenantId: Int): List<TenantRSAKeyPair> {

        val result = ArrayList<TenantRSAKeyPair>()

        try {
            Database.getConnection().use { con ->

                result.addAll(getByTenantId(con, tenantId))
            }
        } catch (e: SQLException) {
            _LOG.error("getByTenantId", e)
        }

        return result
    }

    fun getByTenantId(con: Connection, tenantId: Int): List<TenantRSAKeyPair> {

        val result = ArrayList<TenantRSAKeyPair>()

        con.prepareStatement("SELECT * FROM $T_NAME WHERE $C_TENANT_ID = ? ORDER BY $C_CREATED_AT")
            .use { ps ->

                ps.setInt(1, tenantId)

                ps.executeQuery().use { rs ->

                    while (rs.next()) {
                        result.add(
                            TenantRSAKeyPair(
                                rs.getString(C_ID),
                                rs.getInt(C_TENANT_ID),
                                rs.getString(C_PUBLIC_KEY),
                                rs.getString(C_PRIVATE_KEY),
                                rs.getLong(C_CREATED_AT).toInstant()
                            )
                        )
                    }
                }
            }

        return result
    }
}