package app.vatov.idserver

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.util.Properties


object Configuration : IConfiguration{

    private val _LOG = LoggerFactory.getLogger(Configuration::class.java)

    /** Web Server configuration */
    override val WEB_PORT: Int?
    override val WEB_SSL_PORT: Int?
    override val WEB_KEYSTORE_FILE: String?
    override val WEB_KEY_ALIAS: String?
    override val WEB_KEYSTORE_PASSWORD: String?
    override val WEB_PRIVATE_KEY_PASSWORD: String?

    /** Database  */
    override val DATABASE_HOST: String
    override val DATABASE_NAME: String
    override val DATABASE_USER: String
    override val DATABASE_PASSWORD: String
    override val DATABASE_MAX_CONNECTIONS: Int

    override val JWT_SIGNING_KEY_SIZE: Int
    override val JWT_SIGNING_ALGORITHM: String

    override val ADMINISTRATION_ACCOUNTS: Map<String, String>

    override val ADMINISTRATION_HOST: String

    override val ALLOWED_REMOTE_IPS: Set<String>

    override val FLUTTER_ADMINISTRATION_FILES_LOCATION: String = "./admin"

    init {

        _LOG.info("Loading configuration")

        val prop = Properties()
        val input = FileInputStream(File("./config/Settings.conf"))
        prop.load(input)

        WEB_PORT = prop.getOrNull("Port")?.toInt()
        WEB_SSL_PORT = prop.getOrNull("SSLPort")?.toInt()
        WEB_KEYSTORE_FILE = "config/${prop.getOrNull("KeyStoreFile")}"
        WEB_KEY_ALIAS = prop.getOrNull("KeyAlias")
        WEB_KEYSTORE_PASSWORD = prop.getOrNull("KeyStorePassword")
        WEB_PRIVATE_KEY_PASSWORD = prop.getOrNull("PrivateKeyPassword")


        DATABASE_HOST = prop.getProperty("DatabaseHost")
        DATABASE_NAME = prop.getProperty("DatabaseName")
        DATABASE_USER = prop.getProperty("DatabaseUser")
        DATABASE_PASSWORD = prop.getProperty("DatabasePassword")
        DATABASE_MAX_CONNECTIONS = prop.getProperty("DatabaseMaxConnections").toInt()


        JWT_SIGNING_KEY_SIZE = prop.getProperty("JWTSigningKeySize").toInt()
        JWT_SIGNING_ALGORITHM = prop.getProperty("JWTSigningAlgorithm")

        ADMINISTRATION_ACCOUNTS = HashMap<String, String>().apply {
            prop.getProperty("AdministrationAccounts").split(" ").forEach { account ->
                val split = account.split(":")
                if (split.size != 2) throw Exception("AdministrationAccounts configuration error $account")
                put(split.first(), split.last())
            }
        }

        ADMINISTRATION_HOST = prop.getProperty("AdministrationHost")

        ALLOWED_REMOTE_IPS = HashSet<String>().apply {
            prop.getProperty("AllowedRemoteIPs").split(" ").forEach { ip ->
                add(ip)
            }
        }


        input.close()

        _LOG.info("Configuration loaded successfully")
    }
}