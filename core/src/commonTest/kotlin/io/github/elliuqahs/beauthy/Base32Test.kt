package io.github.elliuqahs.beauthy

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Base32Test {

    // RFC 4648 §10
    private val vectors = listOf(
        "f" to "MY======",
        "fo" to "MZXQ====",
        "foo" to "MZXW6===",
        "foob" to "MZXW6YQ=",
        "fooba" to "MZXW6YTB",
        "foobar" to "MZXW6YTBOI======"
    )

    @Test
    fun encode_rfc4648_withPadding() {
        vectors.forEach { (plain, encoded) ->
            assertEquals(encoded, Base32.encode(plain.encodeToByteArray(), padding = true))
        }
    }

    @Test
    fun encode_rfc4648_withoutPadding() {
        vectors.forEach { (plain, encoded) ->
            assertEquals(encoded.trimEnd('='), Base32.encode(plain.encodeToByteArray()))
        }
    }

    @Test
    fun encode_empty() {
        assertEquals("", Base32.encode(ByteArray(0)))
    }

    @Test
    fun decode_rfc4648() {
        vectors.forEach { (plain, encoded) ->
            assertEquals(plain, Base32.decode(encoded).decodeToString())
            assertEquals(plain, Base32.decode(encoded.trimEnd('=')).decodeToString())
        }
    }

    @Test
    fun decode_isLenientAboutCaseAndWhitespace() {
        assertEquals("foobar", Base32.decode("mzxw 6ytb\toi").decodeToString())
    }

    @Test
    fun roundTrip_allByteValues() {
        val bytes = ByteArray(256) { it.toByte() }
        assertContentEquals(bytes, Base32.decode(Base32.encode(bytes)))
    }

    @Test
    fun decode_rejectsInvalidInput() {
        listOf("", "   ", "====", "MZXW6YT!", "MZ=XW6", "A", "ABC", "ABCDEF").forEach { input ->
            assertFailsWith<IllegalArgumentException>("input=\"$input\"") { Base32.decode(input) }
        }
    }

    @Test
    fun isValid_agreesWithDecode() {
        listOf("JBSWY3DPEHPK3PXP", "jbsw y3dp ehpk 3pxp", "MZXW6YTBOI======").forEach {
            assertTrue(Base32.isValid(it), it)
        }
        listOf("", "MZ=XW6", "A", "ABC", "ABCDEF", "01289").forEach {
            assertFalse(Base32.isValid(it), it)
        }
    }
}
