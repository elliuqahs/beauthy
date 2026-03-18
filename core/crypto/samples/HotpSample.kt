package com.maoungedev.beauthy.core.crypto.samples

import com.maoungedev.beauthy.core.crypto.JvmHmacProvider
import com.maoungedev.beauthy.core.crypto.TotpGenerator

/**
 * HOTP (counter-based) code generation.
 */
fun main() {
    val generator = TotpGenerator(JvmHmacProvider())

    val secret = "JBSWY3DPEHPK3PXP"

    // Generate codes for consecutive counters
    for (counter in 0L..4L) {
        val code = generator.generateHotp(secret = secret, counter = counter)
        println("Counter $counter → HOTP: $code")
    }
}
