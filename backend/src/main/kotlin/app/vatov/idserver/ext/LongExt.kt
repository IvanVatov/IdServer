package app.vatov.idserver.ext

import java.time.Instant

fun Long.toInstant(): Instant {
    return Instant.ofEpochSecond(this)
}