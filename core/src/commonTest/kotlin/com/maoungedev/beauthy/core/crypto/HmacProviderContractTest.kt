@file:Suppress("DEPRECATION")

package com.maoungedev.beauthy.core.crypto

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Runs RFC test vectors against a real [HmacProvider] implementation.
 *
 * Each platform subclasses this with its own provider, so the same vectors
 * verify both the HMAC backend and the full [TotpGenerator] pipeline
 * (Base32 decoding, counter encoding, truncation) end to end.
 */
abstract class HmacProviderContractTest {

    abstract val provider: HmacProvider

    private val generator by lazy { TotpGenerator(provider) }

    // --- Raw HMAC (RFC 2202 §3 / RFC 4231 §4.3, key "Jefe") ---

    @Test
    fun hmacSha1_rfc2202_jefe() {
        assertEquals(
            "effcdf6ae5eb2fa2d27416d5f184df9c259a7c79",
            provider.hmac(HmacAlgorithm.SHA1, JEFE_KEY, JEFE_DATA).toHex()
        )
    }

    @Test
    fun hmacSha256_rfc4231_jefe() {
        assertEquals(
            "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843",
            provider.hmac(HmacAlgorithm.SHA256, JEFE_KEY, JEFE_DATA).toHex()
        )
    }

    @Test
    fun hmacSha512_rfc4231_jefe() {
        assertEquals(
            "164b7a7bfcf819e2e395fbe73b56e0a387bd64222e831fd610270cd7ea250554" +
                "9758bf75c05a994a6d034f65f8f0e6fdcaeab1a34d4a6b4b636e070a38bce737",
            provider.hmac(HmacAlgorithm.SHA512, JEFE_KEY, JEFE_DATA).toHex()
        )
    }

    // --- HOTP (RFC 4226 Appendix D) ---

    @Test
    fun hotp_rfc4226_counters0to9() {
        val expected = listOf(
            "755224", "287082", "359152", "969429", "338314",
            "254676", "287922", "162583", "399871", "520489"
        )
        expected.forEachIndexed { counter, code ->
            assertEquals(code, generator.generateHotp(SECRET_SHA1, counter.toLong()), "counter=$counter")
        }
    }

    // --- TOTP (RFC 6238 Appendix B, 8 digits) ---

    @Test
    fun totpSha1_rfc6238() {
        assertTotpVectors(
            SECRET_SHA1, HmacAlgorithm.SHA1,
            "94287082", "07081804", "14050471", "89005924", "69279037", "65353130"
        )
    }

    @Test
    fun totpSha256_rfc6238() {
        assertTotpVectors(
            SECRET_SHA256, HmacAlgorithm.SHA256,
            "46119246", "68084774", "67062674", "91819424", "90698825", "77737706"
        )
    }

    @Test
    fun totpSha512_rfc6238() {
        assertTotpVectors(
            SECRET_SHA512, HmacAlgorithm.SHA512,
            "90693936", "25091201", "99943326", "93441116", "38618901", "47863826"
        )
    }

    // --- Helpers ---

    private fun assertTotpVectors(secret: String, algorithm: HmacAlgorithm, vararg expected: String) {
        RFC6238_TIMES.zip(expected.toList()).forEach { (seconds, code) ->
            val actual = generator.generate(
                secret = secret,
                timestampMillis = seconds * 1000L,
                digits = 8,
                algorithm = algorithm
            )
            assertEquals(code, actual, "$algorithm at t=$seconds")
        }
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

    companion object {
        private val JEFE_KEY = "Jefe".encodeToByteArray()
        private val JEFE_DATA = "what do ya want for nothing?".encodeToByteArray()

        private val RFC6238_TIMES = listOf(59L, 1111111109L, 1111111111L, 1234567890L, 2000000000L, 20000000000L)

        // "12345678901234567890" in Base32
        private const val SECRET_SHA1 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
        // "12345678901234567890123456789012" in Base32
        private const val SECRET_SHA256 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZA"
        // "1234567890123456789012345678901234567890123456789012345678901234" in Base32
        private const val SECRET_SHA512 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNA"
    }
}
