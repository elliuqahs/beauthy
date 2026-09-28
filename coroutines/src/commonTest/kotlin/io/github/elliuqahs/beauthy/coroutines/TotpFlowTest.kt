package io.github.elliuqahs.beauthy.coroutines

import io.github.elliuqahs.beauthy.Totp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TotpFlowTest {

    private val totp = Totp("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ")

    @Test
    fun emitsImmediately() = runTest {
        val first = tickingCodes { totp.at(START + currentTime) }.first()
        assertEquals(0L, currentTime)
        assertEquals(totp.at(START), first)
    }

    @Test
    fun ticksEverySecond_countingDown() = runTest {
        val codes = tickingCodes { totp.at(START + currentTime) }.take(4).toList()
        assertEquals(listOf(30, 29, 28, 27), codes.map { it.remainingSeconds })
    }

    @Test
    fun alignsToWholeSeconds() = runTest {
        // Starting 400 ms into a second, the next tick waits only 600 ms.
        val codes = tickingCodes { totp.at(START + 400L + currentTime) }.take(3).toList()
        assertEquals(listOf(START + 400L, START + 1000L, START + 2000L), codes.map { it.timestampMillis })
    }

    @Test
    fun codeChangesAtPeriodBoundary() = runTest {
        // 2 seconds before the 30s boundary: codes for t=28s, 29s, 30s.
        val codes = tickingCodes { totp.at(28_000L + currentTime) }.take(3).toList()
        assertEquals(codes[0].code, codes[1].code)
        assertTrue(codes[1].code != codes[2].code)
        assertEquals(totp.generate(30_000L), codes[2].code)
        assertEquals(30, codes[2].remainingSeconds)
    }

    @Test
    fun codes_emitsCurrentCode() = runTest {
        val first = totp.codes().first()
        assertEquals(totp.generate(first.timestampMillis), first.code)
    }

    @Test
    fun codes_usesClockOffset() = runTest {
        val offset = 10 * 60 * 1000L
        val shifted = Totp("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", clockOffsetMillis = offset)
        val plainNow = totp.current().timestampMillis
        val first = shifted.codes().first()
        assertTrue(first.timestampMillis - plainNow in offset..offset + 1_000L)
        assertEquals(totp.generate(first.timestampMillis), first.code)
    }

    private companion object {
        // Start of a period: 1111111110 s is a multiple of 30.
        const val START = 1111111110_000L
    }
}
