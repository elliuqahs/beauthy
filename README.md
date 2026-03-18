<p align="center">
  <img src="composeApp/src/commonMain/composeResources/drawable/logo.png" alt="Beauthy" width="100"/>
</p>

<h1 align="center">Beauthy</h1>

<p align="center">
  <b>Be Authentic. Be Secure. Be Everywhere.</b><br/>
  A modern two-factor authenticator built with Kotlin Multiplatform & Compose Multiplatform.
</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF.svg?style=flat&logo=kotlin&logoColor=white" alt="Kotlin"/></a>
  <a href="https://www.jetbrains.com/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose_Multiplatform-1.10.0-4285F4.svg?style=flat&logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform"/></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-24+-34A853.svg?style=flat&logo=android&logoColor=white" alt="Android"/></a>
  <a href="https://developer.apple.com/ios/"><img src="https://img.shields.io/badge/iOS-16+-000000.svg?style=flat&logo=apple&logoColor=white" alt="iOS"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat" alt="License"/></a>
</p>

<!-- screenshots here: replace with actual app screenshots
<p align="center">
  <img src="docs/screenshots/android_list.png" width="250" alt="Android List"/>
  &nbsp;&nbsp;
  <img src="docs/screenshots/android_add.png" width="250" alt="Android Add"/>
  &nbsp;&nbsp;
  <img src="docs/screenshots/ios_list.png" width="250" alt="iOS List"/>
</p>
-->

---

Beauthy is a cross-platform two-factor authentication app that generates **TOTP** ([RFC 6238](https://tools.ietf.org/html/rfc6238)) and **HOTP** ([RFC 4226](https://tools.ietf.org/html/rfc4226)) one-time passwords. Built entirely with **Kotlin Multiplatform** and **Compose Multiplatform** — single codebase, native performance on Android & iOS.

The cryptographic core (`core/crypto`) is a **standalone SDK** that can be used independently in any Kotlin project. See [Beauthy OTP SDK](#beauthy-otp-sdk) for details.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [How TOTP Works](#how-totp-works)
- [Security](#security)
- [Beauthy OTP SDK](#beauthy-otp-sdk)
- [Build & Run](#build--run)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Features

| Feature | Description |
|---------|-------------|
| **TOTP Generation** | Time-based codes that refresh every 30 seconds |
| **HOTP Generation** | Counter-based codes with manual refresh |
| **QR Code Scanning** | Scan `otpauth://` URIs via camera (CameraX / AVFoundation) |
| **Manual Entry** | Add accounts with Base32 secret validation |
| **Encrypted Storage** | AES-256-GCM (Android) / NSFileProtectionComplete (iOS) |
| **Biometric Lock** | Fingerprint / Face ID authentication on app launch |
| **Copy to Clipboard** | Tap to copy OTP code, auto-clear after 30 seconds |
| **Search & Filter** | Find accounts by issuer or account name |
| **Sort** | Sort accounts by issuer (A-Z, Z-A) |
| **Dark Theme** | Material3 dark theme support |
| **Multi-Algorithm** | SHA-1, SHA-256, SHA-512 HMAC support |

## Tech Stack

| Category | Library | Version |
|----------|---------|---------|
| Language | Kotlin Multiplatform | 2.3.0 |
| UI | Compose Multiplatform + Material3 | 1.10.0 |
| DI | Koin | 4.0.4 |
| Navigation | Voyager | 1.1.0-beta03 |
| Serialization | kotlinx-serialization-json | 1.8.0 |
| Async | kotlinx-coroutines | 1.9.0 |
| QR Scan (Android) | CameraX + ML Kit Barcode | 1.4.2 / 17.3.0 |
| QR Scan (iOS) | AVFoundation | System |
| Secure Storage (Android) | EncryptedSharedPreferences | 1.1.0-alpha06 |
| Secure Storage (iOS) | NSFileProtectionComplete | System |
| Biometric (Android) | androidx.biometric | 1.2.0-alpha05 |
| Biometric (iOS) | LocalAuthentication | System |

## Architecture

**Clean Architecture + MVI + SOLID** — business logic is fully shared, platform code injected via Koin DI.

### Module Structure

```
Beauthy/
├── core/crypto/                  # Standalone OTP SDK (publishable)
│   └── src/
│       ├── commonMain/           Base32, TotpGenerator, HmacProvider
│       ├── androidMain/          JvmHmacProvider (javax.crypto)
│       ├── iosMain/              IosHmacProvider (CoreCrypto)
│       └── commonTest/           RFC 4648 & RFC 6238 test vectors
│
├── composeApp/                   # Main application module
│   └── src/
│       ├── commonMain/
│       │   ├── core/             TimeProvider, OtpAuthParser, ClipboardService
│       │   ├── domain/           OtpAccount, AccountRepository
│       │   ├── data/             AccountDto, AccountStorage, RepositoryImpl
│       │   ├── presentation/
│       │   │   ├── splash/       SplashScreen
│       │   │   ├── biometric/    BiometricLockScreen
│       │   │   ├── list/         AccountListScreen + MVI ScreenModel
│       │   │   ├── add/          AddAccountScreen + MVI ScreenModel
│       │   │   └── scan/         QR Scanner (expect/actual)
│       │   └── di/               Koin modules
│       ├── androidMain/          Platform implementations
│       ├── iosMain/              Platform implementations
│       └── commonTest/           Unit tests
│
├── iosApp/                       # Xcode project entry point
└── gradle/libs.versions.toml    # Version catalog
```

### Layer Diagram

```
┌─────────────────────────────────────────┐
│  Presentation (MVI)                     │
│  Voyager Screens + ScreenModels         │
│  Intent → ScreenModel → State/Effect   │
├─────────────────────────────────────────┤
│  Domain                                 │
│  OtpAccount, AccountRepository          │
├─────────────────────────────────────────┤
│  Data                                   │
│  RepositoryImpl, AccountDto, Storage    │
├──────────────────┬──────────────────────┤
│  Android         │  iOS                 │
│  EncryptedSP     │  NSFileProtection    │
│  CameraX/MLKit   │  AVFoundation        │
│  BiometricPrompt │  LAContext           │
└──────────────────┴──────────────────────┘
```

### Navigation Flow

```
SplashScreen → BiometricLockScreen → AccountListScreen ─┬─→ AddAccountScreen
                                                        └─→ ScanBarcodeScreen
```

## How TOTP Works

```
Secret (Base32)  +  Current Time
       │                  │
       ▼                  ▼
  Base32.decode()    timestamp / 30 = counter
       │                  │
       └───────┬──────────┘
               ▼
    HMAC-SHA(key, counter)        ← SHA-1, SHA-256, or SHA-512
               │
               ▼
    Dynamic Truncation (RFC 4226)
               │
               ▼
         6-digit OTP              ← refreshes every 30 seconds
```

## Security

| Layer | Android | iOS |
|-------|---------|-----|
| **Storage** | EncryptedSharedPreferences (AES-256-GCM) | File + NSFileProtectionComplete |
| **Authentication** | BiometricPrompt (fingerprint/face) | LAContext (Face ID / Touch ID) |
| **Crypto** | javax.crypto.Mac | CoreCrypto CCHmac |
| **Memory** | ByteArray.fill(0) in finally blocks | Same |
| **Validation** | Base32 check + min 16 char secret | Same |
| **Display** | OtpAccount.toString() masks secret | Same |
| **Clipboard** | Auto-clear after 30 seconds | Same |

## Beauthy OTP SDK

The `core/crypto` module is a **standalone Kotlin Multiplatform library** for generating TOTP/HOTP codes. It has zero dependencies on the app module and can be used in any Kotlin project.

```kotlin
implementation("com.beauthy:otp-sdk:1.0.0")
```

```kotlin
val generator = TotpGenerator(JvmHmacProvider())
val code = generator.generate(
    secret = "JBSWY3DPEHPK3PXP",
    timestampMillis = System.currentTimeMillis()
)
```

For full documentation, API reference, and more examples, see the **[OTP SDK README](core/crypto/README.md)**.

## Build & Run

```bash
# Android — build APK
./gradlew composeApp:assembleDebug

# Android — run on connected device/emulator
./gradlew composeApp:installDebug

# iOS — compile check
./gradlew composeApp:compileKotlinIosSimulatorArm64

# iOS — run via Xcode
# Open iosApp/iosApp.xcodeproj → Select target → Run
```

## Testing

All business logic tests are in `commonTest` — no emulator or device required.

```bash
# Run all tests
./gradlew :core:crypto:allTests
./gradlew :composeApp:testDebugUnitTest

# Run specific test class
./gradlew :core:crypto:testDebugUnitTest --tests "*.TotpGeneratorTest"
./gradlew :composeApp:testDebugUnitTest --tests "*.AccountListScreenModelTest"
```

| Test Suite | Tests | Coverage |
|------------|-------|----------|
| Base32Test | 9 | RFC 4648 vectors, validation, edge cases |
| TotpGeneratorTest | 15 | RFC 6238 vectors (SHA-1, SHA-256, SHA-512) |
| AccountRepositoryImplTest | 7 | CRUD, corrupted data, counter increment |
| AccountListScreenModelTest | 10 | State, timer, delete, copy, search, sort |
| AddAccountScreenModelTest | 8 | Validation, submit, consume event |

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes and add tests
4. Ensure all tests pass: `./gradlew :core:crypto:allTests :composeApp:testDebugUnitTest`
5. Commit with a descriptive message
6. Open a Pull Request

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
