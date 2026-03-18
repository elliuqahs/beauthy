<img width="1086" height="360" alt="image" src="https://github.com/user-attachments/assets/b7f3e278-f5f7-45f8-9748-32306ce8e04e" />


<h1 align="center">Beauthy OTP SDK</h1>

<p align="center">A lightweight Kotlin Multiplatform library for generating TOTP & HOTP one-time passwords.</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF.svg?style=flat&logo=kotlin&logoColor=white"/></a>
  <a href="https://kotlinlang.org/docs/multiplatform.html"><img src="https://img.shields.io/badge/Kotlin_Multiplatform-orange.svg?style=flat&logo=kotlin&logoColor=white"/></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-24+-34A853.svg?style=flat&logo=android&logoColor=white"/></a>
  <a href="https://developer.apple.com/ios/"><img src="https://img.shields.io/badge/iOS-16+-000000.svg?style=flat&logo=apple&logoColor=white"/></a>
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat"/>
</p>

---

RFC-compliant [TOTP (RFC 6238)](https://tools.ietf.org/html/rfc6238) and [HOTP (RFC 4226)](https://tools.ietf.org/html/rfc4226) implementation supporting SHA-1, SHA-256, and SHA-512. Zero third-party dependencies — uses only platform-native cryptography.

## Download

```gradle
implementation("com.beauthy:otp-sdk:0.1.0")
```

## Usage

```kotlin
val generator = TotpGenerator(JvmHmacProvider()) // or IosHmacProvider()
```

### TOTP

```kotlin
val code = generator.generate(secret = "JBSWY3DPEHPK3PXP", timestampMillis = System.currentTimeMillis())
val remaining = generator.remainingSeconds(System.currentTimeMillis()) // seconds until next code
```

### TOTP with SHA-256

```kotlin
val code = generator.generate(
    secret = "JBSWY3DPEHPK3PXP",
    timestampMillis = System.currentTimeMillis(),
    algorithm = HmacAlgorithm.SHA256
)
```

### HOTP

```kotlin
val code = generator.generateHotp(secret = "JBSWY3DPEHPK3PXP", counter = 42)
```

### Validate Base32

```kotlin
if (Base32.isValid(userInput)) {
    val bytes = Base32.decode(userInput)
}
```

## Supported Platforms

| Platform | HMAC Backend |
|----------|-------------|
| Android (minSdk 24) | `javax.crypto.Mac` |
| iOS (arm64, simulatorArm64) | CoreCrypto `CCHmac` |

## Find this library useful?

Support it by joining __[stargazers](https://github.com/elliuqahs/beauthy/stargazers)__ for this repository. :star:

## License

```
Copyright 2025 Beauthy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
