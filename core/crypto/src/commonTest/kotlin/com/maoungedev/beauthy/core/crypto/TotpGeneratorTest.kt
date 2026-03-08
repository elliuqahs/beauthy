package com.maoungedev.beauthy.core.crypto

import kotlin.test.Test
import kotlin.test.assertEquals

class TotpGeneratorTest {

    private val fakeHmac = object : HmacProvider {
        override fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
            // Real HMAC-SHA1 for the well-known test secret "12345678901234567890"
            // We use a lookup table for RFC 6238 test vectors
            val counter = bytesToLong(data)
            return when (counter) {
                // T = 59s -> counter = 1 (59/30 = 1)
                1L -> hexToBytes("75a48a19d4cbe100644e8ac1397eea747a0f5bd1")
                // T = 1111111109s -> counter = 37037036
                37037036L -> hexToBytes("0decb764980a90e846ca3d33e81f3520a0376bfc")
                // T = 1111111111s -> counter = 37037037
                37037037L -> hexToBytes("66c28227d03a2d5529262ff016a1e6ef76557ece")
                // T = 2000000000s -> counter = 66666666
                66666666L -> hexToBytes("a1ace5396a06de4d4c16a8c4a1d2f26f2e4ad179")
                else -> ByteArray(20) // fallback
            }
        }
    }

    private val generator = TotpGenerator(fakeHmac)

    @Test
    fun generate_rfc6238_vector_t59() {
        // At time = 59s (counter=1), expected OTP = 287082
        val result = generator.generate(
            secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", // "12345678901234567890" in Base32
            timestampMillis = 59_000L
        )
        assertEquals("287082", result)
    }

    @Test
    fun generate_rfc6238_vector_t1111111109() {
        val result = generator.generate(
            secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
            timestampMillis = 1111111109_000L
        )
        assertEquals("081804", result)
    }

    @Test
    fun generate_rfc6238_vector_t1111111111() {
        val result = generator.generate(
            secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
            timestampMillis = 1111111111_000L
        )
        assertEquals("050471", result)
    }

    @Test
    fun generate_rfc6238_vector_t2000000000() {
        val result = generator.generate(
            secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
            timestampMillis = 2000000000_000L
        )
        assertEquals("279037", result)
    }

    @Test
    fun remainingSeconds_atPeriodBoundary() {
        // At exactly 0s (boundary)
        assertEquals(30, generator.remainingSeconds(0L))
        // At exactly 30s (next boundary)
        assertEquals(30, generator.remainingSeconds(30_000L))
    }

    @Test
    fun remainingSeconds_midPeriod() {
        // At 15s into a period
        assertEquals(15, generator.remainingSeconds(15_000L))
        // At 29s into a period
        assertEquals(1, generator.remainingSeconds(29_000L))
    }

    @Test
    fun generate_padsShorterCodes() {
        // Counter = 0 with our fake -> returns ByteArray(20) of zeros
        // Truncation of all zeros: offset=0, code=0 -> "000000"
        val result = generator.generate(
            secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
            timestampMillis = 0L
        )
        assertEquals(6, result.length)
    }

    private fun hexToBytes(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    private fun bytesToLong(bytes: ByteArray): Long {
        var result = 0L
        for (b in bytes) {
            result = (result shl 8) or (b.toLong() and 0xFF)
        }
        return result
    }
}
