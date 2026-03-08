package com.maoungedev.beauthy.presentation.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import beauthy.composeapp.generated.resources.Res
import beauthy.composeapp.generated.resources.action_add_account
import beauthy.composeapp.generated.resources.add_subtitle
import beauthy.composeapp.generated.resources.cd_back
import beauthy.composeapp.generated.resources.cd_hide_secret
import beauthy.composeapp.generated.resources.cd_show_secret
import beauthy.composeapp.generated.resources.error_account_empty
import beauthy.composeapp.generated.resources.error_issuer_empty
import beauthy.composeapp.generated.resources.error_secret_empty
import beauthy.composeapp.generated.resources.error_secret_invalid
import beauthy.composeapp.generated.resources.error_secret_short
import beauthy.composeapp.generated.resources.hint_account_name
import beauthy.composeapp.generated.resources.hint_issuer
import beauthy.composeapp.generated.resources.hint_secret_key
import beauthy.composeapp.generated.resources.key_type_hotp
import beauthy.composeapp.generated.resources.key_type_totp
import beauthy.composeapp.generated.resources.label_account_name
import beauthy.composeapp.generated.resources.label_issuer
import beauthy.composeapp.generated.resources.label_key_type
import beauthy.composeapp.generated.resources.label_secret_key
import beauthy.composeapp.generated.resources.title_add_account
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.maoungedev.beauthy.domain.model.OtpType
import org.jetbrains.compose.resources.stringResource

class AddAccountScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AddAccountScreenModel>()

        var issuer by remember { mutableStateOf("") }
        var accountName by remember { mutableStateOf("") }
        var secret by remember { mutableStateOf("") }
        var secretVisible by remember { mutableStateOf(false) }
        var selectedType by remember { mutableStateOf(OtpType.TOTP) }
        var typeDropdownExpanded by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val effect by screenModel.sideEffect.collectAsStateWithLifecycle()

        val errorMessages = mapOf(
            ValidationErrorType.ISSUER_EMPTY to stringResource(Res.string.error_issuer_empty),
            ValidationErrorType.ACCOUNT_EMPTY to stringResource(Res.string.error_account_empty),
            ValidationErrorType.SECRET_EMPTY to stringResource(Res.string.error_secret_empty),
            ValidationErrorType.SECRET_SHORT to stringResource(Res.string.error_secret_short),
            ValidationErrorType.SECRET_INVALID to stringResource(Res.string.error_secret_invalid),
        )

        LaunchedEffect(effect) {
            when (val e = effect) {
                is AddAccountSideEffect.Success -> {
                    screenModel.onIntent(AddAccountIntent.ConsumeEvent)
                    navigator.pop()
                }
                is AddAccountSideEffect.ValidationError -> {
                    errorMessage = errorMessages[e.type]
                    screenModel.onIntent(AddAccountIntent.ConsumeEvent)
                }
                is AddAccountSideEffect.Idle -> {}
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.title_add_account)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.cd_back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.add_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = issuer,
                    onValueChange = { issuer = it; errorMessage = null },
                    label = { Text(stringResource(Res.string.label_issuer)) },
                    placeholder = { Text(stringResource(Res.string.hint_issuer)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it; errorMessage = null },
                    label = { Text(stringResource(Res.string.label_account_name)) },
                    placeholder = { Text(stringResource(Res.string.hint_account_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it; errorMessage = null },
                    label = { Text(stringResource(Res.string.label_secret_key)) },
                    placeholder = { Text(stringResource(Res.string.hint_secret_key)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (secretVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { secretVisible = !secretVisible }) {
                            Icon(
                                if (secretVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = if (secretVisible)
                                    stringResource(Res.string.cd_hide_secret)
                                else
                                    stringResource(Res.string.cd_show_secret)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Key Type Selector + Add Button
                val totpLabel = stringResource(Res.string.key_type_totp)
                val hotpLabel = stringResource(Res.string.key_type_hotp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (selectedType == OtpType.TOTP) totpLabel else hotpLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.label_key_type)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(totpLabel) },
                                onClick = {
                                    selectedType = OtpType.TOTP
                                    typeDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(hotpLabel) },
                                onClick = {
                                    selectedType = OtpType.HOTP
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            screenModel.onIntent(
                                AddAccountIntent.Submit(issuer, accountName, secret, selectedType)
                            )
                        }
                    ) {
                        Text(stringResource(Res.string.action_add_account))
                    }
                }
            }
        }
    }
}
