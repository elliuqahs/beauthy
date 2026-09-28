package io.github.elliuqahs.beauthy

internal const val MIN_DIGITS: Int = 6
internal const val MAX_DIGITS: Int = 9

private val POWERS_OF_TEN = IntArray(MAX_DIGITS + 1).also {
    it[0] = 1
    for (i in 1..MAX_DIGITS) it[i] = it[i - 1] * 10
}

/**
 * HOTP value for [counter] as defined in [RFC 4226 §5.3](https://tools.ietf.org/html/rfc4226#section-5.3).
 * TOTP is the same computation with a time-derived counter.
 */
internal fun generateOtp(key: ByteArray, counter: Long, digits: Int, algorithm: HmacAlgorithm): String {
    val counterBytes = ByteArray(8)
    var value = counter
    for (i in 7 downTo 0) {
        counterBytes[i] = (value and 0xFF).toByte()
        value = value shr 8
    }
    try {
        val digest = hmac(algorithm, key, counterBytes)
        try {
            val offset = digest[digest.size - 1].toInt() and 0x0F
            val binary = ((digest[offset].toInt() and 0x7F) shl 24) or
                ((digest[offset + 1].toInt() and 0xFF) shl 16) or
                ((digest[offset + 2].toInt() and 0xFF) shl 8) or
                (digest[offset + 3].toInt() and 0xFF)
            return (binary % POWERS_OF_TEN[digits]).toString().padStart(digits, '0')
        } finally {
            digest.fill(0)
        }
    } finally {
        counterBytes.fill(0)
    }
}

internal fun requireDigits(digits: Int) {
    require(digits in MIN_DIGITS..MAX_DIGITS) { "digits must be in $MIN_DIGITS..$MAX_DIGITS, was $digits" }
}

internal fun decodeSecret(secret: String): ByteArray = Base32.decode(secret)

/**
 * Strips whitespace so a code copied from [TotpCode.formatted] (`"861 370"`) still verifies.
 */
internal fun normalizeCode(code: String): String = code.filterNot { it.isWhitespace() }

/**
 * Compares two codes in time independent of where they first differ.
 */
internal fun constantTimeEquals(a: String, b: String): Boolean {
    if (a.length != b.length) return false
    var diff = 0
    for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
    return diff == 0
}
