package io.github.elliuqahs.beauthy

/**
 * Time-based one-time passwords ([RFC 6238](https://tools.ietf.org/html/rfc6238)).
 *
 * ```kotlin
 * val totp = Totp(secret = "JBSWY3DPEHPK3PXP")
 *
 * val current = totp.current()            // code + countdown for display
 * current.formatted()                     // "861 370"
 * current.remainingSeconds                // 15
 *
 * val ok = totp.verify(userInput)         // accepts ±1 period of clock drift
 * ```
 *
 * Every function that takes a timestamp defaults to the current system time; pass one
 * explicitly for tests or to use your own clock.
 *
 * The decoded secret is held in memory for the lifetime of this instance.
 *
 * @param secret Base32-encoded shared secret
 * @param algorithm HMAC algorithm (default [HmacAlgorithm.SHA1])
 * @param digits code length, 6 to 9 (default 6)
 * @param period time step in seconds (default 30)
 * @throws IllegalArgumentException if [secret] is not valid Base32, or [digits] or [period] is out of range
 */
public class Totp(
    secret: String,
    public val algorithm: HmacAlgorithm = HmacAlgorithm.SHA1,
    public val digits: Int = 6,
    public val period: Int = 30
) {
    private val key: ByteArray

    init {
        requireDigits(digits)
        require(period > 0) { "period must be positive, was $period" }
        key = decodeSecret(secret)
    }

    /**
     * Returns the code for the current time with its countdown and progress.
     *
     * To keep a display up to date, call this again every second, or schedule the next
     * refresh for [TotpCode.expiresAtMillis].
     */
    public fun current(): TotpCode = at(currentTimeMillis())

    /**
     * Returns the code for [timestampMillis] with its countdown and progress.
     *
     * @throws IllegalArgumentException if [timestampMillis] is negative
     */
    public fun at(timestampMillis: Long): TotpCode {
        val step = timeStep(timestampMillis)
        val validFrom = step * period * 1000L
        return TotpCode(
            code = generateOtp(key, step, digits, algorithm),
            validFromMillis = validFrom,
            expiresAtMillis = validFrom + period * 1000L,
            timestampMillis = timestampMillis
        )
    }

    /**
     * Returns just the code for the time step containing [timestampMillis].
     *
     * Use [current] or [at] when you also need the countdown.
     *
     * @throws IllegalArgumentException if [timestampMillis] is negative
     */
    public fun generate(timestampMillis: Long = currentTimeMillis()): String =
        generateOtp(key, timeStep(timestampMillis), digits, algorithm)

    /**
     * Seconds until the code for [timestampMillis] expires, from 1 to [period].
     */
    public fun remainingSeconds(timestampMillis: Long = currentTimeMillis()): Int {
        requireTimestamp(timestampMillis)
        return (period - (timestampMillis / 1000L) % period).toInt()
    }

    /**
     * Checks [code] against the time step containing [timestampMillis] and [window]
     * steps on either side, to tolerate clock drift between client and server.
     *
     * A code stays valid for its whole window, so a server should remember which time
     * step each user last authenticated with and reject reuse.
     *
     * @throws IllegalArgumentException if [timestampMillis] or [window] is negative
     */
    public fun verify(
        code: String,
        timestampMillis: Long = currentTimeMillis(),
        window: Int = 1
    ): Boolean {
        require(window >= 0) { "window must not be negative, was $window" }
        val current = timeStep(timestampMillis)
        var matched = false
        for (step in (current - window).coerceAtLeast(0)..current + window) {
            // Check every step so the time taken does not reveal which one matched.
            if (constantTimeEquals(generateOtp(key, step, digits, algorithm), code)) matched = true
        }
        return matched
    }

    private fun timeStep(timestampMillis: Long): Long {
        requireTimestamp(timestampMillis)
        return timestampMillis / 1000L / period
    }

    private fun requireTimestamp(timestampMillis: Long) {
        require(timestampMillis >= 0) { "timestampMillis must not be negative, was $timestampMillis" }
    }
}
