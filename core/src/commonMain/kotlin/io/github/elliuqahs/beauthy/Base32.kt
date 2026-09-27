package io.github.elliuqahs.beauthy

/**
 * Base32 encoding and decoding as defined in [RFC 4648 §6](https://tools.ietf.org/html/rfc4648#section-6).
 *
 * Decoding is lenient in the ways authenticator secrets are usually written:
 * it is case-insensitive, ignores whitespace, and accepts optional trailing `=` padding.
 */
public object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    // Lengths (mod 8) that a whole number of bytes can produce: 0, 2, 4, 5 or 7 characters.
    private val VALID_REMAINDERS = setOf(0, 2, 4, 5, 7)

    /**
     * Encodes [bytes] to an uppercase Base32 string.
     *
     * @param padding whether to append `=` padding to a multiple of 8 characters.
     *   Authenticator secrets are conventionally written without padding.
     */
    public fun encode(bytes: ByteArray, padding: Boolean = false): String {
        val sb = StringBuilder((bytes.size * 8 + 4) / 5 + 8)
        var buffer = 0
        var bitsLeft = 0
        for (b in bytes) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                sb.append(ALPHABET[(buffer shr (bitsLeft - 5)) and 0x1F])
                bitsLeft -= 5
            }
        }
        if (bitsLeft > 0) {
            sb.append(ALPHABET[(buffer shl (5 - bitsLeft)) and 0x1F])
        }
        if (padding) {
            while (sb.length % 8 != 0) sb.append('=')
        }
        return sb.toString()
    }

    /**
     * Decodes a Base32 string to raw bytes.
     *
     * @throws IllegalArgumentException if [input] is empty, contains characters outside
     *   the Base32 alphabet, has padding anywhere but the end, or has a length no byte
     *   sequence can encode to.
     */
    public fun decode(input: String): ByteArray {
        val clean = normalize(input)
        require(clean.isNotEmpty()) { "Base32 input must not be empty" }
        require(clean.all { it in ALPHABET }) { "Base32 input contains invalid characters" }
        require(clean.length % 8 in VALID_REMAINDERS) { "Base32 input has invalid length" }

        val output = ByteArray(clean.length * 5 / 8)
        var buffer = 0
        var bitsLeft = 0
        var index = 0
        for (c in clean) {
            buffer = (buffer shl 5) or ALPHABET.indexOf(c)
            bitsLeft += 5
            if (bitsLeft >= 8) {
                output[index++] = (buffer shr (bitsLeft - 8)).toByte()
                bitsLeft -= 8
            }
        }
        return output
    }

    /**
     * Returns `true` if [decode] would accept [input].
     */
    public fun isValid(input: String): Boolean {
        val clean = normalize(input)
        return clean.isNotEmpty() &&
            clean.all { it in ALPHABET } &&
            clean.length % 8 in VALID_REMAINDERS
    }

    private fun normalize(input: String): String =
        input.filterNot { it.isWhitespace() }.uppercase().trimEnd('=')
}
