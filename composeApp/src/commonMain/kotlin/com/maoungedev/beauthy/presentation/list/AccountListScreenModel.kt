package com.maoungedev.beauthy.presentation.list

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.maoungedev.beauthy.core.clipboard.ClipboardService
import com.maoungedev.beauthy.core.crypto.TotpGenerator
import com.maoungedev.beauthy.core.time.TimeProvider
import com.maoungedev.beauthy.domain.model.OtpAccount
import com.maoungedev.beauthy.domain.model.OtpType
import com.maoungedev.beauthy.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AccountListIntent {
    data class DeleteAccount(val id: String) : AccountListIntent
    data class RefreshHotp(val id: String) : AccountListIntent
    data class CopyCode(val code: String) : AccountListIntent
    data class SearchQuery(val query: String) : AccountListIntent
    data object ToggleSort : AccountListIntent
    data object ConsumeSideEffect : AccountListIntent
}

class AccountListScreenModel(
    private val repository: AccountRepository,
    private val totpGenerator: TotpGenerator,
    private val timeProvider: TimeProvider,
    private val clipboardService: ClipboardService,
    private val scope: CoroutineScope? = null
) : ScreenModel {

    private val coroutineScope: CoroutineScope get() = scope ?: screenModelScope

    private val _state = MutableStateFlow(AccountListUiState())
    val state: StateFlow<AccountListUiState> = _state.asStateFlow()

    private val _sideEffect = MutableStateFlow<AccountListSideEffect>(AccountListSideEffect.Idle)
    val sideEffect: StateFlow<AccountListSideEffect> = _sideEffect.asStateFlow()

    private var allAccountsWithCodes: List<AccountWithCode> = emptyList()
    private var searchQuery: String = ""
    private var sortOrder: SortOrder = SortOrder.NONE
    private var clearClipboardJob: Job? = null

    init {
        observeAccounts()
        startTimer()
    }

    fun onIntent(intent: AccountListIntent) {
        when (intent) {
            is AccountListIntent.DeleteAccount -> deleteAccount(intent.id)
            is AccountListIntent.RefreshHotp -> refreshHotp(intent.id)
            is AccountListIntent.CopyCode -> copyCode(intent.code)
            is AccountListIntent.SearchQuery -> updateSearch(intent.query)
            is AccountListIntent.ToggleSort -> toggleSort()
            is AccountListIntent.ConsumeSideEffect -> _sideEffect.value = AccountListSideEffect.Idle
        }
    }

    private fun observeAccounts() {
        coroutineScope.launch {
            repository.observeAccounts().collect { accounts ->
                updateCodesFor(accounts)
            }
        }
    }

    private fun startTimer() {
        coroutineScope.launch {
            while (true) {
                val now = timeProvider.currentTimeMillis()
                val remaining = totpGenerator.remainingSeconds(now)
                allAccountsWithCodes = allAccountsWithCodes.map { item ->
                    item.copy(code = generateCode(item.account, now))
                }
                emitState(remaining)
                delay(1000)
            }
        }
    }

    private fun deleteAccount(id: String) {
        coroutineScope.launch {
            repository.deleteAccount(id)
        }
    }

    private fun refreshHotp(id: String) {
        coroutineScope.launch {
            repository.incrementCounter(id)
        }
    }

    private fun copyCode(code: String) {
        val cleanCode = code.replace(" ", "")
        clipboardService.copy(cleanCode)
        _sideEffect.value = AccountListSideEffect.CodeCopied
        clearClipboardJob?.cancel()
        clearClipboardJob = coroutineScope.launch {
            delay(30_000)
            clipboardService.clear()
        }
    }

    private fun updateSearch(query: String) {
        searchQuery = query
        emitState()
    }

    private fun toggleSort() {
        sortOrder = when (sortOrder) {
            SortOrder.NONE -> SortOrder.ISSUER_ASC
            SortOrder.ISSUER_ASC -> SortOrder.ISSUER_DESC
            SortOrder.ISSUER_DESC -> SortOrder.NONE
        }
        emitState()
    }

    private fun updateCodesFor(accounts: List<OtpAccount>) {
        val now = timeProvider.currentTimeMillis()
        val remaining = totpGenerator.remainingSeconds(now)
        allAccountsWithCodes = accounts.map { account ->
            AccountWithCode(
                account = account,
                code = generateCode(account, now)
            )
        }
        emitState(remaining)
    }

    private fun emitState(remaining: Int = _state.value.remainingSeconds) {
        var result = allAccountsWithCodes

        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.account.issuer.contains(searchQuery, ignoreCase = true) ||
                    it.account.accountName.contains(searchQuery, ignoreCase = true)
            }
        }

        result = when (sortOrder) {
            SortOrder.NONE -> result
            SortOrder.ISSUER_ASC -> result.sortedBy { it.account.issuer.lowercase() }
            SortOrder.ISSUER_DESC -> result.sortedByDescending { it.account.issuer.lowercase() }
        }

        _state.value = AccountListUiState(
            accounts = result,
            remainingSeconds = remaining,
            progress = remaining / 30f,
            searchQuery = searchQuery,
            sortOrder = sortOrder
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
