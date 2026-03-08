package com.maoungedev.beauthy.presentation.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import beauthy.composeapp.generated.resources.Res
import beauthy.composeapp.generated.resources.cd_back
import beauthy.composeapp.generated.resources.scan_invalid_qr
import beauthy.composeapp.generated.resources.scan_permission_required
import beauthy.composeapp.generated.resources.title_scan_qr
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.maoungedev.beauthy.core.time.TimeProvider
import com.maoungedev.beauthy.core.uri.OtpAuthParser
import com.maoungedev.beauthy.domain.repository.AccountRepository
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class ScanBarcodeScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = koinInject<AccountRepository>()
        val timeProvider = koinInject<TimeProvider>()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val invalidQrMessage = stringResource(Res.string.scan_invalid_qr)
        val permissionRequiredMessage = stringResource(Res.string.scan_permission_required)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.title_scan_qr)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.cd_back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                BarcodeScannerView(
                    onBarcodeScanned = { rawValue ->
                        val id = "${timeProvider.currentTimeMillis()}_${(1000..9999).random()}"
                        val account = OtpAuthParser.parse(rawValue, id)
                        if (account != null) {
                            scope.launch {
                                repository.addAccount(account)
                                navigator.pop()
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(invalidQrMessage)
                            }
                        }
                    },
                    onPermissionDenied = {
                        scope.launch {
                            snackbarHostState.showSnackbar(permissionRequiredMessage)
                        }
                    }
                )
            }
        }
    }
}
