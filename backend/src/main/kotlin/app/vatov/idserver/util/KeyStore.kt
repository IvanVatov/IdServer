package app.vatov.idserver.util

import java.io.File
import java.security.KeyStore


fun loadKeyStore(keystore: File, password: String): KeyStore {
    return KeyStore.getInstance(keystore, password.toCharArray())
}