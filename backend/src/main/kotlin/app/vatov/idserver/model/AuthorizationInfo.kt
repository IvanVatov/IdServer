package app.vatov.idserver.model

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

        return "$createdAt&$tenantId&$clientId&$state&$nonce&$redirectUrl&$scope"
    }

    companion object {
        fun fromString(str: String): AuthorizationInfo {

            val split = str.split('&')

            return AuthorizationInfo(
                split[0].toLong(),
                split[1].toInt(),
                split[2],
                split[3],
                split[4],
                split[5],
                split[6]
            )
        }

    }
}