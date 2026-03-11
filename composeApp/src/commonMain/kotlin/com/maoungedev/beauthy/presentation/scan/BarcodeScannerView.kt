package com.maoungedev.beauthy.presentation.scan

import androidx.compose.runtime.Composable

@Composable
expect fun BarcodeScannerView(
    onBarcodeScanned: (String) -> Boolean,
    onPermissionDenied: () -> Unit
)
