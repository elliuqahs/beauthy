<img width="1088" height="316" alt="image" src="https://github.com/user-attachments/assets/d691d67e-ab19-4624-abcf-1e1709d61a5d" />



<h1 align="center">Beauthy SDK</h1>

<p align="center">A lightweight Kotlin Multiplatform library for generating TOTP & HOTP one-time passwords.</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.elliuqahs/beauthy-sdk"><img src="https://img.shields.io/maven-central/v/io.github.elliuqahs/beauthy-sdk.svg?style=flat&label=Maven%20Central&color=blue" alt="Maven Central"/></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-7F52FF.svg?style=flat&logo=kotlin&logoColor=white"/></a>
  <a href="https://kotlinlang.org/docs/multiplatform.html"><img src="https://img.shields.io/badge/Kotlin_Multiplatform-orange.svg?style=flat&logo=kotlin&logoColor=white"/></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-34A853.svg?style=flat&logo=android&logoColor=white"/></a>
  <a href="https://developer.apple.com/ios/"><img src="https://img.shields.io/badge/iOS-000000.svg?style=flat&logo=apple&logoColor=white"/></a>
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat"/>
</p>

---

RFC-compliant [TOTP (RFC 6238)](https://tools.ietf.org/html/rfc6238) and [HOTP (RFC 4226)](https://tools.ietf.org/html/rfc4226) for Kotlin Multiplatform. Use it from common code on every platform, whether you are building an authenticator app or adding two-factor login to a server. Zero third-party dependencies: it uses each platform's native cryptography.

## Features

- **TOTP and HOTP** with SHA-1, SHA-256 and SHA-512, 6 to 9 digits, and any period
- **Ready for display**: the code, a formatted version, the countdown and progress from one call
- **Live updates** (optional `beauthy-sdk-coroutines`): a `Flow` that ticks every second
- **Verification** with a clock-drift window (TOTP) or look-ahead (HOTP), using constant-time comparison
- **`otpauth://` URIs**: parse authenticator QR codes and build them for enrollment
- **Secret generation** from the platform's secure random source
- **Base32** encoding and decoding
- **Common API**: no platform-specific setup, everything works from `commonMain`
- **No dependencies** in `beauthy-sdk` beyond the Kotlin standard library
- Tested against the RFC 4226 and RFC 6238 test vectors on Android, JVM and iOS

## Download

<details open>
<summary><b>Kotlin Multiplatform</b></summary>

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.elliuqahs:beauthy-sdk:0.2.0")

            // Optional: live codes as a Flow
            implementation("io.github.elliuqahs:beauthy-sdk-coroutines:0.2.0")
        }
    }
}
```

</details>

<details>
<summary><b>Android or JVM only (Gradle picks the right variant)</b></summary>

```kotlin
dependencies {
    implementation("io.github.elliuqahs:beauthy-sdk:0.2.0")
}
```

</details>

## Usage

All examples below are common code and run unchanged on every supported platform.

### Generate codes

```kotlin
import io.github.elliuqahs.beauthy.*

// TOTP (defaults: SHA-1, 6 digits, 30 seconds)
val totp = Totp(secret = "JBSWY3DPEHPK3PXP")
val current = totp.current()
current.code              // "861370"
current.formatted()       // "861 370"
current.remainingSeconds  // 15
current.progress          // 0.5

// Custom parameters
val totp256 = Totp(
    secret = "JBSWY3DPEHPK3PXP",
    algorithm = HmacAlgorithm.SHA256,
    digits = 8,
    period = 60
)

// HOTP
val hotp = Hotp(secret = "JBSWY3DPEHPK3PXP")
val hotpCode = hotp.generate(counter = 42)
```

`totp.generate()` returns just the code string. `totp.at(timestampMillis)` and the `timestampMillis` parameters let you use your own clock, for example in tests.

### Display a live code

A code is valid for one period (30 seconds by default). `current()` returns the code with its countdown and progress, all computed from the same instant.

With `beauthy-sdk-coroutines`, `codes()` turns that into a `Flow` that emits at the start of every second, so the countdown ticks with the clock and the code changes as soon as a new period begins:

```kotlin
class AccountViewModel(secret: String) : ViewModel() {
    private val totp = Totp(secret)

    val code: StateFlow<TotpCode> = totp.codes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), totp.current())
}

@Composable
fun AccountRow(viewModel: AccountViewModel) {
    val current by viewModel.code.collectAsState()
    Text(current.formatted())
    Text("${current.remainingSeconds}s")
    LinearProgressIndicator(progress = { current.progress })
}
```

Without coroutines, call `current()` once a second yourself, or schedule the next refresh for `current.expiresAtMillis` if you only need to know when the code changes.

### Scan a QR code

```kotlin
val uri = OtpAuthUri.parseOrNull(scannedText) ?: return  // not an otpauth:// QR code

println("${uri.issuer} - ${uri.accountName}")
val code = when (uri.type) {
    OtpType.TOTP -> uri.toTotp().generate()
    OtpType.HOTP -> uri.toHotp().generate(uri.counter)
}
```

### Server-side two-factor login

```kotlin
// Enrollment: store the secret, show the URI as a QR code
val secret = Secret.generate()
val qrContent = OtpAuthUri(
    type = OtpType.TOTP,
    secret = secret,
    accountName = "alice@example.com",
    issuer = "Example"
).toUriString()

// Login: accept the current code or one period either side
val valid = Totp(secret).verify(userInput)
```

A TOTP code stays valid for its whole window, so remember which time step each user last logged in with and reject reuse. `Hotp.verify` returns the matched counter; store it plus one as the next expected counter.

### Validate user input

```kotlin
if (Base32.isValid(userInput)) {
    val totp = Totp(userInput)
}
```

Constructors throw `IllegalArgumentException` for an invalid secret, `digits` outside 6..9, or a non-positive `period`.

More examples are in [`samples`](samples/src/main/kotlin/io/github/elliuqahs/beauthy/samples). Run them all with `./gradlew :samples:run`.

## Supported Platforms

| Platform | Targets | HMAC backend |
|----------|---------|--------------|
| Android | minSdk 24 | `javax.crypto.Mac` |
| JVM | Java 11+ | `javax.crypto.Mac` |
| iOS | arm64, simulatorArm64, x64 | CommonCrypto `CCHmac` |

## Migrating from 0.1.x

0.2.0 moves the API to the `io.github.elliuqahs.beauthy` package and removes the need for a platform `HmacProvider`. The old `com.maoungedev.beauthy.core.crypto` API still works but is deprecated and will be removed in a future release.

| 0.1.x | 0.2.0 |
|-------|-------|
| `TotpGenerator(JvmHmacProvider())` / `TotpGenerator(IosHmacProvider())` | not needed |
| `generator.generate(secret, now, digits, period, algorithm)` | `Totp(secret, algorithm, digits, period).generate(now)` |
| `generator.generateHotp(secret, counter, digits, algorithm)` | `Hotp(secret, algorithm, digits).generate(counter)` |
| `generator.remainingSeconds(now, period)` | `Totp(secret, period = period).remainingSeconds(now)` |
| `com.maoungedev.beauthy.core.crypto.Base32` | `io.github.elliuqahs.beauthy.Base32` |
| `com.maoungedev.beauthy.core.crypto.HmacAlgorithm` | `io.github.elliuqahs.beauthy.HmacAlgorithm` |

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
