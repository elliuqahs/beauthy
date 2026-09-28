# Module beauthy-sdk

TOTP ([RFC 6238](https://tools.ietf.org/html/rfc6238)) and HOTP ([RFC 4226](https://tools.ietf.org/html/rfc4226))
one-time passwords for Kotlin Multiplatform, with no dependencies beyond the Kotlin standard library.

Start with [io.github.elliuqahs.beauthy.Totp]. Use [io.github.elliuqahs.beauthy.OtpAuthUri] to read
authenticator QR codes and [io.github.elliuqahs.beauthy.Secret] to create secrets for new accounts.

# Package io.github.elliuqahs.beauthy

The Beauthy SDK API: code generation, verification, `otpauth://` URIs, secrets and Base32.

# Package com.maoungedev.beauthy.core.crypto

Deprecated 0.1.x API, kept for migration. Use `io.github.elliuqahs.beauthy` instead.
