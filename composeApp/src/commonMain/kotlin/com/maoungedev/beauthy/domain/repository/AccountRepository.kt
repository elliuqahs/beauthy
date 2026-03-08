package com.maoungedev.beauthy.domain.repository

import com.maoungedev.beauthy.domain.model.OtpAccount
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<OtpAccount>>
    suspend fun addAccount(account: OtpAccount)
    suspend fun deleteAccount(id: String)
    suspend fun incrementCounter(id: String)
}
