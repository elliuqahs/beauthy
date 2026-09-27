package io.github.elliuqahs.beauthy.samples

import io.github.elliuqahs.beauthy.Totp

/**
 * TOTP with default settings (SHA-1, 6 digits, 30s period), from common code.
 */
fun basicTotpSample() {
    val totp = Totp(secret = "JBSWY3DPEHPK3PXP")

    println("TOTP code: ${totp.generate()}")
    println("Expires in: ${totp.remainingSeconds()}s")
}
