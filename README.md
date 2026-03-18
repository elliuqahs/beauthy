<img width="1358" height="764" alt="image" src="https://github.com/user-attachments/assets/945900b5-5ba4-437c-8617-f469b7a86385" />


<h1 align="center">Beauthy</h1>

<p align="center"><b>Be Authentic. Be Secure. Be Everywhere.</b></p>

<p align="center">Cross-platform two-factor authenticator app generating TOTP & HOTP codes, built with Kotlin Multiplatform and Compose Multiplatform.</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF.svg?style=flat&logo=kotlin&logoColor=white"/></a>
  <a href="https://www.jetbrains.com/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose_MP-1.10.0-4285F4.svg?style=flat&logo=jetpackcompose&logoColor=white"/></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-24+-34A853.svg?style=flat&logo=android&logoColor=white"/></a>
  <a href="https://developer.apple.com/ios/"><img src="https://img.shields.io/badge/iOS-16+-000000.svg?style=flat&logo=apple&logoColor=white"/></a>
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat"/>
</p>

<!-- screenshots here
<p align="center">
  <img src="docs/screenshots/android_list.png" width="250"/>
  &nbsp;&nbsp;
  <img src="docs/screenshots/android_add.png" width="250"/>
  &nbsp;&nbsp;
  <img src="docs/screenshots/ios_list.png" width="250"/>
</p>
-->

---

## Features

- **TOTP & HOTP** — RFC 6238 / RFC 4226 compliant, SHA-1/256/512
- **QR Code Scan** — Add accounts instantly via camera
- **Encrypted Storage** — AES-256-GCM (Android) / NSFileProtection (iOS)
- **Biometric Lock** — Fingerprint & Face ID on app launch
- **Copy OTP** — Tap to copy, auto-clear clipboard after 30s
- **Search & Sort** — Filter by issuer or account name
- **Dark Theme** — Material3 dark mode
- **Standalone SDK** — `core` module usable as independent library

## Tech Stack

| Category | Library | Version |
|----------|---------|---------|
| Language | Kotlin Multiplatform | 2.3.0 |
| UI | Compose Multiplatform + Material3 | 1.10.0 |
| DI | Koin | 4.0.4 |
| Navigation | Voyager | 1.1.0-beta03 |
| QR Scan | CameraX + ML Kit / AVFoundation | 1.4.2 |
| Storage | EncryptedSharedPreferences / NSFileProtection | — |

## Architecture

Clean Architecture + MVI — shared business logic, platform code injected via Koin.

```
┌─────────────────────────────────────────┐
│  Presentation (MVI)                     │
│  Voyager Screens + ScreenModels         │
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

## Beauthy OTP SDK

The `core` module is a standalone KMP library for TOTP/HOTP generation — zero app dependencies, publishable to Maven Central. Full docs at **[core/README.md](core/README.md)**.

```kotlin
implementation("com.beauthy:otp-sdk:1.0.0")
```

```kotlin
val generator = TotpGenerator(JvmHmacProvider())
val code = generator.generate(secret = "JBSWY3DPEHPK3PXP", timestampMillis = System.currentTimeMillis())
```

## Build & Run

```bash
./gradlew composeApp:assembleDebug          # Build Android APK
./gradlew composeApp:installDebug           # Run on device/emulator
./gradlew composeApp:compileKotlinIosSimulatorArm64  # iOS compile check
# iOS: open iosApp/iosApp.xcodeproj → Run
```

## License

```
Copyright 2025 Beauthy

Licensed under the Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0
```
