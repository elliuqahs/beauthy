package com.maoungedev.beauthy.core.crypto

import kotlin.test.Test
import kotlin.test.assertEquals

class TotpGeneratorTest {

    // Real HMAC values for RFC 6238 test vectors, keyed by (algorithm, counter).
    // SHA-1   secret: "12345678901234567890" (20 bytes)
    // SHA-256 secret: "12345678901234567890123456789012" (32 bytes)
    // SHA-512 secret: "1234567890123456789012345678901234567890123456789012345678901234" (64 bytes)
    private val fakeHmac = object : HmacProvider {
        override fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray {
            val counter = bytesToLong(data)
            return when (algorithm) {
                HmacAlgorithm.SHA1 -> sha1Vectors(counter)
                HmacAlgorithm.SHA256 -> sha256Vectors(counter)
                HmacAlgorithm.SHA512 -> sha512Vectors(counter)
            }
        }

        private fun sha1Vectors(counter: Long) = when (counter) {
            1L -> hexToBytes("75a48a19d4cbe100644e8ac1397eea747a2d33ab")
            37037036L -> hexToBytes("278c02e53610f84c40bd9135acd4101012410a14")
            37037037L -> hexToBytes("b0092b21d048af209da0a1ddd498ade8a79487ed")
            66666666L -> hexToBytes("25a326d31fc366244cad054976020c7b56b13d5f")
            else -> ByteArray(20)
        }

        private fun sha256Vectors(counter: Long) = when (counter) {
            1L -> hexToBytes("392514c9dd4165d4709456062c78e04e16e68718515951333bdb8b26caa3053c")
            37037036L -> hexToBytes("4eed729864525d771326c6049bc885629fb8813ebb417e5704df02358793f056")
            37037037L -> hexToBytes("cb48f7ef5cd98f6d7bfcb31ae7458ff692a015776205de7e1abfff29d6d48a9d")
            66666666L -> hexToBytes("a4e8eabbe549adfa65408945a9282cb93f394f06c0d4f122260963641bc3abe2")
            else -> ByteArray(32)
        }

        private fun sha512Vectors(counter: Long) = when (counter) {
            1L -> hexToBytes("6f76f324230cefda1d3f65309a0badb36efce9528ada64967d71e4e9d74c4aa37fe7650f931ab86ddccc2d38962d720ee626a20feb311b485a92e3bb0796df28")
            37037036L -> hexToBytes("b3381250260d6a9e811ae58dfa406705e38c804c97528d5a7ed8ee533331f8c43cc3454911ad1d2761f9380170c0b180a657e3a944c796e05d09f2d1630b7505")
            37037037L -> hexToBytes("01713ed59e49948a4f0fffb7466baebac66362d90764a5a23df761636e1535c44b635339ec00a789b8ca45cd3d727acd6b995047547f6f68adc6f16a7436c331")
            66666666L -> hexToBytes("129baa738cfa1565a24297237bce282671ff6e261754eb7011e1e75bd2555b326313142a1f9fe2f31d9ce6cc95d3b16a0dee56f2492f2f76885702d98bfadc93")
            else -> ByteArray(64)
        }
    }

    private val generator = TotpGenerator(fakeHmac)

    // --- SHA-1 (RFC 6238 §B.1) ---

    @Test
    fun sha1_rfc6238_t59() {
        val result = generator.generate(
            secret = SECRET_SHA1,
            timestampMillis = 59_000L
        )
        assertEquals("287082", result)
    }

    @Test
    fun sha1_rfc6238_t1111111109() {
        val result = generator.generate(
            secret = SECRET_SHA1,
            timestampMillis = 1111111109_000L
        )
        assertEquals("081804", result)
    }

    @Test
    fun sha1_rfc6238_t1111111111() {
        val result = generator.generate(
            secret = SECRET_SHA1,
            timestampMillis = 1111111111_000L
        )
        assertEquals("050471", result)
    }

    @Test
    fun sha1_rfc6238_t2000000000() {
        val result = generator.generate(
            secret = SECRET_SHA1,
            timestampMillis = 2000000000_000L
        )
        assertEquals("279037", result)
    }

    // --- SHA-256 (RFC 6238 §B.2) ---

    @Test
    fun sha256_rfc6238_t59() {
        val result = generator.generate(
            secret = SECRET_SHA256,
            timestampMillis = 59_000L,
            algorithm = HmacAlgorithm.SHA256
        )
        assertEquals("119246", result)
    }

    @Test
    fun sha256_rfc6238_t1111111109() {
        val result = generator.generate(
            secret = SECRET_SHA256,
            timestampMillis = 1111111109_000L,
            algorithm = HmacAlgorithm.SHA256
        )
        assertEquals("084774", result)
    }

    @Test
    fun sha256_rfc6238_t1111111111() {
        val result = generator.generate(
            secret = SECRET_SHA256,
            timestampMillis = 1111111111_000L,
            algorithm = HmacAlgorithm.SHA256
        )
        assertEquals("062674", result)
    }

    @Test
    fun sha256_rfc6238_t2000000000() {
        val result = generator.generate(
            secret = SECRET_SHA256,
            timestampMillis = 2000000000_000L,
            algorithm = HmacAlgorithm.SHA256
        )
        assertEquals("698825", result)
    }

    // --- SHA-512 (RFC 6238 §B.3) ---

    @Test
    fun sha512_rfc6238_t59() {
        val result = generator.generate(
            secret = SECRET_SHA512,
            timestampMillis = 59_000L,
            algorithm = HmacAlgorithm.SHA512
        )
        assertEquals("693936", result)
    }

    @Test
    fun sha512_rfc6238_t1111111109() {
        val result = generator.generate(
            secret = SECRET_SHA512,
            timestampMillis = 1111111109_000L,
            algorithm = HmacAlgorithm.SHA512
        )
        assertEquals("091201", result)
    }

    @Test
    fun sha512_rfc6238_t1111111111() {
        val result = generator.generate(
            secret = SECRET_SHA512,
            timestampMillis = 1111111111_000L,
            algorithm = HmacAlgorithm.SHA512
        )
        assertEquals("943326", result)
    }

    @Test
    fun sha512_rfc6238_t2000000000() {
        val result = generator.generate(
            secret = SECRET_SHA512,
            timestampMillis = 2000000000_000L,
            algorithm = HmacAlgorithm.SHA512
        )
        assertEquals("618901", result)
    }

    // --- Remaining seconds ---

    @Test
    fun remainingSeconds_atPeriodBoundary() {
        assertEquals(30, generator.remainingSeconds(0L))
        assertEquals(30, generator.remainingSeconds(30_000L))
    }

    @Test
    fun remainingSeconds_midPeriod() {
        assertEquals(15, generator.remainingSeconds(15_000L))
        assertEquals(1, generator.remainingSeconds(29_000L))
    }

    // --- Edge cases ---

    @Test
    fun generate_padsShorterCodes() {
        val result = generator.generate(
            secret = SECRET_SHA1,
            timestampMillis = 0L
        )
        assertEquals(6, result.length)
    }

    // --- Helpers ---

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

    companion object {
        // "12345678901234567890" in Base32
        private const val SECRET_SHA1 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
        // "12345678901234567890123456789012" in Base32
        private const val SECRET_SHA256 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZA"
        // "1234567890123456789012345678901234567890123456789012345678901234" in Base32
        private const val SECRET_SHA512 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNA"
    }
}
