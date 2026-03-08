package com.maoungedev.beauthy.core.crypto

class TotpGenerator(
    private val hmacProvider: HmacProvider
) {
    fun generate(secret: String, timestampMillis: Long, digits: Int = 6, period: Int = 30): String {
        val counter = timestampMillis / 1000L / period.toLong()
        return generateByCounter(secret, counter, digits)
    }

    fun generateHotp(secret: String, counter: Long, digits: Int = 6): String {
        return generateByCounter(secret, counter, digits)
    }

    fun remainingSeconds(timestampMillis: Long, period: Int = 30): Int {
        val seconds = timestampMillis / 1000L
        return (period - (seconds % period)).toInt()
    }

    private fun generateByCounter(secret: String, counter: Long, digits: Int): String {
        val key = Base32.decode(secret)
        try {
            val counterBytes = counterToBytes(counter)
            try {
                val hmac = hmacProvider.hmacSha1(key, counterBytes)
                try {
                    return truncate(hmac, digits)
                } finally {
                    hmac.fill(0)
                }
            } finally {
                counterBytes.fill(0)
            }
        } finally {
            key.fill(0)
        }
    }

    private fun counterToBytes(counter: Long): ByteArray {
        val bytes = ByteArray(8)
        var value = counter
        for (i in 7 downTo 0) {
            bytes[i] = (value and 0xFF).toByte()
            value = value shr 8
        }
        return bytes
    }

    private fun truncate(hmac: ByteArray, digits: Int): String {
        val offset = hmac[hmac.size - 1].toInt() and 0x0F
        val code = ((hmac[offset].toInt() and 0x7F) shl 24) or
                ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
                ((hmac[offset + 2].toInt() and 0xFF) shl 8) or
                (hmac[offset + 3].toInt() and 0xFF)
        val mod = powOfTen(digits)
        val otp = code % mod
        return otp.toString().padStart(digits, '0')
    }

    private fun powOfTen(n: Int): Int {
        var result = 1
        repeat(n) { result *= 10 }
        return result
    }
}
