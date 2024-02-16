package app.vatov.idserver.model

import app.vatov.idserver.Configuration
import kotlinx.serialization.Serializable

@Serializable
data class JsonWebKey(
    val kty: String = "RSA",
    val use: String = "sig",
    val alg: String,
    val kid: String,
    val n: String,
    val e: String,
)
