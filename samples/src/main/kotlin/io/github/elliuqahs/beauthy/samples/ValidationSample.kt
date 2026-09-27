package io.github.elliuqahs.beauthy.samples

import io.github.elliuqahs.beauthy.Base32

/**
 * Validating secrets typed in by the user before saving them.
 */
fun validationSample() {
    val inputs = listOf(
        "JBSWY3DPEHPK3PXP",    // valid
        "jbsw y3dp ehpk 3pxp", // valid (case and spaces are ignored)
        "INVALID!KEY",         // invalid character
        "ABC",                 // invalid length
        "",                    // empty
    )

    for (input in inputs) {
        val display = input.ifEmpty { "(empty)" }
        if (Base32.isValid(input)) {
            println("✓ \"$display\" → ${Base32.decode(input).size} bytes")
        } else {
            println("✗ \"$display\" → invalid Base32")
        }
    }
}
