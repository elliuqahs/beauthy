package io.github.elliuqahs.beauthy

/**
 * Generates shared secrets for enrolling new accounts.
 */
public object Secret {

    /** Default secret size in bytes (160 bits, as recommended by RFC 4226). */
    public const val DEFAULT_BYTE_LENGTH: Int = 20

    /** Smallest secret size accepted, in bytes (128 bits, the RFC 4226 minimum). */
    public const val MIN_BYTE_LENGTH: Int = 16

    /**
     * Returns a new random secret, Base32-encoded without padding.
     *
     * Bytes come from the platform's cryptographically secure random source.
     *
     * @throws IllegalArgumentException if [byteLength] is below [MIN_BYTE_LENGTH]
     */
    public fun generate(byteLength: Int = DEFAULT_BYTE_LENGTH): String {
        require(byteLength >= MIN_BYTE_LENGTH) {
            "byteLength must be at least $MIN_BYTE_LENGTH, was $byteLength"
        }
        val bytes = secureRandomBytes(byteLength)
        try {
            return Base32.encode(bytes)
        } finally {
            bytes.fill(0)
        }
    }
}
