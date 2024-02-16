package app.vatov.idserver

import app.vatov.idserver.database.Database
import app.vatov.idserver.util.loadKeyStore
import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
internal val jsonInstance = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}


fun main(args: Array<String>): Unit {

    val scope = CoroutineScope(Dispatchers.IO)

    scope.launch {
        while (true) {
            // TODO: cleanup task
            delay(1000 * 60)
        }
    }

    // Load configuration
    Configuration

    // Init database
    Database

    // Init IDServer
    IDServer

    // Start web server
    startWebServer()

    scope.cancel()
}

private var applicationEngine: ApplicationEngine? = null

private fun startWebServer() {

    val environment = applicationEngineEnvironment {

        log = LoggerFactory.getLogger("ktor.application")

        if (Configuration.WEB_PORT != null) {
            connector {
                port = Configuration.WEB_PORT
            }
        }

        // SSL Configuration
        if (Configuration.WEB_SSL_PORT != null &&
            Configuration.WEB_KEYSTORE_FILE != null &&
            Configuration.WEB_KEYSTORE_PASSWORD != null &&
            Configuration.WEB_KEY_ALIAS != null &&
            Configuration.WEB_PRIVATE_KEY_PASSWORD != null
        ) {
            val keyStoreFile = File(Configuration.WEB_KEYSTORE_FILE)

            sslConnector(
                keyStore = loadKeyStore(
                    keyStoreFile,
                    Configuration.WEB_KEYSTORE_PASSWORD
                ),
                keyAlias = Configuration.WEB_KEY_ALIAS,
                keyStorePassword = { Configuration.WEB_KEYSTORE_PASSWORD.toCharArray() },
                privateKeyPassword = { Configuration.WEB_PRIVATE_KEY_PASSWORD.toCharArray() }) {
                port = Configuration.WEB_SSL_PORT
                keyStorePath = keyStoreFile
            }
        }

        module(Application::webServerModule)
    }

    applicationEngine = embeddedServer(Netty, environment)

    applicationEngine?.start(wait = true)
}

fun stopWebServer() {
    applicationEngine?.stop(gracePeriodMillis = 15000, timeoutMillis = 15000)
}