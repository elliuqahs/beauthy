package com.maoungedev.beauthy.presentation.list

import com.maoungedev.beauthy.domain.model.OtpAccount

data class AccountWithCode(
    val account: OtpAccount,
    val code: String
)

enum class SortOrder {
    NONE,
    ISSUER_ASC,
    ISSUER_DESC
}

sealed interface AccountListSideEffect {
    data object Idle : AccountListSideEffect
    data object CodeCopied : AccountListSideEffect
}

data class AccountListUiState(
    val accounts: List<AccountWithCode> = emptyList(),
    val remainingSeconds: Int = 30,
    val progress: Float = 1f,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NONE
)
