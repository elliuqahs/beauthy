package io.github.elliuqahs.beauthy

/**
 * A TOTP code together with its validity window, everything a UI needs to show it.
 *
 * All values are derived from the single timestamp the code was created for, so the
 * code, countdown and progress always agree with each other.
 *
 * ```kotlin
 * val current = totp.current()
 * codeText.text = current.formatted()          // "861 370"
 * countdown.text = "${current.remainingSeconds}s"
 * progressBar.progress = current.progress
 * ```
 *
 * [toString] hides the code so instances are safe to log.
 */
public class TotpCode internal constructor(
    /** The one-time password, zero-padded to the configured number of digits. */
    public val code: String,
    /** Start of the time step this code belongs to, in milliseconds since the Unix epoch. */
    public val validFromMillis: Long,
    /** End of the time step (exclusive); the next code starts at this instant. */
    public val expiresAtMillis: Long,
    /** The instant this snapshot describes; [remainingSeconds] and [progress] are relative to it. */
    public val timestampMillis: Long
) {
    /** Length of the time step in seconds. */
    public val period: Int
        get() = ((expiresAtMillis - validFromMillis) / 1000L).toInt()

    /** Whole seconds until this code expires, from 1 to [period]. Suitable for a countdown label. */
    public val remainingSeconds: Int
        get() = ((expiresAtMillis - timestampMillis + 999L) / 1000L).toInt()

    /**
     * Fraction of the time step still remaining, from `1.0` just after the code appears
     * down towards `0.0` as it expires. Millisecond precision, suitable for a progress bar.
     */
    public val progress: Float
        get() = (expiresAtMillis - timestampMillis).toFloat() / (expiresAtMillis - validFromMillis)

    /**
     * The code split into groups for display: `"861 370"` for 6 digits,
     * `"8617 3708"` for 8, `"861 3708"` for 7 and `"861 370 124"` for 9.
     */
    public fun formatted(separator: String = " "): String {
        val groups = when (code.length) {
            7 -> listOf(3, 4)
            8 -> listOf(4, 4)
            else -> List(code.length / 3) { 3 }
        }
        var start = 0
        return groups.joinToString(separator) { size ->
            code.substring(start, start + size).also { start += size }
        }
    }

    override fun equals(other: Any?): Boolean =
        other is TotpCode &&
            code == other.code &&
            validFromMillis == other.validFromMillis &&
            expiresAtMillis == other.expiresAtMillis &&
            timestampMillis == other.timestampMillis

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + validFromMillis.hashCode()
        result = 31 * result + expiresAtMillis.hashCode()
        result = 31 * result + timestampMillis.hashCode()
        return result
    }

    override fun toString(): String =
        "TotpCode(code=***, validFromMillis=$validFromMillis, expiresAtMillis=$expiresAtMillis, " +
            "remainingSeconds=$remainingSeconds)"
}
