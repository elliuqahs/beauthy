package io.github.elliuqahs.beauthy.samples

import io.github.elliuqahs.beauthy.HmacAlgorithm
import io.github.elliuqahs.beauthy.Totp

/**
 * TOTP with SHA-256, 8 digits and a 60s period.
 */
fun customAlgorithmSample() {
    val totp = Totp(
        secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZA",
        algorithm = HmacAlgorithm.SHA256,
        digits = 8,
        period = 60
    )

    println("TOTP (SHA-256, 8 digits, 60s): ${totp.generate()}")
}
