package app.vatov.idserver.util

import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.SecretKey


private val cip = Cipher.getInstance("DES")

fun String.encrypt(key: SecretKey): String {
    val byteArray: ByteArray = this.toByteArray(StandardCharsets.UTF_8)
    cip.init(Cipher.ENCRYPT_MODE, key)
    val encryptedBytes = cip.doFinal(byteArray)
    return Base64.getEncoder().encodeToString(encryptedBytes)
}

fun String.decrypt(key: SecretKey): String {
    val byteArray = Base64.getDecoder().decode(this)
    cip.init(Cipher.DECRYPT_MODE, key)
    val plainTextBytes = cip.doFinal(byteArray)
    return plainTextBytes.toString(StandardCharsets.UTF_8)
}

fun randomBytes(): ByteArray {
    val rd = Random()
    val arr = ByteArray(24)
    rd.nextBytes(arr)
    return arr
}