package com.maoungedev.beauthy.core.crypto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Base32Test {

    @Test
    fun isValid_validBase32_returnsTrue() {
        assertTrue(Base32.isValid("JBSWY3DPEHPK3PXP"))
        assertTrue(Base32.isValid("MFRGGZDFMY"))
        assertTrue(Base32.isValid("GEZDGNBVGY3TQOJQ"))
    }

    @Test
    fun isValid_withSpaces_returnsTrue() {
        assertTrue(Base32.isValid("JBSW Y3DP EHPK 3PXP"))
    }

    @Test
    fun isValid_withPadding_returnsTrue() {
        assertTrue(Base32.isValid("MFRGGZDFMY======"))
    }

    @Test
    fun isValid_emptyString_returnsFalse() {
        assertFalse(Base32.isValid(""))
    }

    @Test
    fun isValid_invalidChars_returnsFalse() {
        assertFalse(Base32.isValid("JBSWY3DPEHPK3PX!"))
        assertFalse(Base32.isValid("01289"))
        assertFalse(Base32.isValid("jbswy+/"))
    }

    @Test
    fun decode_knownVectors() {
        // RFC 4648 test vectors
        assertEquals("", Base32.decode("").decodeToString().ifEmpty { "" })
        assertEquals("f", Base32.decode("MY").decodeToString())
        assertEquals("fo", Base32.decode("MZXQ").decodeToString())
        assertEquals("foo", Base32.decode("MZXW6").decodeToString())
        assertEquals("foob", Base32.decode("MZXW6YQ").decodeToString())
        assertEquals("fooba", Base32.decode("MZXW6YTB").decodeToString())
        assertEquals("foobar", Base32.decode("MZXW6YTBOI").decodeToString())
    }

    @Test
    fun decode_withPadding_ignoresPadding() {
        assertEquals("foobar", Base32.decode("MZXW6YTBOI======").decodeToString())
    }

    @Test
    fun decode_caseInsensitive() {
        assertEquals("foobar", Base32.decode("mzxw6ytboi").decodeToString())
    }

    @Test
    fun decode_invalidChars_throws() {
        assertFailsWith<IllegalArgumentException> {
            Base32.decode("INVALID!")
        }
    }

    @Test
    fun decode_emptyString_throws() {
        assertFailsWith<IllegalArgumentException> {
            Base32.decode("")
        }
    }
}
