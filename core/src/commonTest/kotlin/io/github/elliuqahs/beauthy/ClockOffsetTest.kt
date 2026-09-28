package io.github.elliuqahs.beauthy

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClockOffsetTest {

    // Far enough to land in a different time step for any period used here.
    private val tenMinutes = 10 * 60 * 1000L

    @Test
    fun from_isDifferenceToReferenceTime() {
        val ahead = ClockOffset.from(currentTimeMillis() + 5_000L)
        val behind = ClockOffset.from(currentTimeMillis() - 5_000L)
        assertTrue(abs(ahead - 5_000L) < 1_000L, "ahead=$ahead")
        assertTrue(abs(behind + 5_000L) < 1_000L, "behind=$behind")
    }

    @Test
    fun current_appliesOffset() {
        val totp = Totp(RfcVectors.SECRET_SHA1, clockOffsetMillis = tenMinutes)
        val before = currentTimeMillis() + tenMinutes
        val current = totp.current()
        val after = currentTimeMillis() + tenMinutes
        assertTrue(current.timestampMillis in before..after)
    }

    @Test
    fun defaultTimestamps_applyOffset() {
        val plain = Totp(RfcVectors.SECRET_SHA1)
        val shifted = Totp(RfcVectors.SECRET_SHA1, clockOffsetMillis = tenMinutes)
        val before = currentTimeMillis() + tenMinutes
        val code = shifted.generate()
        val after = currentTimeMillis() + tenMinutes
        assertTrue(code == plain.generate(before) || code == plain.generate(after))
    }

    @Test
    fun explicitTimestamps_ignoreOffset() {
        val plain = Totp(RfcVectors.SECRET_SHA1)
        val shifted = Totp(RfcVectors.SECRET_SHA1, clockOffsetMillis = tenMinutes)
        assertEquals(plain.generate(59_000L), shifted.generate(59_000L))
        assertEquals(plain.at(59_000L), shifted.at(59_000L))
        assertEquals(plain.remainingSeconds(15_000L), shifted.remainingSeconds(15_000L))
    }

    @Test
    fun verify_usesOffsetClock() {
        val plain = Totp(RfcVectors.SECRET_SHA1)
        val shifted = Totp(RfcVectors.SECRET_SHA1, clockOffsetMillis = tenMinutes)

        // A code for "now" is outside the shifted clock's window...
        assertFalse(shifted.verify(plain.generate(currentTimeMillis()), window = 1))
        // ...while a code for "now + offset" is inside it.
        assertTrue(shifted.verify(plain.generate(currentTimeMillis() + tenMinutes), window = 1))
    }

    @Test
    fun otpAuthUri_toTotp_passesOffset() {
        val uri = OtpAuthUri(OtpType.TOTP, RfcVectors.SECRET_SHA1, "alice")
        assertEquals(1234L, uri.toTotp(clockOffsetMillis = 1234L).clockOffsetMillis)
        assertEquals(0L, uri.toTotp().clockOffsetMillis)
    }
}
