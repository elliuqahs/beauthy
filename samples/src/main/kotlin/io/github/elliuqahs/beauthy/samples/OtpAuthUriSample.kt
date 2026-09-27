package io.github.elliuqahs.beauthy.samples

import io.github.elliuqahs.beauthy.OtpAuthUri
import io.github.elliuqahs.beauthy.OtpType

/**
 * Reading an authenticator QR code, and creating one for a new account.
 */
fun otpAuthUriSample() {
    // Client: content scanned from a QR code
    val scanned = OtpAuthUri.parseOrNull(
        "otpauth://totp/GitHub:alice?secret=JBSWY3DPEHPK3PXP&issuer=GitHub"
    )
    if (scanned == null) {
        println("Not a valid otpauth:// QR code")
    } else {
        println("${scanned.issuer} (${scanned.accountName}): ${scanned.toTotp().generate()}")
    }

    // Server: content to encode into a QR code for enrollment
    val enrollment = OtpAuthUri(
        type = OtpType.TOTP,
        secret = "JBSWY3DPEHPK3PXP",
        accountName = "alice@example.com",
        issuer = "Example"
    )
    println(enrollment.toUriString())
}
