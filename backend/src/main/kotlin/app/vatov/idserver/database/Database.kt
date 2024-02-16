package app.vatov.idserver.database

import app.vatov.idserver.Configuration
import app.vatov.idserver.Const
import app.vatov.idserver.model.ClientSettings
import app.vatov.idserver.model.GrantType
import app.vatov.idserver.util.createShortUUID
import app.vatov.idserver.util.hashSHA256
import org.mariadb.jdbc.MariaDbPoolDataSource
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.time.Instant

object Database {

    private val _LOG = LoggerFactory.getLogger(Database::class.java)

    private val DATABASE_POOL =
        MariaDbPoolDataSource(
            "jdbc:mariadb://${Configuration.DATABASE_HOST}/${Configuration.DATABASE_NAME}" +
                    "?useUnicode=true&characterEncoding=utf-8&useSSL=false" +
                    "&user=${Configuration.DATABASE_USER}" +
                    "&password=${Configuration.DATABASE_PASSWORD}" +
                    "&maxPoolSize=${Configuration.DATABASE_MAX_CONNECTIONS}"
        )

    // Increase it in order to trigger migration method
    private const val DATABASE_VERSION = 1

    init {
        _LOG.info("Checking Database connection")

        try {
            DATABASE_POOL.connection.use { con ->
                try {
                    val dbVersion: Int =
                        ConfigurationTable.getByKey(con, ConfigurationTable.Key.DATABASE_VERSION)
                            ?.toInt()
                            ?: throw Exception("Database version is not stored in the ${ConfigurationTable.T_NAME} table, maybe corrupted!")

                    if (dbVersion < DATABASE_VERSION) {
                        _LOG.info("Database migration started...")
                        databaseMigration(con, dbVersion, DATABASE_VERSION)

                        _LOG.info("Database migration Finished!")
                    }
                } catch (e: SQLException) {
                    if (e.sqlState != "42S02") // invalid object name
                        throw e
                    // Create Tables
                    _LOG.info("Seems database is not created. Trying to create...")

                    ConfigurationTable.createTable(con)
                    ConfigurationTable.insertOrUpdate(
                        ConfigurationTable.Key.DATABASE_VERSION,
                        DATABASE_VERSION.toString()
                    )

                    TenantTable.createTable(con)
                    TenantRSAKeyPairTable.createTable(con)

                    createAdministrationTenant(con)

                    _LOG.info("Database created successfully!")
                }
            }

            _LOG.info("Database connection OK.")
        } catch (e: Exception) {
            _LOG.error("Database: Could not get a connection. $e")
            throw e
        }
    }

    private fun databaseMigration(con: Connection, oldVersion: Int, newVersion: Int) {
        // implement database migrations here

    }

    fun getConnection(): Connection {
        var con: Connection? = null
        while (con == null) {
            try {
                con = DATABASE_POOL.connection
            } catch (e: Exception) {
                _LOG.error("Database: Could not get a connection. $e")
            }
        }
        return con
    }

    fun close() {
        try {
            DATABASE_POOL.close()
        } catch (e: Exception) {
            _LOG.error("Database: Could not close the database. $e")
        }
    }

    private fun createAdministrationTenant(connection: Connection) {

        TenantTable.createTenantTransaction(
            connection,
            "Admin",
            "!@#$% admin %$#@!",
            Const.Administration.TENANT_ID
        )

        ClientTable.create(
            connection,
            Const.Administration.TENANT_ID,
            Const.Administration.CLIENT_ID,
            "Administration",
            Const.Administration.CLIENT_SECRET,
            ClientSettings(
                grantTypes = listOf(
                    GrantType.PASSWORD,
                    GrantType.REFRESH_TOKEN
                ),
                scope = listOf(
                    Const.Administration.TENANT_ADMIN_SCOPE,
                    Const.OpenIdScope.OFFLINE_ACCESS
                )
            )
        )

        Configuration.ADMINISTRATION_ACCOUNTS.forEach {

            UserTable.create(
                connection,
                Const.Administration.TENANT_ID,
                createShortUUID(),
                it.key,
                hashSHA256(it.value),
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                listOf(Const.Administration.SUPER_ADMIN_ROLE)
            )
        }
    }
}