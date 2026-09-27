package io.github.elliuqahs.beauthy

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Checks the platform HMAC backend directly against RFC 2202 / RFC 4231 vectors.
 */
class HmacTest {

    private val key = "Jefe".encodeToByteArray()
    private val data = "what do ya want for nothing?".encodeToByteArray()

    @Test
    fun sha1_rfc2202() {
        assertEquals(
            "effcdf6ae5eb2fa2d27416d5f184df9c259a7c79",
            hmac(HmacAlgorithm.SHA1, key, data).toHex()
        )
    }

    @Test
    fun sha256_rfc4231() {
        assertEquals(
            "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843",
            hmac(HmacAlgorithm.SHA256, key, data).toHex()
        )
    }

    @Test
    fun sha512_rfc4231() {
        assertEquals(
            "164b7a7bfcf819e2e395fbe73b56e0a387bd64222e831fd610270cd7ea250554" +
                "9758bf75c05a994a6d034f65f8f0e6fdcaeab1a34d4a6b4b636e070a38bce737",
            hmac(HmacAlgorithm.SHA512, key, data).toHex()
        )
    }

    @Test
    fun digestLength_matchesAlgorithm() {
        HmacAlgorithm.entries.forEach {
            assertEquals(it.digestLength, hmac(it, key, data).size, it.name)
        }
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
}
