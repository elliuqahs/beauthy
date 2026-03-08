package com.maoungedev.beauthy.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import beauthy.composeapp.generated.resources.Res
import beauthy.composeapp.generated.resources.action_cancel
import beauthy.composeapp.generated.resources.action_delete
import beauthy.composeapp.generated.resources.cd_add_account
import beauthy.composeapp.generated.resources.cd_delete_account
import beauthy.composeapp.generated.resources.cd_otp_code
import beauthy.composeapp.generated.resources.cd_refresh_hotp
import beauthy.composeapp.generated.resources.cd_remaining_seconds
import beauthy.composeapp.generated.resources.cd_scan_qr
import beauthy.composeapp.generated.resources.dialog_delete_message
import beauthy.composeapp.generated.resources.dialog_delete_title
import beauthy.composeapp.generated.resources.empty_subtitle
import beauthy.composeapp.generated.resources.empty_title
import beauthy.composeapp.generated.resources.key_type_hotp
import beauthy.composeapp.generated.resources.key_type_totp
import beauthy.composeapp.generated.resources.label_counter
import beauthy.composeapp.generated.resources.title_authenticator
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.maoungedev.beauthy.domain.model.OtpType
import com.maoungedev.beauthy.presentation.add.AddAccountScreen
import com.maoungedev.beauthy.presentation.scan.ScanBarcodeScreen
import org.jetbrains.compose.resources.stringResource

class AccountListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AccountListScreenModel>()
        val uiState by screenModel.state.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.title_authenticator)) },
                    actions = {
                        IconButton(onClick = { navigator.push(ScanBarcodeScreen()) }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = stringResource(Res.string.cd_scan_qr))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigator.push(AddAccountScreen()) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.cd_add_account))
                }
            }
        ) { padding ->
            if (uiState.accounts.isEmpty()) {
                EmptyState(modifier = Modifier.padding(padding))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.accounts, key = { it.account.id }) { item ->
                        AccountCard(
                            item = item,
                            remainingSeconds = uiState.remainingSeconds,
                            progress = uiState.progress,
                            onDelete = {
                                screenModel.onIntent(AccountListIntent.DeleteAccount(item.account.id))
                            },
                            onRefreshHotp = {
                                screenModel.onIntent(AccountListIntent.RefreshHotp(item.account.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    item: AccountWithCode,
    remainingSeconds: Int,
    progress: Float,
    onDelete: () -> Unit,
    onRefreshHotp: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isHotp = item.account.type == OtpType.HOTP
    val deleteTitle = stringResource(Res.string.dialog_delete_title)
    val deleteMessage = stringResource(Res.string.dialog_delete_message, item.account.issuer, item.account.accountName)
    val deleteAction = stringResource(Res.string.action_delete)
    val cancelAction = stringResource(Res.string.action_cancel)
    val cdDelete = stringResource(Res.string.cd_delete_account)
    val cdOtp = stringResource(Res.string.cd_otp_code, formatCode(item.code))
    val cdRemaining = stringResource(Res.string.cd_remaining_seconds, remainingSeconds)
    val typeLabel = if (isHotp) stringResource(Res.string.key_type_hotp) else stringResource(Res.string.key_type_totp)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(deleteTitle) },
            text = { Text(deleteMessage) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text(deleteAction, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(cancelAction) }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = if (isHotp) {
                    "${item.account.issuer}, ${item.account.accountName}, $cdOtp, $typeLabel"
                } else {
                    "${item.account.issuer}, ${item.account.accountName}, $cdOtp, $cdRemaining"
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.account.issuer,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isHotp) "HOTP" else "TOTP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = item.account.accountName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatCode(item.code),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (!isHotp && remainingSeconds <= 5)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isHotp) {
                    // HOTP: Refresh button + counter
                    IconButton(
                        onClick = onRefreshHotp,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.cd_refresh_hotp),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = stringResource(Res.string.label_counter, item.account.counter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // TOTP: Timer countdown
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp,
                            strokeCap = StrokeCap.Round,
                            color = if (remainingSeconds <= 5)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$remainingSeconds",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = cdDelete,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatCode(code: String): String {
    if (code.length != 6) return code
    return "${code.take(3)} ${code.drop(3)}"
}
