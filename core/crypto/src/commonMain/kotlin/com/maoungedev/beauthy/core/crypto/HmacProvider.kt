package com.maoungedev.beauthy.core.crypto

interface HmacProvider {
    fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray
}
