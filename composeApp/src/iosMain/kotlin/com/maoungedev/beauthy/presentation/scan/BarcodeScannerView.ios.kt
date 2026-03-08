package com.maoungedev.beauthy.presentation.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import beauthy.composeapp.generated.resources.Res
import beauthy.composeapp.generated.resources.scan_permission_required
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.compose.resources.stringResource
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectMake
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.posix.QOS_CLASS_USER_INITIATED

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BarcodeScannerView(
    onBarcodeScanned: (String) -> Unit,
    onPermissionDenied: () -> Unit
) {
    var hasPermission by remember { mutableStateOf(false) }
    var permissionChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (status) {
            AVAuthorizationStatusAuthorized -> {
                hasPermission = true
                permissionChecked = true
            }
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    dispatch_async(dispatch_get_main_queue()) {
                        hasPermission = granted
                        permissionChecked = true
                        if (!granted) onPermissionDenied()
                    }
                }
            }
            else -> {
                permissionChecked = true
                onPermissionDenied()
            }
        }
    }

    if (hasPermission) {
        IosCameraPreview(onBarcodeScanned = onBarcodeScanned)
    } else if (permissionChecked) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(Res.string.scan_permission_required))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun IosCameraPreview(
    onBarcodeScanned: (String) -> Unit
) {
    var scanned by remember { mutableStateOf(false) }
    val captureSession = remember { AVCaptureSession() }

    val delegate = remember {
        object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputMetadataObjects: List<*>,
                fromConnection: AVCaptureConnection
            ) {
                if (scanned) return
                for (obj in didOutputMetadataObjects) {
                    val metadataObj = obj as? AVMetadataMachineReadableCodeObject ?: continue
                    val value = metadataObj.stringValue ?: continue
                    if (!scanned) {
                        scanned = true
                        onBarcodeScanned(value)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            dispatch_async(dispatch_get_global_queue(QOS_CLASS_USER_INITIATED.toLong(), 0u)) {
                if (captureSession.isRunning()) {
                    captureSession.stopRunning()
                }
            }
        }
    }

    UIKitView(
        factory = {
            val containerView = UIView(frame = CGRectMake(0.0, 0.0, 1.0, 1.0))

            val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            if (device != null) {
                val input = AVCaptureDeviceInput.deviceInputWithDevice(device, error = null)
                if (input != null && captureSession.canAddInput(input)) {
                    captureSession.addInput(input)
                }

                val metadataOutput = AVCaptureMetadataOutput()
                if (captureSession.canAddOutput(metadataOutput)) {
                    captureSession.addOutput(metadataOutput)
                    metadataOutput.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
                    metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
                }

                val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession)
                previewLayer.frame = containerView.bounds
                previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                containerView.layer.addSublayer(previewLayer)

                dispatch_async(dispatch_get_global_queue(QOS_CLASS_USER_INITIATED.toLong(), 0u)) {
                    captureSession.startRunning()
                }
            }

            containerView
        },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            val previewLayer = view.layer.sublayers?.firstOrNull() as? AVCaptureVideoPreviewLayer
            CATransaction.begin()
            CATransaction.setDisableActions(true)
            previewLayer?.frame = view.bounds
            CATransaction.commit()
        }
    )
}
