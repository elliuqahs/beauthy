package io.github.elliuqahs.beauthy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OtpAuthUriTest {

    @Test
    fun parse_minimalTotp_appliesDefaults() {
        val uri = OtpAuthUri.parse("otpauth://totp/alice@example.com?secret=JBSWY3DPEHPK3PXP")
        assertEquals(OtpType.TOTP, uri.type)
        assertEquals("alice@example.com", uri.accountName)
        assertNull(uri.issuer)
        assertEquals("JBSWY3DPEHPK3PXP", uri.secret)
        assertEquals(HmacAlgorithm.SHA1, uri.algorithm)
        assertEquals(6, uri.digits)
        assertEquals(30, uri.period)
    }

    @Test
    fun parse_allParameters() {
        val uri = OtpAuthUri.parse(
            "otpauth://totp/ACME%20Co:john.doe%40email.com" +
                "?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&algorithm=SHA256&digits=8&period=60"
        )
        assertEquals("ACME Co", uri.issuer)
        assertEquals("john.doe@email.com", uri.accountName)
        assertEquals(HmacAlgorithm.SHA256, uri.algorithm)
        assertEquals(8, uri.digits)
        assertEquals(60, uri.period)
    }

    @Test
    fun parse_hotp_readsCounter() {
        val uri = OtpAuthUri.parse("otpauth://hotp/Example:bob?secret=JBSWY3DPEHPK3PXP&counter=42")
        assertEquals(OtpType.HOTP, uri.type)
        assertEquals(42L, uri.counter)
    }

    @Test
    fun parse_issuerFromLabel_whenParameterMissing() {
        val uri = OtpAuthUri.parse("otpauth://totp/GitHub:%20alice?secret=JBSWY3DPEHPK3PXP")
        assertEquals("GitHub", uri.issuer)
        assertEquals("alice", uri.accountName)
    }

    @Test
    fun parse_encodedColonSeparator() {
        val uri = OtpAuthUri.parse("otpauth://totp/GitHub%3Aalice?secret=JBSWY3DPEHPK3PXP")
        assertEquals("GitHub", uri.issuer)
        assertEquals("alice", uri.accountName)
    }

    @Test
    fun parse_issuerParameter_winsOverLabel() {
        val uri = OtpAuthUri.parse("otpauth://totp/Old:alice?secret=JBSWY3DPEHPK3PXP&issuer=New")
        assertEquals("New", uri.issuer)
    }

    @Test
    fun parse_isCaseInsensitiveForSchemeTypeAndAlgorithm() {
        val uri = OtpAuthUri.parse("OTPAUTH://TOTP/alice?secret=jbswy3dpehpk3pxp&algorithm=sha512")
        assertEquals(OtpType.TOTP, uri.type)
        assertEquals(HmacAlgorithm.SHA512, uri.algorithm)
        assertEquals("JBSWY3DPEHPK3PXP", uri.secret)
    }

    @Test
    fun parse_decodesUtf8() {
        val uri = OtpAuthUri.parse("otpauth://totp/Caf%C3%A9:Jos%C3%A9?secret=JBSWY3DPEHPK3PXP")
        assertEquals("Café", uri.issuer)
        assertEquals("José", uri.accountName)
    }

    @Test
    fun parse_trailingEscape_isDecoded() {
        val uri = OtpAuthUri.parse("otpauth://totp/alice%21?secret=JBSWY3DPEHPK3PXP")
        assertEquals("alice!", uri.accountName)
    }

    @Test
    fun parse_invalidInput_throws() {
        listOf(
            "https://example.com",
            "otpauth://totp",
            "otpauth://xotp/alice?secret=JBSWY3DPEHPK3PXP",
            "otpauth://totp/alice",
            "otpauth://totp/alice?issuer=x",
            "otpauth://totp/alice?secret=not-base32!",
            "otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&algorithm=MD5",
            "otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&digits=abc",
            "otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&digits=4",
            "otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&period=0",
            "otpauth://hotp/alice?secret=JBSWY3DPEHPK3PXP&counter=-1"
        ).forEach { input ->
            assertFailsWith<IllegalArgumentException>(input) { OtpAuthUri.parse(input) }
            assertNull(OtpAuthUri.parseOrNull(input), input)
        }
    }

    @Test
    fun toUriString_roundTrips() {
        val original = OtpAuthUri(
            type = OtpType.TOTP,
            secret = "JBSWY3DPEHPK3PXP",
            accountName = "josé+test@example.com",
            issuer = "ACME Co & Sons",
            algorithm = HmacAlgorithm.SHA256,
            digits = 8,
            period = 60
        )
        assertEquals(original, OtpAuthUri.parse(original.toUriString()))

        val hotp = OtpAuthUri(OtpType.HOTP, "JBSWY3DPEHPK3PXP", "bob", counter = 7)
        assertEquals(hotp, OtpAuthUri.parse(hotp.toUriString()))
    }

    @Test
    fun toUriString_format() {
        val uri = OtpAuthUri(OtpType.TOTP, "jbsw y3dp ehpk 3pxp", "alice@example.com", issuer = "ACME Co")
        assertEquals(
            "otpauth://totp/ACME%20Co:alice%40example.com" +
                "?secret=JBSWY3DPEHPK3PXP&issuer=ACME%20Co&algorithm=SHA1&digits=6&period=30",
            uri.toUriString()
        )
    }

    @Test
    fun toTotp_generatesSameCodesAsDirectTotp() {
        val uri = OtpAuthUri.parse("otpauth://totp/alice?secret=${RfcVectors.SECRET_SHA1}&digits=8")
        assertEquals("94287082", uri.toTotp().generate(59_000L))
    }

    @Test
    fun toHotp_generatesSameCodesAsDirectHotp() {
        val uri = OtpAuthUri.parse("otpauth://hotp/alice?secret=${RfcVectors.SECRET_SHA1}")
        assertEquals("755224", uri.toHotp().generate(0))
    }

    @Test
    fun toTotp_onHotpUri_throws() {
        val uri = OtpAuthUri(OtpType.HOTP, "JBSWY3DPEHPK3PXP", "bob")
        assertFailsWith<IllegalStateException> { uri.toTotp() }
    }

    @Test
    fun toString_hidesSecret() {
        val text = OtpAuthUri(OtpType.TOTP, "JBSWY3DPEHPK3PXP", "alice").toString()
        assertFalse("JBSWY3DPEHPK3PXP" in text)
        assertTrue("secret=***" in text)
    }

    @Test
    fun colonInIssuerOrAccount_throws() {
        assertFailsWith<IllegalArgumentException> { OtpAuthUri(OtpType.TOTP, "JBSWY3DPEHPK3PXP", "a:b") }
        assertFailsWith<IllegalArgumentException> {
            OtpAuthUri(OtpType.TOTP, "JBSWY3DPEHPK3PXP", "alice", issuer = "ACME: Inc")
        }
        // A label with more than one separator cannot be split unambiguously.
        assertNull(OtpAuthUri.parseOrNull("otpauth://totp/ACME:alice:work?secret=JBSWY3DPEHPK3PXP"))
    }
}
