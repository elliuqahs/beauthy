package com.maoungedev.beauthy.presentation.list

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.maoungedev.beauthy.core.crypto.TotpGenerator
import com.maoungedev.beauthy.core.time.TimeProvider
import com.maoungedev.beauthy.domain.model.OtpAccount
import com.maoungedev.beauthy.domain.model.OtpType
import com.maoungedev.beauthy.domain.repository.AccountRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AccountListIntent {
    data class DeleteAccount(val id: String) : AccountListIntent
    data class RefreshHotp(val id: String) : AccountListIntent
}

class AccountListScreenModel(
    private val repository: AccountRepository,
    private val totpGenerator: TotpGenerator,
    private val timeProvider: TimeProvider
) : ScreenModel {

    private val _state = MutableStateFlow(AccountListUiState())
    val state: StateFlow<AccountListUiState> = _state.asStateFlow()

    init {
        observeAccounts()
        startTimer()
    }

    fun onIntent(intent: AccountListIntent) {
        when (intent) {
            is AccountListIntent.DeleteAccount -> deleteAccount(intent.id)
            is AccountListIntent.RefreshHotp -> refreshHotp(intent.id)
        }
    }

    private fun observeAccounts() {
        screenModelScope.launch {
            repository.observeAccounts().collect { accounts ->
                updateCodesFor(accounts)
            }
        }
    }

    private fun startTimer() {
        screenModelScope.launch {
            while (true) {
                val now = timeProvider.currentTimeMillis()
                val remaining = totpGenerator.remainingSeconds(now)
                val accounts = _state.value.accounts.map { it.account }
                val accountsWithCodes = accounts.map { account ->
                    AccountWithCode(
                        account = account,
                        code = generateCode(account, now)
                    )
                }
                _state.value = AccountListUiState(
                    accounts = accountsWithCodes,
                    remainingSeconds = remaining,
                    progress = remaining / 30f
                )
                delay(1000)
            }
        }
    }

    private fun deleteAccount(id: String) {
        screenModelScope.launch {
            repository.deleteAccount(id)
        }
    }

    private fun refreshHotp(id: String) {
        screenModelScope.launch {
            repository.incrementCounter(id)
        }
    }

    private fun updateCodesFor(accounts: List<OtpAccount>) {
        val now = timeProvider.currentTimeMillis()
        val remaining = totpGenerator.remainingSeconds(now)
        _state.value = AccountListUiState(
            accounts = accounts.map { account ->
                AccountWithCode(
                    account = account,
                    code = generateCode(account, now)
                )
            },
            remainingSeconds = remaining,
            progress = remaining / 30f
        )
    }

    private fun generateCode(account: OtpAccount, timestamp: Long): String {
        return try {
            when (account.type) {
                OtpType.TOTP -> totpGenerator.generate(
                    secret = account.secret,
                    timestampMillis = timestamp,
                    digits = account.digits,
                    period = account.period
                )
                OtpType.HOTP -> totpGenerator.generateHotp(
                    secret = account.secret,
                    counter = account.counter,
                    digits = account.digits
                )
            }
        } catch (_: Exception) {
            "------"
        }
    }
}
