package com.maoungedev.beauthy.core.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class JvmHmacProvider : HmacProvider {
    override fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        return mac.doFinal(data)
    }
}
