package io.github.elliuqahs.beauthy

/**
 * Counter-based one-time passwords ([RFC 4226](https://tools.ietf.org/html/rfc4226)).
 *
 * ```kotlin
 * val hotp = Hotp(secret = "JBSWY3DPEHPK3PXP")
 * val code = hotp.generate(counter = 42)
 * ```
 *
 * The decoded secret is held in memory for the lifetime of this instance.
 *
 * @param secret Base32-encoded shared secret
 * @param algorithm HMAC algorithm (default [HmacAlgorithm.SHA1])
 * @param digits code length, 6 to 9 (default 6)
 * @throws IllegalArgumentException if [secret] is not valid Base32 or [digits] is out of range
 */
public class Hotp(
    secret: String,
    public val algorithm: HmacAlgorithm = HmacAlgorithm.SHA1,
    public val digits: Int = 6
) {
    private val key: ByteArray

    init {
        requireDigits(digits)
        key = decodeSecret(secret)
    }

    /**
     * Returns the code for [counter].
     *
     * @throws IllegalArgumentException if [counter] is negative
     */
    public fun generate(counter: Long): String {
        require(counter >= 0) { "counter must not be negative, was $counter" }
        return generateOtp(key, counter, digits, algorithm)
    }

    /**
     * Checks [code] against [counter] and up to [lookAhead] following counters.
     *
     * Whitespace in [code] is ignored. Returns the counter that matched, or `null` if none did. After a successful match
     * the next expected counter is the returned value plus one; store it so the same
     * code cannot be accepted twice.
     *
     * @throws IllegalArgumentException if [counter] or [lookAhead] is negative
     */
    public fun verify(code: String, counter: Long, lookAhead: Int = 0): Long? {
        require(counter >= 0) { "counter must not be negative, was $counter" }
        require(lookAhead >= 0) { "lookAhead must not be negative, was $lookAhead" }
        val normalized = normalizeCode(code)
        for (candidate in counter..counter + lookAhead) {
            if (constantTimeEquals(generateOtp(key, candidate, digits, algorithm), normalized)) return candidate
        }
        return null
    }

    /**
     * Two instances are equal when they produce the same codes: the same decoded secret,
     * [algorithm] and [digits].
     */
    override fun equals(other: Any?): Boolean =
        other is Hotp &&
            key.contentEquals(other.key) &&
            algorithm == other.algorithm &&
            digits == other.digits

    override fun hashCode(): Int {
        var result = key.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + digits
        return result
    }

    /** Hides the secret so instances are safe to log. */
    override fun toString(): String = "Hotp(secret=***, algorithm=$algorithm, digits=$digits)"
}
