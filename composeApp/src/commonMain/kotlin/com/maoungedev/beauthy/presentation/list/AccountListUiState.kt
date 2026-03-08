package com.maoungedev.beauthy.presentation.list

import com.maoungedev.beauthy.domain.model.OtpAccount

data class AccountWithCode(
    val account: OtpAccount,
    val code: String
)

data class AccountListUiState(
    val accounts: List<AccountWithCode> = emptyList(),
    val remainingSeconds: Int = 30,
    val progress: Float = 1f
)
