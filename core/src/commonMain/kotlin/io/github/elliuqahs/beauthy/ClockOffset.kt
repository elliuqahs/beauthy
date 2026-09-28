package io.github.elliuqahs.beauthy

/**
 * Helps correct a device clock that is set wrong, so TOTP codes still match the server.
 *
 * TOTP codes depend only on the current time, so a device whose clock is off by more
 * than about 30 seconds produces codes the server rejects. Compare the device clock with
 * a trusted time once, for example the `Date` header of any HTTPS response, and pass the
 * result to [Totp]:
 *
 * ```kotlin
 * val offset = ClockOffset.from(serverTimeMillis)
 * val totp = Totp(secret, clockOffsetMillis = offset)
 * ```
 */
public object ClockOffset {

    /**
     * Returns how many milliseconds to add to this device's clock to match
     * [referenceTimeMillis], a trusted current time in milliseconds since the Unix epoch.
     *
     * Positive when the device clock is behind, negative when it is ahead.
     */
    public fun from(referenceTimeMillis: Long): Long = referenceTimeMillis - currentTimeMillis()
}
