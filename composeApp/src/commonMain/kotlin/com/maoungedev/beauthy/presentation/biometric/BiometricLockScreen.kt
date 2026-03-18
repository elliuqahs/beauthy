package com.maoungedev.beauthy.presentation.biometric

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import beauthy.composeapp.generated.resources.Res
import beauthy.composeapp.generated.resources.biometric_failed
import beauthy.composeapp.generated.resources.biometric_retry
import beauthy.composeapp.generated.resources.biometric_subtitle
import beauthy.composeapp.generated.resources.biometric_title
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.maoungedev.beauthy.core.biometric.BiometricResult
import com.maoungedev.beauthy.core.biometric.RequestBiometricAuth
import com.maoungedev.beauthy.presentation.list.AccountListScreen
import org.jetbrains.compose.resources.stringResource

class BiometricLockScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var authFailed by remember { mutableStateOf(false) }
        var triggerAuth by remember { mutableStateOf(true) }

        if (triggerAuth) {
            RequestBiometricAuth(
                title = stringResource(Res.string.biometric_title),
                subtitle = stringResource(Res.string.biometric_subtitle),
                onResult = { result ->
                    triggerAuth = false
                    when (result) {
                        BiometricResult.SUCCESS -> navigator.replace(AccountListScreen())
                        BiometricResult.NOT_AVAILABLE -> navigator.replace(AccountListScreen())
                        BiometricResult.FAILED -> authFailed = true
                    }
                }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(Res.string.biometric_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (authFailed) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.biometric_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    authFailed = false
                    triggerAuth = true
                }) {
                    Text(stringResource(Res.string.biometric_retry))
                }
            }
        }
    }
}
