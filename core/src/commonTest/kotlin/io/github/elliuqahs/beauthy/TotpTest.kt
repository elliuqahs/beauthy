package io.github.elliuqahs.beauthy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TotpTest {

    // RFC 6238 Appendix B test times, in seconds
    private val times = listOf(59L, 1111111109L, 1111111111L, 1234567890L, 2000000000L, 20000000000L)

    @Test
    fun generate_rfc6238_sha1() {
        assertVectors(
            Totp(RfcVectors.SECRET_SHA1, HmacAlgorithm.SHA1, digits = 8),
            "94287082", "07081804", "14050471", "89005924", "69279037", "65353130"
        )
    }

    @Test
    fun generate_rfc6238_sha256() {
        assertVectors(
            Totp(RfcVectors.SECRET_SHA256, HmacAlgorithm.SHA256, digits = 8),
            "46119246", "68084774", "67062674", "91819424", "90698825", "77737706"
        )
    }

    @Test
    fun generate_rfc6238_sha512() {
        assertVectors(
            Totp(RfcVectors.SECRET_SHA512, HmacAlgorithm.SHA512, digits = 8),
            "90693936", "25091201", "99943326", "93441116", "38618901", "47863826"
        )
    }

    @Test
    fun generate_defaultDigits_isLastSixOfRfcValue() {
        assertEquals("287082", Totp(RfcVectors.SECRET_SHA1).generate(59_000L))
    }

    @Test
    fun generate_nineDigits() {
        assertEquals(9, Totp(RfcVectors.SECRET_SHA1, digits = 9).generate(59_000L).length)
    }

    @Test
    fun generate_withoutTimestamp_matchesCurrentTime() {
        val totp = Totp(RfcVectors.SECRET_SHA1)
        val before = currentTimeMillis()
        val code = totp.generate()
        val after = currentTimeMillis()
        assertTrue(code == totp.generate(before) || code == totp.generate(after))
    }

    @Test
    fun remainingSeconds() {
        val totp = Totp(RfcVectors.SECRET_SHA1)
        assertEquals(30, totp.remainingSeconds(0L))
        assertEquals(30, totp.remainingSeconds(30_000L))
        assertEquals(15, totp.remainingSeconds(15_000L))
        assertEquals(1, totp.remainingSeconds(29_999L))
        assertEquals(50, Totp(RfcVectors.SECRET_SHA1, period = 60).remainingSeconds(10_000L))
    }

    @Test
    fun verify_acceptsCurrentAndAdjacentSteps() {
        val totp = Totp(RfcVectors.SECRET_SHA1)
        val now = 1111111111_000L
        val code = totp.generate(now)
        assertTrue(totp.verify(code, now))
        assertTrue(totp.verify(code, now + 30_000L))
        assertTrue(totp.verify(code, now - 30_000L))
    }

    @Test
    fun verify_rejectsOutsideWindow() {
        val totp = Totp(RfcVectors.SECRET_SHA1)
        val now = 1111111111_000L
        val code = totp.generate(now)
        assertFalse(totp.verify(code, now + 60_000L))
        assertFalse(totp.verify(code, now + 30_000L, window = 0))
    }

    @Test
    fun verify_rejectsWrongCode() {
        val totp = Totp(RfcVectors.SECRET_SHA1)
        assertFalse(totp.verify("000000", 59_000L, window = 0))
        assertFalse(totp.verify("28708", 59_000L))
        assertFalse(totp.verify("", 59_000L))
    }

    @Test
    fun verify_nearEpoch_doesNotUseNegativeSteps() {
        val totp = Totp(RfcVectors.SECRET_SHA1)
        assertTrue(totp.verify(totp.generate(0L), 0L, window = 2))
    }

    @Test
    fun invalidArguments_throw() {
        assertFailsWith<IllegalArgumentException> { Totp(RfcVectors.SECRET_SHA1, digits = 5) }
        assertFailsWith<IllegalArgumentException> { Totp(RfcVectors.SECRET_SHA1, digits = 10) }
        assertFailsWith<IllegalArgumentException> { Totp(RfcVectors.SECRET_SHA1, period = 0) }
        assertFailsWith<IllegalArgumentException> { Totp("") }
        assertFailsWith<IllegalArgumentException> { Totp("A") }

        val totp = Totp(RfcVectors.SECRET_SHA1)
        assertFailsWith<IllegalArgumentException> { totp.generate(-1L) }
        assertFailsWith<IllegalArgumentException> { totp.remainingSeconds(-1L) }
        assertFailsWith<IllegalArgumentException> { totp.verify("287082", 59_000L, window = -1) }
    }

    private fun assertVectors(totp: Totp, vararg expected: String) {
        times.zip(expected.toList()).forEach { (seconds, code) ->
            assertEquals(code, totp.generate(seconds * 1000L), "${totp.algorithm} at t=$seconds")
        }
    }
}
