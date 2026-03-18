package com.maoungedev.beauthy.core.crypto

/**
 * Generates TOTP ([RFC 6238](https://tools.ietf.org/html/rfc6238)) and
 * HOTP ([RFC 4226](https://tools.ietf.org/html/rfc4226)) one-time passwords.
 *
 * All cryptographic operations delegate to the injected [HmacProvider], so the
 * generator itself is platform-independent and fully testable with fakes.
 *
 * Sensitive byte arrays (decoded key, counter bytes, HMAC output) are zeroed
 * in `finally` blocks after each code generation.
 *
 * @param hmacProvider platform-specific HMAC implementation
 */
public class TotpGenerator(
    private val hmacProvider: HmacProvider
) {

    /**
     * Generates a TOTP code for the given timestamp.
     *
     * @param secret Base32-encoded shared secret
     * @param timestampMillis current time in milliseconds since Unix epoch
     * @param digits number of OTP digits (default 6)
     * @param period time step in seconds (default 30)
     * @param algorithm HMAC algorithm to use (default [HmacAlgorithm.SHA1])
     * @return zero-padded OTP string of length [digits]
     */
    public fun generate(
        secret: String,
        timestampMillis: Long,
        digits: Int = 6,
        period: Int = 30,
        algorithm: HmacAlgorithm = HmacAlgorithm.SHA1
    ): String {
        val counter = timestampMillis / 1000L / period.toLong()
        return generateByCounter(secret, counter, digits, algorithm)
    }

    /**
     * Generates an HOTP code for the given counter value.
     *
     * @param secret Base32-encoded shared secret
     * @param counter monotonically increasing counter
     * @param digits number of OTP digits (default 6)
     * @param algorithm HMAC algorithm to use (default [HmacAlgorithm.SHA1])
     * @return zero-padded OTP string of length [digits]
     */
    public fun generateHotp(
        secret: String,
        counter: Long,
        digits: Int = 6,
        algorithm: HmacAlgorithm = HmacAlgorithm.SHA1
    ): String {
        return generateByCounter(secret, counter, digits, algorithm)
    }

    /**
     * Returns the number of seconds remaining in the current TOTP period.
     *
     * @param timestampMillis current time in milliseconds since Unix epoch
     * @param period time step in seconds (default 30)
     * @return seconds remaining (1..[period])
     */
    public fun remainingSeconds(timestampMillis: Long, period: Int = 30): Int {
        val seconds = timestampMillis / 1000L
        return (period - (seconds % period)).toInt()
    }

    private fun generateByCounter(
        secret: String,
        counter: Long,
        digits: Int,
        algorithm: HmacAlgorithm
    ): String {
        val key = Base32.decode(secret)
        try {
            val counterBytes = counterToBytes(counter)
            try {
                val hmac = hmacProvider.hmac(algorithm, key, counterBytes)
                try {
                    return truncate(hmac, digits)
                } finally {
                    hmac.fill(0)
                }
            } finally {
                counterBytes.fill(0)
            }
        } finally {
            key.fill(0)
        }
    }

    private fun counterToBytes(counter: Long): ByteArray {
        val bytes = ByteArray(8)
        var value = counter
        for (i in 7 downTo 0) {
            bytes[i] = (value and 0xFF).toByte()
            value = value shr 8
        }
        return bytes
    }

    private fun truncate(hmac: ByteArray, digits: Int): String {
        val offset = hmac[hmac.size - 1].toInt() and 0x0F
        val code = ((hmac[offset].toInt() and 0x7F) shl 24) or
                ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
                ((hmac[offset + 2].toInt() and 0xFF) shl 8) or
                (hmac[offset + 3].toInt() and 0xFF)
        val mod = powOfTen(digits)
        val otp = code % mod
        return otp.toString().padStart(digits, '0')
    }

    private fun powOfTen(n: Int): Int {
        var result = 1
        repeat(n) { result *= 10 }
        return result
    }
}
