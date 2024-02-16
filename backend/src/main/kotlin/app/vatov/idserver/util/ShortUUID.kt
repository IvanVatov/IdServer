package app.vatov.idserver.util

import java.math.BigInteger
import java.util.UUID

fun createShortUUID(): String {
    return encode(UUID.randomUUID())
}

private fun encode(uuid: UUID): String {
    val pair: BigInteger = uuid.toBigInteger()
    return encodeBase62(pair)
}

private fun UUID.toBigInteger(): BigInteger {
    return pair(
        BigInteger.valueOf(this.mostSignificantBits),
        BigInteger.valueOf(this.leastSignificantBits)
    )
}

private val HALF = BigInteger.ONE.shiftLeft(64) // 2^64

private fun pair(hi: BigInteger, lo: BigInteger): BigInteger {
    val unsignedLo: BigInteger = lo.toUnsigned()
    val unsignedHi: BigInteger = hi.toUnsigned()
    return unsignedLo.add(unsignedHi.multiply(HALF))
}

private fun BigInteger.toUnsigned(): BigInteger {
    return if (this.signum() < 0)
        this.add(HALF)
    else this
}
