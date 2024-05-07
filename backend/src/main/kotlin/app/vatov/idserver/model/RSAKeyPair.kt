package app.vatov.idserver.model


import java.security.Key
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*


class RSAKeyPair(val publicKey: RSAPublicKey, val privateKey: RSAPrivateKey) {

    companion object {
        fun generate(keySize: Int = 2048): RSAKeyPair {
            val kpg = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(keySize)

            val keyPair = kpg.generateKeyPair()

            return RSAKeyPair(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey)
        }
    }
}

fun Key.encodeToString(): String {
    val encoder = Base64.getEncoder()
    return encoder.encodeToString(this.encoded)
}


fun RSAPrivateKey.toPresentationString(): String {
    val stringBuilder = StringBuilder()

    stringBuilder.append("-----BEGIN RSA PRIVATE KEY-----")
    stringBuilder.append(System.lineSeparator())
    stringBuilder.append(this.encodeToString())
    stringBuilder.append(System.lineSeparator())
    stringBuilder.append("-----END RSA PRIVATE KEY-----")

    return stringBuilder.toString()
}

fun String.decodeToRSAPrivateKey(): RSAPrivateKey {
    val ks = PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.trimKeyString()))
    val kf = KeyFactory.getInstance("RSA")
    return kf.generatePrivate(ks) as RSAPrivateKey
}

fun RSAPublicKey.toPresentationString(): String {
    val stringBuilder = StringBuilder()

    stringBuilder.append("-----BEGIN PUBLIC KEY-----")
    stringBuilder.append(System.lineSeparator())
    stringBuilder.append(this.encodeToString())
    stringBuilder.append(System.lineSeparator())
    stringBuilder.append("-----END PUBLIC KEY-----")

    return stringBuilder.toString()
}

fun String.decodeToRSAPublicKey(): RSAPublicKey {
    val ks = X509EncodedKeySpec(Base64.getDecoder().decode(this.trimKeyString()))
    val kf = KeyFactory.getInstance("RSA")
    return kf.generatePublic(ks) as RSAPublicKey
}

private fun String.trimKeyString(): String {
    val sb = StringBuilder()
    val rows = this.split(Regex("\r?\n"))

    rows.forEach {
        if (!it.contains("---")) {
            sb.append(it.replace("\\s", ""))
        }
    }

    return sb.toString()
}