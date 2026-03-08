package com.maoungedev.beauthy.core.crypto

object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun isValid(input: String): Boolean {
        val clean = input.uppercase().replace(" ", "").replace("=", "")
        if (clean.isEmpty()) return false
        return clean.all { it in ALPHABET }
    }

    fun decode(input: String): ByteArray {
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
