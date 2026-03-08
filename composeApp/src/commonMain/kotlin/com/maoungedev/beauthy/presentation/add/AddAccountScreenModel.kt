package com.maoungedev.beauthy.presentation.add

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.maoungedev.beauthy.core.crypto.Base32
import com.maoungedev.beauthy.core.time.TimeProvider
import com.maoungedev.beauthy.domain.model.OtpAccount
import com.maoungedev.beauthy.domain.model.OtpType
import com.maoungedev.beauthy.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AddAccountIntent {
    data class Submit(
        val issuer: String,
        val accountName: String,
        val secret: String,
        val otpType: OtpType = OtpType.TOTP
    ) : AddAccountIntent
    data object ConsumeEvent : AddAccountIntent
}

enum class ValidationErrorType {
    ISSUER_EMPTY,
    ACCOUNT_EMPTY,
    SECRET_EMPTY,
    SECRET_SHORT,
    SECRET_INVALID
}

sealed interface AddAccountSideEffect {
    data object Idle : AddAccountSideEffect
    data object Success : AddAccountSideEffect
    data class ValidationError(val type: ValidationErrorType) : AddAccountSideEffect
}

class AddAccountScreenModel(
    private val repository: AccountRepository,
    private val timeProvider: TimeProvider
) : ScreenModel {

    private val _sideEffect = MutableStateFlow<AddAccountSideEffect>(AddAccountSideEffect.Idle)
    val sideEffect: StateFlow<AddAccountSideEffect> = _sideEffect.asStateFlow()

    fun onIntent(intent: AddAccountIntent) {
        when (intent) {
            is AddAccountIntent.Submit -> addAccount(intent.issuer, intent.accountName, intent.secret, intent.otpType)
            is AddAccountIntent.ConsumeEvent -> _sideEffect.value = AddAccountSideEffect.Idle
        }
    }

    private fun addAccount(issuer: String, accountName: String, secret: String, otpType: OtpType) {
        val trimmedIssuer = issuer.trim()
        val trimmedName = accountName.trim()
        val cleanSecret = secret.trim().uppercase().replace(" ", "")

        val errorType = validate(trimmedIssuer, trimmedName, cleanSecret)
        if (errorType != null) {
            _sideEffect.value = AddAccountSideEffect.ValidationError(errorType)
            return
        }

        screenModelScope.launch {
            val account = OtpAccount(
                id = generateId(),
                issuer = trimmedIssuer,
                accountName = trimmedName,
                secret = cleanSecret,
                type = otpType
            )
            repository.addAccount(account)
            _sideEffect.value = AddAccountSideEffect.Success
        }
    }

    private fun validate(issuer: String, accountName: String, secret: String): ValidationErrorType? {
        return when {
            issuer.isBlank() -> ValidationErrorType.ISSUER_EMPTY
            accountName.isBlank() -> ValidationErrorType.ACCOUNT_EMPTY
            secret.isBlank() -> ValidationErrorType.SECRET_EMPTY
            secret.length < 16 -> ValidationErrorType.SECRET_SHORT
            !Base32.isValid(secret) -> ValidationErrorType.SECRET_INVALID
            else -> null
        }
    }

    private fun generateId(): String =
        "${timeProvider.currentTimeMillis()}_${(1000..9999).random()}"
}
