package io.github.elliuqahs.beauthy

import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val secureRandom = SecureRandom()

internal actual fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray {
    val algoName = when (algorithm) {
        HmacAlgorithm.SHA1 -> "HmacSHA1"
        HmacAlgorithm.SHA256 -> "HmacSHA256"
        HmacAlgorithm.SHA512 -> "HmacSHA512"
    }
    val mac = Mac.getInstance(algoName)
    mac.init(SecretKeySpec(key, algoName))
    return mac.doFinal(data)
}

internal actual fun secureRandomBytes(size: Int): ByteArray =
    ByteArray(size).also { secureRandom.nextBytes(it) }

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()
