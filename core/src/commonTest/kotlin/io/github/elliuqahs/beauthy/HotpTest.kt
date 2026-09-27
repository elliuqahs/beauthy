package io.github.elliuqahs.beauthy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class HotpTest {

    // RFC 4226 Appendix D
    private val expected = listOf(
        "755224", "287082", "359152", "969429", "338314",
        "254676", "287922", "162583", "399871", "520489"
    )

    private val hotp = Hotp(RfcVectors.SECRET_SHA1)

    @Test
    fun generate_rfc4226() {
        expected.forEachIndexed { counter, code ->
            assertEquals(code, hotp.generate(counter.toLong()), "counter=$counter")
        }
    }

    @Test
    fun verify_exactCounter_returnsCounter() {
        assertEquals(3L, hotp.verify("969429", counter = 3))
    }

    @Test
    fun verify_withinLookAhead_returnsMatchedCounter() {
        assertEquals(5L, hotp.verify("254676", counter = 2, lookAhead = 3))
    }

    @Test
    fun verify_beyondLookAhead_returnsNull() {
        assertNull(hotp.verify("254676", counter = 2, lookAhead = 2))
    }

    @Test
    fun verify_wrongCode_returnsNull() {
        assertNull(hotp.verify("000000", counter = 0, lookAhead = 9))
    }

    @Test
    fun verify_earlierCounter_isNotAccepted() {
        assertNull(hotp.verify("755224", counter = 1, lookAhead = 5))
    }

    @Test
    fun invalidArguments_throw() {
        assertFailsWith<IllegalArgumentException> { Hotp(RfcVectors.SECRET_SHA1, digits = 5) }
        assertFailsWith<IllegalArgumentException> { Hotp(RfcVectors.SECRET_SHA1, digits = 10) }
        assertFailsWith<IllegalArgumentException> { Hotp("not base32!") }
        assertFailsWith<IllegalArgumentException> { hotp.generate(-1) }
        assertFailsWith<IllegalArgumentException> { hotp.verify("755224", counter = 0, lookAhead = -1) }
    }
}
