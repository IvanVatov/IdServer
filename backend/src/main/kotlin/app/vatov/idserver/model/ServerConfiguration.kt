package app.vatov.idserver.model

import app.vatov.idserver.Configuration
import kotlinx.serialization.Serializable

@Serializable
data class ServerConfiguration(
    val jwtSigningKeySize: Int = Configuration.JWT_SIGNING_KEY_SIZE,
    val jwtSigningAlgorithm: String = Configuration.JWT_SIGNING_ALGORITHM
)
