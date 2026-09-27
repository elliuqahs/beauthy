package io.github.elliuqahs.beauthy

/**
 * Computes an HMAC digest using the platform's native crypto implementation.
 */
internal expect fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray

/**
 * Returns [size] bytes from the platform's cryptographically secure random source.
 */
internal expect fun secureRandomBytes(size: Int): ByteArray

/**
 * Current wall-clock time in milliseconds since the Unix epoch.
 */
internal expect fun currentTimeMillis(): Long
