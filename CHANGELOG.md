# Changelog

All notable changes to Beauthy SDK are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Before 1.0.0, minor versions may contain breaking changes.

## 0.2.0 - Unreleased

The API moves to the `io.github.elliuqahs.beauthy` package and works from common code
with no platform setup. The 0.1.x API keeps working but is deprecated; see
[Migrating from 0.1.x](README.md#migrating-from-01x).

### Added

- `Totp` and `Hotp`, created once from a secret and usable from `commonMain`. The HMAC
  implementation is chosen automatically for each platform.
- `Totp.current()` and `Totp.at(timestampMillis)` return a `TotpCode` with the code, a
  grouped display format (`formatted()`), `remainingSeconds`, `progress`, and the
  validity window, all computed from one instant.
- `Totp.verify()` with a clock-drift window and `Hotp.verify()` with look-ahead, using
  constant-time comparison and ignoring whitespace in the entered code.
- `OtpAuthUri` to parse `otpauth://` URIs from authenticator QR codes and to build them
  for enrollment, with UTF-8 labels and `toTotp()` / `toHotp()`.
- `Secret.generate()` for new shared secrets from the platform's secure random source.
- `Base32.encode()`.
- `clockOffsetMillis` on `Totp` and `ClockOffset.from()` to correct a device clock that
  is set wrong.
- JVM target, for servers and desktop apps, and the `iosX64` target.
- `beauthy-sdk-coroutines` artifact: `Totp.codes()` is a `Flow<TotpCode>` that emits at
  the start of every second.
- `beauthy-sdk-compose` artifact: `rememberTotpCode()` exposes the live code as Compose
  state.
- Runnable examples in the `samples` module.

### Changed

- Arguments are validated: `digits` must be 6 to 9, `period` positive, and timestamps,
  counters and windows non-negative. Invalid values throw `IllegalArgumentException`
  with a descriptive message instead of producing wrong codes or crashing.
- `Base32.isValid()` agrees with `Base32.decode()`: padding is only accepted at the end,
  and lengths that no byte sequence can produce are rejected.
- `toString()` of `Totp`, `Hotp`, `TotpCode` and `OtpAuthUri` hides secrets and codes.

### Deprecated

- The `com.maoungedev.beauthy.core.crypto` API: `TotpGenerator`, `HmacProvider`,
  `JvmHmacProvider`, `IosHmacProvider`, `HmacAlgorithm` and `Base32`. Use the classes in
  `io.github.elliuqahs.beauthy` instead. They will be removed in 1.0.0.

## 0.1.0 - 2026-03-19

### Added

- `TotpGenerator` for TOTP (RFC 6238) and HOTP (RFC 4226) codes with SHA-1, SHA-256
  and SHA-512.
- `HmacProvider` with `JvmHmacProvider` for Android and `IosHmacProvider` for iOS.
- `Base32` decoding and validation.
- Android, iOS arm64 and iOS simulator arm64 targets.
