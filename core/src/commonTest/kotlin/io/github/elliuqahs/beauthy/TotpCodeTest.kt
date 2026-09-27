package io.github.elliuqahs.beauthy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TotpCodeTest {

    private val totp = Totp(RfcVectors.SECRET_SHA1)

    @Test
    fun at_matchesGenerate() {
        assertEquals(totp.generate(59_000L), totp.at(59_000L).code)
        assertEquals("94287082", Totp(RfcVectors.SECRET_SHA1, digits = 8).at(59_000L).code)
    }

    @Test
    fun at_validityWindow() {
        val code = totp.at(45_500L)
        assertEquals(30_000L, code.validFromMillis)
        assertEquals(60_000L, code.expiresAtMillis)
        assertEquals(30, code.period)
    }

    @Test
    fun remainingSeconds_matchesTotpRemainingSeconds() {
        listOf(0L, 1L, 15_000L, 15_500L, 29_000L, 29_999L, 30_000L, 1111111111_000L).forEach { t ->
            assertEquals(totp.remainingSeconds(t), totp.at(t).remainingSeconds, "t=$t")
        }
    }

    @Test
    fun remainingSeconds_customPeriod() {
        assertEquals(50, Totp(RfcVectors.SECRET_SHA1, period = 60).at(10_000L).remainingSeconds)
    }

    @Test
    fun progress_fallsFromOneTowardsZero() {
        assertEquals(1.0f, totp.at(30_000L).progress)
        assertEquals(0.5f, totp.at(45_000L).progress)
        assertEquals(0.25f, totp.at(52_500L).progress)
        assertTrue(totp.at(59_999L).progress > 0f)
    }

    @Test
    fun current_isForNow() {
        val before = currentTimeMillis()
        val current = totp.current()
        val after = currentTimeMillis()
        assertTrue(current.validFromMillis <= after && current.expiresAtMillis > before)
        assertTrue(current.code == totp.generate(before) || current.code == totp.generate(after))
    }

    @Test
    fun formatted_groupsByLength() {
        assertEquals("287 082", Totp(RfcVectors.SECRET_SHA1, digits = 6).at(59_000L).formatted())
        assertEquals("428 7082", Totp(RfcVectors.SECRET_SHA1, digits = 7).at(59_000L).formatted())
        assertEquals("9428 7082", Totp(RfcVectors.SECRET_SHA1, digits = 8).at(59_000L).formatted())
        assertEquals(11, Totp(RfcVectors.SECRET_SHA1, digits = 9).at(59_000L).formatted().length)
    }

    @Test
    fun formatted_customSeparator() {
        assertEquals("287-082", totp.at(59_000L).formatted(separator = "-"))
    }

    @Test
    fun sameTimestamp_isEqual() {
        assertEquals(totp.at(59_000L), totp.at(59_000L))
        assertEquals(totp.at(59_000L).hashCode(), totp.at(59_000L).hashCode())
    }

    @Test
    fun toString_hidesCode() {
        val code = totp.at(59_000L)
        assertFalse(code.code in code.toString())
        assertTrue("code=***" in code.toString())
    }

    @Test
    fun at_negativeTimestamp_throws() {
        assertFailsWith<IllegalArgumentException> { totp.at(-1L) }
    }
}
