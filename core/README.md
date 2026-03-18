<h1 align="center">Beauthy OTP SDK</h1>

<p align="center">Kotlin Multiplatform TOTP/HOTP library.</p>

<p align="center">
  <a href="https://kotlinlang.org/docs/multiplatform.html"><img src="https://img.shields.io/badge/Kotlin_Multiplatform-2.3.0-7F52FF.svg?style=flat&logo=kotlin&logoColor=white"/></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-24+-34A853.svg?style=flat&logo=android&logoColor=white"/></a>
  <a href="https://developer.apple.com/ios/"><img src="https://img.shields.io/badge/iOS-16+-000000.svg?style=flat&logo=apple&logoColor=white"/></a>
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat"/>
</p>

---

RFC-compliant [TOTP (RFC 6238)](https://tools.ietf.org/html/rfc6238) and [HOTP (RFC 4226)](https://tools.ietf.org/html/rfc4226) implementation with SHA-1, SHA-256, and SHA-512 support. Zero third-party dependencies — platform-native crypto only.

## Download

```kotlin
implementation("com.beauthy:otp-sdk:1.0.0")
```

## Usage

```kotlin
val generator = TotpGenerator(JvmHmacProvider()) // or IosHmacProvider()
```

**TOTP (SHA-1, 6 digits, 30s)**
```kotlin
val code = generator.generate(secret = "JBSWY3DPEHPK3PXP", timestampMillis = System.currentTimeMillis())
val remaining = generator.remainingSeconds(System.currentTimeMillis())
```

**TOTP (SHA-256)**
```kotlin
val code = generator.generate(
    secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZA",
    timestampMillis = System.currentTimeMillis(),
    algorithm = HmacAlgorithm.SHA256
)
```

**HOTP**
```kotlin
val code = generator.generateHotp(secret = "JBSWY3DPEHPK3PXP", counter = 42)
```

**Validate Base32**
```kotlin
if (Base32.isValid(userInput)) {
    val bytes = Base32.decode(userInput)
}
```

## API

| Class | Method | Description |
|-------|--------|-------------|
| `TotpGenerator` | `generate(secret, timestampMillis, digits, period, algorithm)` | Generate TOTP code |
| `TotpGenerator` | `generateHotp(secret, counter, digits, algorithm)` | Generate HOTP code |
| `TotpGenerator` | `remainingSeconds(timestampMillis, period)` | Seconds left in current period |
| `Base32` | `isValid(input)` | Validate Base32 string |
| `Base32` | `decode(input)` | Decode Base32 to ByteArray |

## Supported Platforms

| Platform | Target | HMAC Backend |
|----------|--------|-------------|
| Android | `androidTarget` (minSdk 24) | `javax.crypto.Mac` |
| iOS | `iosArm64`, `iosSimulatorArm64` | CoreCrypto `CCHmac` |

## License

```
Copyright 2025 Beauthy

Licensed under the Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0
```
