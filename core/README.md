<h1 align="center">Beauthy OTP SDK</h1>

<p align="center">
  Lightweight Kotlin Multiplatform library for generating TOTP & HOTP one-time passwords.
</p>

<p align="center">
  <!-- <a href="https://search.maven.org/artifact/com.beauthy/otp-sdk"><img src="https://img.shields.io/maven-central/v/com.beauthy/otp-sdk.svg?style=flat&label=Maven%20Central" alt="Maven Central"/></a> -->
  <a href="https://kotlinlang.org/docs/multiplatform.html"><img src="https://img.shields.io/badge/Kotlin_Multiplatform-2.3.0-7F52FF.svg?style=flat&logo=kotlin&logoColor=white" alt="KMP"/></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-24+-34A853.svg?style=flat&logo=android&logoColor=white" alt="Android"/></a>
  <a href="https://developer.apple.com/ios/"><img src="https://img.shields.io/badge/iOS-16+-000000.svg?style=flat&logo=apple&logoColor=white" alt="iOS"/></a>
  <a href="../LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat" alt="License"/></a>
</p>

---

RFC-compliant implementation of [TOTP (RFC 6238)](https://tools.ietf.org/html/rfc6238) and [HOTP (RFC 4226)](https://tools.ietf.org/html/rfc4226) with support for SHA-1, SHA-256, and SHA-512. Zero third-party dependencies — uses only platform-native cryptography.

## Features

- **TOTP** — Time-based one-time passwords with configurable digits and period
- **HOTP** — Counter-based one-time passwords
- **Multi-Algorithm** — SHA-1, SHA-256, SHA-512 HMAC
- **Base32** — RFC 4648 decoder with validation
- **Memory Safe** — Secret keys and HMAC outputs zeroed after use
- **Testable** — HMAC injected via interface, easy to fake in tests
- **Explicit API** — All public declarations have explicit visibility modifiers
- **Binary Compatible** — API tracked with kotlinx.binary-compatibility-validator

## Installation

<details open>
<summary><b>Gradle (Version Catalog)</b></summary>

```toml
# gradle/libs.versions.toml
[versions]
beauthy-otp = "1.0.0"

[libraries]
beauthy-otp-sdk = { module = "com.beauthy:otp-sdk", version.ref = "beauthy-otp" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.beauthy.otp.sdk)
}
```

</details>

<details>
<summary><b>Gradle (Direct)</b></summary>

```kotlin
dependencies {
    implementation("com.beauthy:otp-sdk:1.0.0")
}
```

</details>

## Quick Start

### Setup

```kotlin
import com.maoungedev.beauthy.core.crypto.*

// Android / JVM
val generator = TotpGenerator(JvmHmacProvider())

// iOS
val generator = TotpGenerator(IosHmacProvider())
```

### Generate TOTP (Default — SHA-1, 6 digits, 30s)

```kotlin
val code = generator.generate(
    secret = "JBSWY3DPEHPK3PXP",
    timestampMillis = System.currentTimeMillis()
)
// code = "287082"

val remaining = generator.remainingSeconds(System.currentTimeMillis())
// remaining = 15  (seconds until next code)
```

### Generate TOTP with SHA-256

```kotlin
val code = generator.generate(
    secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZA",
    timestampMillis = System.currentTimeMillis(),
    algorithm = HmacAlgorithm.SHA256,
    digits = 8,
    period = 60
)
```

### Generate HOTP (Counter-Based)

```kotlin
val code = generator.generateHotp(
    secret = "JBSWY3DPEHPK3PXP",
    counter = 42
)

// Increment counter after each use
val next = generator.generateHotp(secret = "JBSWY3DPEHPK3PXP", counter = 43)
```

### Validate Secret Key

```kotlin
val userInput = "JBSW Y3DP EHPK 3PXP"

if (Base32.isValid(userInput)) {
    val decoded = Base32.decode(userInput) // ByteArray
    println("Valid! ${decoded.size} bytes")
} else {
    println("Invalid Base32 input")
}
```

## API Reference

### `TotpGenerator`

| Method | Description |
|--------|-------------|
| `generate(secret, timestampMillis, digits, period, algorithm)` | Generate a TOTP code. Defaults: 6 digits, 30s period, SHA-1 |
| `generateHotp(secret, counter, digits, algorithm)` | Generate an HOTP code. Defaults: 6 digits, SHA-1 |
| `remainingSeconds(timestampMillis, period)` | Seconds remaining in current TOTP period (1..period) |

### `Base32`

| Method | Description |
|--------|-------------|
| `isValid(input)` | Check if string contains only valid Base32 characters |
| `decode(input)` | Decode Base32 string to `ByteArray`. Throws `IllegalArgumentException` on invalid input |

### `HmacProvider`

| Implementation | Platform | Backend |
|----------------|----------|---------|
| `JvmHmacProvider` | Android / JVM | `javax.crypto.Mac` |
| `IosHmacProvider` | iOS | CoreCrypto `CCHmac` |

### `HmacAlgorithm`

| Value | HMAC Output Size | RFC Reference |
|-------|-----------------|---------------|
| `SHA1` | 20 bytes | RFC 6238 Appendix B.1 |
| `SHA256` | 32 bytes | RFC 6238 Appendix B.2 |
| `SHA512` | 64 bytes | RFC 6238 Appendix B.3 |

## Supported Platforms

| Platform | Target | HMAC Backend |
|----------|--------|-------------|
| Android | `androidTarget` (minSdk 24) | `javax.crypto.Mac` |
| iOS | `iosArm64`, `iosSimulatorArm64` | CoreCrypto `CCHmac` |

## Security Considerations

- **Memory safety** — Decoded keys, counter bytes, and HMAC outputs are zeroed (`ByteArray.fill(0)`) in `finally` blocks after every operation
- **Input validation** — `Base32.decode()` validates input before decoding and throws on invalid characters or empty input
- **Pluggable crypto** — `HmacProvider` interface allows hardware-backed implementations (e.g., Android Keystore) without changing generator code
- **No secrets in logs** — The SDK does not log or expose secret keys

## Testing

Tests use pre-computed HMAC values from the RFC 6238 test vectors — no real crypto needed in tests.

```bash
# Run all tests (Android + iOS Simulator)
./gradlew :core:allTests

# Run specific test class
./gradlew :core:testDebugUnitTest --tests "*.Base32Test"
./gradlew :core:testDebugUnitTest --tests "*.TotpGeneratorTest"

# Verify API compatibility
./gradlew apiCheck
```

| Test Suite | Tests | Description |
|------------|-------|-------------|
| `Base32Test` | 9 | RFC 4648 vectors, validation, padding, case insensitivity |
| `TotpGeneratorTest` | 15 | RFC 6238 vectors for SHA-1, SHA-256, SHA-512, remaining seconds |

## Samples

See the [`samples/`](samples/) directory for runnable examples:

- [`BasicTotpSample.kt`](samples/BasicTotpSample.kt) — TOTP with default settings
- [`CustomAlgorithmSample.kt`](samples/CustomAlgorithmSample.kt) — TOTP with SHA-256, 8 digits, 60s period
- [`HotpSample.kt`](samples/HotpSample.kt) — Counter-based HOTP generation
- [`ValidationSample.kt`](samples/ValidationSample.kt) — Base32 secret key validation

## Architecture

```
commonMain/
├── Base32.kt          — RFC 4648 Base32 decoder & validator
├── HmacProvider.kt    — HmacAlgorithm enum + HmacProvider interface
└── TotpGenerator.kt   — TOTP/HOTP generation + remaining seconds

androidMain/
└── JvmHmacProvider.kt — javax.crypto.Mac implementation

iosMain/
└── IosHmacProvider.kt — CoreCrypto CCHmac implementation
```

## Roadmap

- [ ] Publish to Maven Central
- [ ] Add JVM (non-Android) target
- [ ] Add macOS / watchOS targets
- [ ] Steam Guard TOTP support
- [ ] TOTP with custom epoch

## License

```
Copyright 2025 Beauthy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
