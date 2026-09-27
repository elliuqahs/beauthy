package io.github.elliuqahs.beauthy.samples

import io.github.elliuqahs.beauthy.Hotp

/**
 * HOTP (counter-based) codes, and verifying one with a look-ahead window.
 */
fun hotpSample() {
    val hotp = Hotp(secret = "JBSWY3DPEHPK3PXP")

    for (counter in 0L..4L) {
        println("Counter $counter → HOTP: ${hotp.generate(counter)}")
    }

    // The user pressed the button a few extra times; accept up to 3 counters ahead.
    val matched = hotp.verify(code = hotp.generate(2), counter = 0, lookAhead = 3)
    println("Matched counter: $matched, next expected: ${matched?.plus(1)}")
}
