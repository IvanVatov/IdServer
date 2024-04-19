package app.vatov.idserver.util

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

private val messageDigestSHA256 = MessageDigest.getInstance("SHA-256")

fun hashSHA256(text: String): String {

    val byteArray = messageDigestSHA256.digest(text.toByteArray(StandardCharsets.UTF_8))

    val bigInt = BigInteger(1, byteArray)

    return bigInt.toString(16)
}