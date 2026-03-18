package com.maoungedev.beauthy.core.crypto.samples

import com.maoungedev.beauthy.core.crypto.JvmHmacProvider
import com.maoungedev.beauthy.core.crypto.TotpGenerator

/**
 * Basic TOTP generation with default settings (SHA-1, 6 digits, 30s period).
 */
fun main() {
    val generator = TotpGenerator(JvmHmacProvider())

    val secret = "JBSWY3DPEHPK3PXP"
    val now = System.currentTimeMillis()

    val code = generator.generate(secret = secret, timestampMillis = now)
    val remaining = generator.remainingSeconds(now)

    println("TOTP code: $code")
    println("Expires in: ${remaining}s")
}
