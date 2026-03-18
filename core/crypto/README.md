# core/crypto

Platform-independent TOTP/HOTP library for Kotlin Multiplatform, implementing
[RFC 6238](https://tools.ietf.org/html/rfc6238) (TOTP) and
[RFC 4226](https://tools.ietf.org/html/rfc4226) (HOTP).

## Features

- **TOTP & HOTP** generation with configurable digits and period
- **SHA-1, SHA-256, SHA-512** HMAC algorithms
- **Base32** decoding ([RFC 4648](https://tools.ietf.org/html/rfc4648))
- **Secure by default** — byte arrays are zeroed after use
- **Fully testable** — HMAC is injected via `HmacProvider` interface

## Architecture

```
commonMain/
├── Base32.kt          — RFC 4648 Base32 decoder
├── HmacProvider.kt    — HmacAlgorithm enum + HmacProvider interface
└── TotpGenerator.kt   — TOTP/HOTP code generation + remaining seconds

androidMain/
└── JvmHmacProvider.kt — javax.crypto.Mac implementation

iosMain/
└── IosHmacProvider.kt — CoreCrypto CCHmac implementation
```

## Usage

```kotlin
// Setup (typically via DI)
val hmacProvider: HmacProvider = JvmHmacProvider() // or IosHmacProvider
val generator = TotpGenerator(hmacProvider)

// Generate TOTP (SHA-1, 6 digits, 30s period — defaults)
val code = generator.generate(
    secret = "JBSWY3DPEHPK3PXP",
    timestampMillis = System.currentTimeMillis()
)

// Generate TOTP with SHA-256
val code256 = generator.generate(
    secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZA",
    timestampMillis = System.currentTimeMillis(),
    algorithm = HmacAlgorithm.SHA256
)

// Generate HOTP
val hotpCode = generator.generateHotp(
    secret = "JBSWY3DPEHPK3PXP",
    counter = 42
)

// Remaining seconds in current period
val remaining = generator.remainingSeconds(System.currentTimeMillis())

// Base32 validation & decoding
if (Base32.isValid(userInput)) {
    val bytes = Base32.decode(userInput)
}
```

## Supported Algorithms

| Algorithm | HMAC Output | RFC Reference |
|-----------|-------------|---------------|
| SHA-1     | 20 bytes    | RFC 6238 §B.1 |
| SHA-256   | 32 bytes    | RFC 6238 §B.2 |
| SHA-512   | 64 bytes    | RFC 6238 §B.3 |

## Testing

Tests use fake `HmacProvider` implementations with pre-computed HMAC values
from the RFC 6238 test vectors. Run per-file:

```bash
# Base32 tests
./gradlew :core:crypto:testDebugUnitTest --tests "*.Base32Test"

# TOTP/HOTP tests (SHA-1, SHA-256, SHA-512)
./gradlew :core:crypto:testDebugUnitTest --tests "*.TotpGeneratorTest"
```

## Security Notes

- Decoded secret keys, counter bytes, and HMAC outputs are cleared (`fill(0)`)
  in `finally` blocks after each code generation.
- The `HmacProvider` interface lets you swap in hardware-backed implementations
  (e.g., Android Keystore) without changing the generator.
- `Base32.decode()` validates input before decoding and throws
  `IllegalArgumentException` on invalid characters or empty input.
