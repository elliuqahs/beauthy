package com.maoungedev.beauthy.core.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.cinterop.ObjCObjectVar

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun RequestBiometricAuth(
    title: String,
    subtitle: String,
    onResult: (BiometricResult) -> Unit
) {
    LaunchedEffect(Unit) {
        val context = LAContext()

        val canEvaluate = memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            context.canEvaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                error.ptr
            )
        }

        if (!canEvaluate) {
            onResult(BiometricResult.NOT_AVAILABLE)
            return@LaunchedEffect
        }

        val success = suspendCoroutine { continuation ->
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = subtitle
            ) { success, _ ->
                dispatch_async(dispatch_get_main_queue()) {
                    continuation.resume(success)
                }
            }
        }

        onResult(if (success) BiometricResult.SUCCESS else BiometricResult.FAILED)
    }
}
