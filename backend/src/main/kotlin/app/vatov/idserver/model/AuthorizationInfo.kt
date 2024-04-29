package app.vatov.idserver.model

import java.util.concurrent.ThreadLocalRandom

class AuthorizationInfo(
    val createdAt: Long = System.currentTimeMillis(),
    val tenantId: Int,
    val clientId: String,
    val state: String,
    val nonce: String,
    val redirectUrl: String,
    val scope: String
) {
    override fun toString(): String {

        return "${ThreadLocalRandom.current().nextInt()}&$createdAt&$tenantId&$clientId&$state&$nonce&$redirectUrl&$scope"
    }

    companion object {


        fun fromString(str: String): AuthorizationInfo {

            val split = str.split('&')

            return AuthorizationInfo(
                split[1].toLong(),
                split[2].toInt(),
                split[3],
                split[4],
                split[5],
                split[6],
                split[7]
            )
        }

    }
}