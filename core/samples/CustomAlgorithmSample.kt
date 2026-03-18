package com.maoungedev.beauthy.core.crypto.samples

import com.maoungedev.beauthy.core.crypto.HmacAlgorithm
import com.maoungedev.beauthy.core.crypto.JvmHmacProvider
import com.maoungedev.beauthy.core.crypto.TotpGenerator

/**
 * TOTP generation with SHA-256 algorithm, 8 digits, and 60s period.
 */
fun main() {
    val generator = TotpGenerator(JvmHmacProvider())

    val secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZA"
    val now = System.currentTimeMillis()

    val code = generator.generate(
        secret = secret,
        timestampMillis = now,
        algorithm = HmacAlgorithm.SHA256,
        digits = 8,
        period = 60
    )

    println("TOTP (SHA-256, 8 digits, 60s): $code")
}
