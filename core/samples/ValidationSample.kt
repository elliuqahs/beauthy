package com.maoungedev.beauthy.core.crypto.samples

import com.maoungedev.beauthy.core.crypto.Base32

/**
 * Validate and decode Base32 secret keys before use.
 */
fun main() {
    val inputs = listOf(
        "JBSWY3DPEHPK3PXP",   // valid
        "JBSW Y3DP EHPK 3PXP", // valid (spaces allowed)
        "INVALID!KEY",          // invalid
        "",                     // invalid (empty)
    )

    for (input in inputs) {
        val display = input.ifEmpty { "(empty)" }
        if (Base32.isValid(input)) {
            val bytes = Base32.decode(input)
            println("✓ \"$display\" → ${bytes.size} bytes")
        } else {
            println("✗ \"$display\" → invalid Base32")
        }
    }
}
