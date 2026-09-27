package io.github.elliuqahs.beauthy.samples

import io.github.elliuqahs.beauthy.OtpAuthUri
import io.github.elliuqahs.beauthy.OtpType
import io.github.elliuqahs.beauthy.Secret
import io.github.elliuqahs.beauthy.Totp

/**
 * Server-side two-factor flow: enroll a user, then verify their codes.
 */
fun serverVerificationSample() {
    // 1. Enrollment: generate and store a secret, show it to the user as a QR code
    val secret = Secret.generate()
    val qrContent = OtpAuthUri(OtpType.TOTP, secret, accountName = "alice@example.com", issuer = "Example")
        .toUriString()
    println("Show as QR code: $qrContent")

    // 2. Login: check the code the user typed, allowing ±1 period of clock drift
    val totp = Totp(secret)
    val userInput = totp.generate() // stands in for what the user typed
    println("Code accepted: ${totp.verify(userInput)}")
}
