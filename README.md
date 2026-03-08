# Beauthy

Google Authenticator-like app that generates **TOTP/HOTP** one-time passwords. Built with **Kotlin Multiplatform + Compose Multiplatform** — single codebase for Android & iOS.

---

## Features

- Generate 6-digit TOTP codes (RFC 6238) that refresh every 30 seconds
- HOTP (counter-based) support
- QR code scanning to add accounts (otpauth:// URI)
- Manual account entry with Base32 secret validation
- Encrypted local storage (AES-256-GCM on Android, NSFileProtectionComplete on iOS)
- Auto-migration from plain-text storage

---

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

---

## Architecture

**Clean Architecture + MVI + SOLID**

### Module Structure

```
Beauthy/
├── core/crypto/               # Standalone KMP library module
│   └── src/
│       ├── commonMain/        Base32, TotpGenerator, HmacProvider (interface)
│       ├── androidMain/       JvmHmacProvider (javax.crypto)
│       ├── iosMain/           IosHmacProvider (CoreCrypto CCHmac)
│       └── commonTest/        RFC 4648 & RFC 6238 test vectors
│
├── composeApp/                # Main application module
│   └── src/
│       ├── commonMain/
│       │   ├── App.kt
│       │   ├── core/time/         TimeProvider (interface)
│       │   ├── core/uri/          OtpAuthParser
│       │   ├── domain/model/      OtpAccount, OtpType
│       │   ├── domain/repository/ AccountRepository (interface)
│       │   ├── data/dto/          AccountDto (@Serializable)
│       │   ├── data/storage/      AccountStorage (interface)
│       │   ├── data/repository/   AccountRepositoryImpl
│       │   ├── presentation/
│       │   │   ├── splash/        SplashScreen
│       │   │   ├── list/          AccountListScreen + ScreenModel
│       │   │   ├── add/           AddAccountScreen + ScreenModel
│       │   │   └── scan/          ScanBarcodeScreen + BarcodeScannerView
│       │   └── di/                Koin modules
│       ├── androidMain/           Platform implementations
│       ├── iosMain/               Platform implementations
│       └── commonTest/            Unit tests
│
├── iosApp/                    # Xcode project
└── gradle/libs.versions.toml # Version catalog
```

### Layer Diagram

```
Presentation (MVI)
  │  Voyager Screens + ScreenModels
  │  Intent → ScreenModel → StateFlow / SideEffect → UI
  ▼
Domain
  │  OtpAccount, AccountRepository (interface)
  ▼
Data
  │  AccountRepositoryImpl, AccountDto, AccountStorage (interface)
  ▼
Platform (Koin DI)
     Android: EncryptedSharedPreferences, CameraX, ML Kit
     iOS: NSFileProtection, AVFoundation, CoreCrypto
```

### Design Principles

- **No expect/actual for business logic** — all abstractions via interfaces + Koin DI
- `expect/actual` only for: `BarcodeScannerView`, `platformModule()`, `koinPlatformConfig()`
- Separate ScreenModel per screen (SRP)
- `AccountRepository.observeAccounts()` returns `Flow` for reactive updates

---

## How TOTP Works (RFC 6238)

```
Secret (Base32)  +  Current Time
       │                  │
       ▼                  ▼
  Base32.decode()    timestamp / 30 = counter
       │                  │
       └───────┬──────────┘
               ▼
        HMAC-SHA1(key, counter)
               │
               ▼
        Dynamic Truncation (RFC 4226)
               │
               ▼
          6-digit OTP
```

Code changes every 30 seconds because `counter = timestamp / 30`.

---

## Security

| Layer | Android | iOS |
|-------|---------|-----|
| Storage | EncryptedSharedPreferences (AES-256-GCM) | File + NSFileProtectionComplete |
| Crypto | javax.crypto.Mac (HmacSHA1) | CoreCrypto CCHmac |
| Memory | ByteArray.fill(0) in finally blocks | Same |
| Validation | Base32 validation + min 16 chars | Same |
| Display | OtpAccount.toString() masks secret | Same |

---

## Navigation

```
SplashScreen ──replace──> AccountListScreen ──push──> AddAccountScreen
                                            ──push──> ScanBarcodeScreen
```

---

## Testing

All tests in **commonTest** — run without emulator/device:

```
core/crypto/commonTest/
├── Base32Test              RFC 4648 vectors, validation, edge cases
└── TotpGeneratorTest       RFC 6238 vectors with FakeHmacProvider

composeApp/commonTest/
├── data/repository/
│   └── AccountRepositoryImplTest   CRUD, corrupted data, FakeStorage
└── presentation/
    ├── list/AccountListScreenModelTest   state, timer, delete intent
    └── add/AddAccountScreenModelTest    validation, submit, consume event
```

```bash
# Run all tests
./gradlew :core:crypto:allTests :composeApp:testDebugUnitTest
```

---

## Build & Run

```bash
# Android
./gradlew composeApp:assembleDebug

# iOS (compile check)
./gradlew composeApp:compileKotlinIosSimulatorArm64

# iOS (run via Xcode)
# Open iosApp/iosApp.xcodeproj → Run
```

---

## DI Graph (Koin)

```
KoinApplication (App.kt)
├── commonModule
│   ├── TotpGenerator (single) ← HmacProvider
│   ├── AccountRepositoryImpl (single) ← AccountStorage
│   ├── AccountListScreenModel (factory) ← AccountRepository, TotpGenerator, TimeProvider
│   └── AddAccountScreenModel (factory) ← AccountRepository, TimeProvider
└── platformModule
    ├── [Android] JvmHmacProvider, SystemTimeProvider, EncryptedAccountStorage
    └── [iOS] IosHmacProvider, IosTimeProvider, SecureFileAccountStorage
```
