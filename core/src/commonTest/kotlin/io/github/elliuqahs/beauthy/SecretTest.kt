package io.github.elliuqahs.beauthy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class SecretTest {

    @Test
    fun generate_defaultLength_is20Bytes() {
        assertEquals(Secret.DEFAULT_BYTE_LENGTH, Base32.decode(Secret.generate()).size)
    }

    @Test
    fun generate_customLength() {
        assertEquals(32, Base32.decode(Secret.generate(32)).size)
    }

    @Test
    fun generate_isUsableAsTotpSecret() {
        assertEquals(6, Totp(Secret.generate()).generate(0L).length)
    }

    @Test
    fun generate_isRandom() {
        assertNotEquals(Secret.generate(), Secret.generate())
    }

    @Test
    fun generate_belowMinimum_throws() {
        assertFailsWith<IllegalArgumentException> { Secret.generate(Secret.MIN_BYTE_LENGTH - 1) }
    }
}
