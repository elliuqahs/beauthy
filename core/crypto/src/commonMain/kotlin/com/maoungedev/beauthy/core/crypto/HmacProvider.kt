package com.maoungedev.beauthy.core.crypto

/**
 * Supported HMAC algorithms for OTP generation.
 *
 * Maps to the algorithm identifiers used in [RFC 6238](https://tools.ietf.org/html/rfc6238)
 * and the `otpauth://` URI `algorithm` parameter.
 */
enum class HmacAlgorithm {
    SHA1,
    SHA256,
    SHA512
}

/**
 * Platform-abstracted HMAC computation.
 *
 * Implementations must produce correct HMAC output for all [HmacAlgorithm] variants.
 * Key and data arrays should be treated as sensitive material; callers are responsible
 * for clearing them after use.
 */
interface HmacProvider {

    /**
     * Computes an HMAC digest.
     *
     * @param algorithm the hash algorithm to use
     * @param key the secret key bytes
     * @param data the message bytes
     * @return the raw HMAC digest (20 bytes for SHA-1, 32 for SHA-256, 64 for SHA-512)
     */
    fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray
}
