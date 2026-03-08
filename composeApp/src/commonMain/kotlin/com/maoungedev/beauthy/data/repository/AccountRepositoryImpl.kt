package com.maoungedev.beauthy.data.repository

import com.maoungedev.beauthy.data.dto.AccountDto
import com.maoungedev.beauthy.data.storage.AccountStorage
import com.maoungedev.beauthy.domain.model.OtpAccount
import com.maoungedev.beauthy.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AccountRepositoryImpl(
    private val storage: AccountStorage
) : AccountRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _accounts = MutableStateFlow<List<OtpAccount>>(emptyList())

    init {
        _accounts.value = loadFromStorage()
    }

    override fun observeAccounts(): Flow<List<OtpAccount>> = _accounts.asStateFlow()

    override suspend fun addAccount(account: OtpAccount) {
        val updated = _accounts.value + account
        saveToStorage(updated)
        _accounts.value = updated
    }

    override suspend fun deleteAccount(id: String) {
        val updated = _accounts.value.filter { it.id != id }
        saveToStorage(updated)
        _accounts.value = updated
    }

    override suspend fun incrementCounter(id: String) {
        val updated = _accounts.value.map { account ->
            if (account.id == id) account.copy(counter = account.counter + 1)
            else account
        }
        saveToStorage(updated)
        _accounts.value = updated
    }

    private fun loadFromStorage(): List<OtpAccount> {
        val raw = storage.loadRawJson() ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return try {
            json.decodeFromString<List<AccountDto>>(raw).map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveToStorage(accounts: List<OtpAccount>) {
        val dtos = accounts.map { AccountDto.fromDomain(it) }
        storage.saveRawJson(json.encodeToString(dtos))
    }
}
