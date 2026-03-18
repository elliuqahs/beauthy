package com.maoungedev.beauthy.core.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

public class JvmHmacProvider : HmacProvider {

    override fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray {
        val algoName = when (algorithm) {
            HmacAlgorithm.SHA1 -> "HmacSHA1"
            HmacAlgorithm.SHA256 -> "HmacSHA256"
            HmacAlgorithm.SHA512 -> "HmacSHA512"
        }
        val mac = Mac.getInstance(algoName)
        mac.init(SecretKeySpec(key, algoName))
        return mac.doFinal(data)
    }
}
