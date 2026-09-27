package io.github.elliuqahs.beauthy

/**
 * HMAC hash function used to derive one-time passwords.
 *
 * Matches the `algorithm` parameter of `otpauth://` URIs and the variants
 * defined in [RFC 6238](https://tools.ietf.org/html/rfc6238).
 */
public enum class HmacAlgorithm(
    /** Digest size in bytes. */
    public val digestLength: Int
) {
    SHA1(20),
    SHA256(32),
    SHA512(64)
}
