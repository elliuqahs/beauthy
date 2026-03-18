package com.maoungedev.beauthy.core.biometric

import androidx.compose.runtime.Composable

enum class BiometricResult {
    SUCCESS,
    FAILED,
    NOT_AVAILABLE
}

@Composable
expect fun RequestBiometricAuth(
    title: String,
    subtitle: String,
    onResult: (BiometricResult) -> Unit
)
