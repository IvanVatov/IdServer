package app.vatov.idserver.client

import app.vatov.idserver.jsonInstance
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*


object HttpClient {

    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(jsonInstance)
        }
    }
}
