package app.vatov.idserver.util

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

fun hashSHA256(text: String): String {

    val messageDigest = MessageDigest.getInstance("SHA-256")

    val byteArray = messageDigest.digest(text.toByteArray(StandardCharsets.UTF_8))

    val bigInt = BigInteger(1, byteArray)

    return bigInt.toString(16)
}