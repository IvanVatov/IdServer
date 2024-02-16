package app.vatov.idserver.util

import org.junit.jupiter.api.Test
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import kotlin.test.assertEquals

internal class EncryptionTest {

    @Test
    fun test() {
        val secretKey =
            SecretKeyFactory.getInstance("DES").generateSecret(DESKeySpec(randomBytes()))

        val expected = "1&asd&asd&https://test.gg/asd&read write&12312456"

        val encrypted = URLEncoder.encode(expected.encrypt(secretKey), StandardCharsets.UTF_8)

        val decrypted = URLDecoder.decode(encrypted, StandardCharsets.UTF_8).decrypt(secretKey)

        assertEquals(expected, decrypted)
    }
}