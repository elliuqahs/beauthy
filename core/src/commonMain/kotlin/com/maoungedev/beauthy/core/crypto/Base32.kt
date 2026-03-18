package com.maoungedev.beauthy.core.crypto

/**
 * Base32 decoder following [RFC 4648](https://tools.ietf.org/html/rfc4648).
 *
 * Used to decode the shared secret from `otpauth://` URIs and manual user input.
 * Supports the standard alphabet (`A-Z`, `2-7`), optional padding (`=`),
 * spaces, and case-insensitive input.
 */
public object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    /**
     * Checks whether [input] contains only valid Base32 characters.
     *
     * Spaces and padding (`=`) are stripped before validation.
     *
     * @param input the string to validate
     * @return `true` if [input] is non-empty and contains only Base32 characters
     */
    public fun isValid(input: String): Boolean {
        val clean = input.uppercase().replace(" ", "").replace("=", "")
        if (clean.isEmpty()) return false
        return clean.all { it in ALPHABET }
    }

    /**
     * Decodes a Base32-encoded string to raw bytes.
     *
     * @param input Base32 string (case-insensitive, padding and spaces allowed)
     * @return decoded byte array
     * @throws IllegalArgumentException if [input] is empty or contains invalid characters
     */
    public fun decode(input: String): ByteArray {
        val clean = input.uppercase().replace(" ", "").trimEnd('=')
        require(clean.isNotEmpty()) { "Base32 input must not be empty" }
        require(clean.all { it in ALPHABET }) { "Invalid Base32 character in input" }

        val outputLength = clean.length * 5 / 8
        val buffer = ByteArray(outputLength)
        var bitsLeft = 0
        var current = 0
        var index = 0

        for (c in clean) {
            val value = ALPHABET.indexOf(c)
            current = (current shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                buffer[index++] = (current shr (bitsLeft - 8)).toByte()
                bitsLeft -= 8
            }
        }
        return buffer
    }
}
