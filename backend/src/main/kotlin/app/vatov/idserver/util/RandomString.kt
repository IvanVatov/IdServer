package app.vatov.idserver.util

import kotlin.random.Random

fun generateRandomString(length: Int = 64): String {
    val charsLength = ALPHA_NUMERIC_CHARS.length
    return (0..length)
        .map { ALPHA_NUMERIC_CHARS[Random.nextInt(0, charsLength)] }
        .joinToString("")
}