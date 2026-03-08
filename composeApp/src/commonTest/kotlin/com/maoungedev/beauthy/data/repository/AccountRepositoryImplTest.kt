package com.maoungedev.beauthy.data.repository

import com.maoungedev.beauthy.data.storage.AccountStorage
import com.maoungedev.beauthy.domain.model.OtpAccount
import com.maoungedev.beauthy.domain.model.OtpType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccountRepositoryImplTest {

    private fun createFakeStorage(initialJson: String? = null): FakeAccountStorage {
        return FakeAccountStorage(initialJson)
    }

    private fun createAccount(
        id: String = "1",
        issuer: String = "Google",
        accountName: String = "user@gmail.com",
        secret: String = "JBSWY3DPEHPK3PXP"
    ) = OtpAccount(id = id, issuer = issuer, accountName = accountName, secret = secret)

    @Test
    fun observeAccounts_emptyStorage_returnsEmptyList() = runTest {
        val repo = AccountRepositoryImpl(createFakeStorage())
        val accounts = repo.observeAccounts().first()
        assertTrue(accounts.isEmpty())
    }

    @Test
    fun observeAccounts_withExistingData_loadsAccounts() = runTest {
        val json = """[{"id":"1","issuer":"Google","accountName":"user@gmail.com","secret":"JBSWY3DPEHPK3PXP","digits":6,"period":30}]"""
        val repo = AccountRepositoryImpl(createFakeStorage(json))
        val accounts = repo.observeAccounts().first()
        assertEquals(1, accounts.size)
        assertEquals("Google", accounts[0].issuer)
    }

    @Test
    fun addAccount_persistsAndEmits() = runTest {
        val storage = createFakeStorage()
        val repo = AccountRepositoryImpl(storage)

        val account = createAccount()
        repo.addAccount(account)

        val accounts = repo.observeAccounts().first()
        assertEquals(1, accounts.size)
        assertEquals("Google", accounts[0].issuer)
        assertTrue(storage.savedJson!!.contains("Google"))
    }

    @Test
    fun deleteAccount_removesAndPersists() = runTest {
        val storage = createFakeStorage()
        val repo = AccountRepositoryImpl(storage)

        repo.addAccount(createAccount(id = "1"))
        repo.addAccount(createAccount(id = "2", issuer = "GitHub"))
        repo.deleteAccount("1")

        val accounts = repo.observeAccounts().first()
        assertEquals(1, accounts.size)
        assertEquals("GitHub", accounts[0].issuer)
    }

    @Test
    fun observeAccounts_corruptedJson_returnsEmptyList() = runTest {
        val repo = AccountRepositoryImpl(createFakeStorage("not valid json"))
        val accounts = repo.observeAccounts().first()
        assertTrue(accounts.isEmpty())
    }

    @Test
    fun addAccount_multipleAccounts_allPersisted() = runTest {
        val storage = createFakeStorage()
        val repo = AccountRepositoryImpl(storage)

        repo.addAccount(createAccount(id = "1", issuer = "Google"))
        repo.addAccount(createAccount(id = "2", issuer = "GitHub"))
        repo.addAccount(createAccount(id = "3", issuer = "AWS"))

        val accounts = repo.observeAccounts().first()
        assertEquals(3, accounts.size)
    }

    @Test
    fun incrementCounter_updatesCounterAndPersists() = runTest {
        val storage = createFakeStorage()
        val repo = AccountRepositoryImpl(storage)

        val account = OtpAccount(
            id = "1",
            issuer = "YubiKey",
            accountName = "user",
            secret = "JBSWY3DPEHPK3PXP",
            type = OtpType.HOTP,
            counter = 0
        )
        repo.addAccount(account)
        repo.incrementCounter("1")

        val accounts = repo.observeAccounts().first()
        assertEquals(1, accounts.size)
        assertEquals(1L, accounts[0].counter)
        assertTrue(storage.savedJson!!.contains("\"counter\":1"))
    }
}

private class FakeAccountStorage(private val initialJson: String? = null) : AccountStorage {
    var savedJson: String? = null
        private set

    override fun loadRawJson(): String? = savedJson ?: initialJson

    override fun saveRawJson(json: String) {
        savedJson = json
    }
}
