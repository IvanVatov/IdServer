package app.vatov.idserver.util

import kotlin.test.Test
import kotlin.test.assertEquals

internal class HashingTest {

    @Test
    fun hash() {
        val expected = "741f67765bef6f01f37bf5cb1724509a83409324efa6ad2586d27f4e3edea296"

        assertEquals(expected, hashSHA256("hashedpassword"))
    }
}