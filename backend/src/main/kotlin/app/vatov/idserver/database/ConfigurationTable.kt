package app.vatov.idserver.database

import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

object ConfigurationTable {

    private val _LOG = LoggerFactory.getLogger(ConfigurationTable::class.java)

    internal const val T_NAME = "configuration"

    internal const val C_KEY = "id"
    internal const val C_VALUE = "json"

    fun createTable(con: Connection) {
        con.prepareStatement(
            """CREATE TABLE `$T_NAME` (
                        `$C_KEY` CHAR(22) NOT NULL,
                        `$C_VALUE` VARCHAR(8192) NOT NULL,
                        PRIMARY KEY (`$C_KEY`)
                    ) COLLATE='utf8_general_ci';"""
        ).use { ps ->
            ps.execute()
        }
    }

    fun insertOrUpdate(key: Int, value: String): Boolean {
        var result: Boolean = false

        try {
            Database.getConnection().use { con ->
                result = insertOrUpdate(con, key, value)
            }
        } catch (e: SQLException) {
            _LOG.error("insertOrUpdate", e)
        }

        return result
    }

    fun insertOrUpdate(con: Connection, key: Int, value: String): Boolean {

        con.prepareStatement(
            "INSERT INTO $T_NAME ($C_KEY, $C_VALUE) VALUES (?, ?) ON DUPLICATE KEY UPDATE $C_VALUE = ?"
        ).use { ps ->
            ps.setInt(1, key)
            ps.setString(2, value)
            ps.setString(3, value)

            return ps.executeUpdate() == 1
        }
    }



    fun getByKey(key: Int): String? {
        try {
            Database.getConnection().use { con ->

                return getByKey(con, key)
            }
        } catch (e: SQLException) {
            _LOG.error("getByKey", e)
        }

        return null
    }

    fun getByKey(con: Connection, key: Int): String? {

        con.prepareStatement("SELECT $C_VALUE FROM $T_NAME WHERE $C_KEY = ?")
            .use { ps ->

                ps.setInt(1, key)

                ps.executeQuery().use { rs ->

                    if (rs.next()) {
                        return rs.getString(C_VALUE)
                    }
                }
            }

        return null
    }

    object Key {
        const val DATABASE_VERSION = 1
    }
}