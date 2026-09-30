<img width="1088" height="316" alt="image" src="https://github.com/user-attachments/assets/d691d67e-ab19-4624-abcf-1e1709d61a5d" />



<h1 align="center">Beauthy SDK</h1>

<p align="center">A lightweight Kotlin Multiplatform library for generating TOTP & HOTP one-time passwords.</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.elliuqahs/beauthy-sdk"><img src="https://img.shields.io/maven-central/v/io.github.elliuqahs/beauthy-sdk.svg?style=flat&label=Maven%20Central&color=blue" alt="Maven Central"/></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-7F52FF.svg?style=flat&logo=kotlin&logoColor=white"/></a>
  <a href="https://kotlinlang.org/docs/multiplatform.html"><img src="https://img.shields.io/badge/Kotlin_Multiplatform-orange.svg?style=flat&logo=kotlin&logoColor=white"/></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-34A853.svg?style=flat&logo=android&logoColor=white"/></a>
  <a href="https://developer.apple.com/ios/"><img src="https://img.shields.io/badge/iOS-000000.svg?style=flat&logo=apple&logoColor=white"/></a>
  <a href="https://kotlinlang.org/docs/jvm-get-started.html"><img src="https://img.shields.io/badge/JVM-ED8B00.svg?style=flat&logo=openjdk&logoColor=white"/></a>
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat"/>
</p>

---

RFC-compliant [TOTP (RFC 6238)](https://tools.ietf.org/html/rfc6238) and [HOTP (RFC 4226)](https://tools.ietf.org/html/rfc4226) for Kotlin Multiplatform. Use it from common code on every platform, whether you are building an authenticator app or adding two-factor login to a server. Zero third-party dependencies: it uses each platform's native cryptography.

## Apps Demo
- **Play Store**: https://play.google.com/store/apps/details?id=com.elliuqahs.kunciku&pcampaignid=web_share

## Features

- **TOTP and HOTP** with SHA-1, SHA-256 and SHA-512, 6 to 9 digits, and any period
- **Ready for display**: the code, a formatted version, the countdown and progress from one call
- **Clock correction** for devices whose clock is wrong
- **Live updates**: a `Flow` (`beauthy-sdk-coroutines`) or Compose state (`beauthy-sdk-compose`) that ticks every second
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
            // Optional: live codes as Compose state (includes beauthy-sdk-coroutines)
            implementation("io.github.elliuqahs:beauthy-sdk-compose:0.2.0")
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

A code is valid for one period (30 seconds by default). `current()` returns the code with its countdown and progress, all computed from the same instant. The optional artifacts keep it up to date for you, ticking at the start of every second so the countdown follows the clock and the code changes as soon as a new period begins.

**Compose Multiplatform** (`beauthy-sdk-compose`):

```kotlin
@Composable
fun AccountRow(secret: String) {
    val current by rememberTotpCode(secret)
    Text(current.formatted())                                  // "861 370"
    Text("${current.remainingSeconds}s")                        // "15s"
    LinearProgressIndicator(progress = { current.progress })
}
```

`rememberTotpCode(secret, algorithm, digits, period)` throws for an invalid secret, so validate user input with `Base32.isValid` first, or pass a `Totp` you created yourself: `rememberTotpCode(totp)`.

**Coroutines** (`beauthy-sdk-coroutines`), for example in a ViewModel:

```kotlin
class AccountViewModel(secret: String) : ViewModel() {
    private val totp = Totp(secret)

    val code: StateFlow<TotpCode> = totp.codes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), totp.current())
}
```

**Neither**: call `current()` once a second yourself, or schedule the next refresh for `current.expiresAtMillis` if you only need to know when the code changes.

### When the device clock is wrong

Codes and countdowns come only from the clock, so the server and the app agree as long as both clocks are right. A phone whose clock is off by more than about 30 seconds produces codes the server rejects. Measure the error once against a trusted time, such as the `Date` header of any HTTPS response, and pass it in:

```kotlin
val offset = ClockOffset.from(serverTimeMillis)   // positive when the device is behind

val totp = Totp(secret, clockOffsetMillis = offset)
val totpFromQr = OtpAuthUri.parse(qrText).toTotp(clockOffsetMillis = offset)
val current by rememberTotpCode(secret, clockOffsetMillis = offset)   // Compose
```

The offset applies wherever the current time is used (`current()`, `generate()`, `verify()`, `codes()`); timestamps you pass explicitly are used as given.

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

The full API reference is on javadoc.io for [beauthy-sdk](https://javadoc.io/doc/io.github.elliuqahs/beauthy-sdk), [beauthy-sdk-coroutines](https://javadoc.io/doc/io.github.elliuqahs/beauthy-sdk-coroutines) and [beauthy-sdk-compose](https://javadoc.io/doc/io.github.elliuqahs/beauthy-sdk-compose).

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

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for what changed in each version.

## Find this library useful?

Support it by joining __[stargazers](https://github.com/elliuqahs/beauthy/stargazers)__ for this repository. :star:

## License

```
Copyright 2025 elliuqahs

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
