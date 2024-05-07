package app.vatov.idserver.util

import java.math.BigInteger

fun encodeBase62(num: Int): String {
    var n = num
    var result = ""
    while (n > 0) {
        result = ALPHA_NUMERIC_CHARS[n % 62] + result
        n /= 62
    }
    return result
}

fun decodeBase62(str: String): Int {
    var result = 0
    for (c in str) {
        result = result * 62 + ALPHA_NUMERIC_CHARS.indexOf(c)
    }
    return result
}

fun encodeBase62(number: BigInteger): String {
    var mNumber = number
    if (mNumber < BigInteger.ZERO) {
        throw IllegalArgumentException("number must not be negative")
    }
    val result = StringBuilder()
    while (mNumber > BigInteger.ZERO) {
        val divMod = mNumber.divideAndRemainder(BigInteger.valueOf(62))
        mNumber = divMod[0]
        val digit = divMod[1].toInt()
        result.insert(0, ALPHA_NUMERIC_CHARS[digit])
    }
    return if (result.isEmpty()) ALPHA_NUMERIC_CHARS.substring(0, 1) else result.toString()
}