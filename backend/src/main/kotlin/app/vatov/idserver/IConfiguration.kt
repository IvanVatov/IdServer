package app.vatov.idserver

import java.util.Properties

interface IConfiguration {

    /** Web Server configuration */
    val WEB_PORT: Int?
    val WEB_SSL_PORT: Int?
    val WEB_KEYSTORE_FILE: String?
    val WEB_KEY_ALIAS: String?
    val WEB_KEYSTORE_PASSWORD: String?
    val WEB_PRIVATE_KEY_PASSWORD: String?

    /** Database  */
    val DATABASE_HOST: String
    val DATABASE_NAME: String
    val DATABASE_USER: String
    val DATABASE_PASSWORD: String
    val DATABASE_MAX_CONNECTIONS: Int

    val JWT_SIGNING_KEY_SIZE: Int

    val JWT_SIGNING_ALGORITHM: String

    /** Administration */
    val ADMINISTRATION_ACCOUNTS: Map<String, String>
    val ADMINISTRATION_HOST: String
    val ALLOWED_REMOTE_IPS: Set<String>

    val FLUTTER_ADMINISTRATION_FILES_LOCATION: String
}

internal fun Properties.getOrNull(key: String): String? {
    val value = getProperty(key)

    if (value.isEmpty()) {
        return null
    }
    return value
}