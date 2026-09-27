package io.github.elliuqahs.beauthy.samples

/**
 * Runs every sample. Start with `./gradlew :samples:run`.
 */
fun main() {
    val samples = listOf(
        "Basic TOTP" to ::basicTotpSample,
        "Custom algorithm" to ::customAlgorithmSample,
        "HOTP" to ::hotpSample,
        "otpauth:// URIs" to ::otpAuthUriSample,
        "Server verification" to ::serverVerificationSample,
        "Base32 validation" to ::validationSample,
    )
    for ((title, sample) in samples) {
        println("=== $title ===")
        sample()
        println()
    }
}
