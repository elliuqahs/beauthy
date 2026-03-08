package com.maoungedev.beauthy.presentation.list

import com.maoungedev.beauthy.core.crypto.HmacProvider
import com.maoungedev.beauthy.core.crypto.TotpGenerator
import com.maoungedev.beauthy.core.time.TimeProvider
import com.maoungedev.beauthy.domain.model.OtpAccount
import com.maoungedev.beauthy.domain.model.OtpType
import com.maoungedev.beauthy.domain.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountListScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeAccountRepository
    private lateinit var fakeTimeProvider: FakeTimeProvider
    private lateinit var screenModel: AccountListScreenModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeAccountRepository()
        fakeTimeProvider = FakeTimeProvider(fixedTime = 15_000L)
        val totpGenerator = TotpGenerator(FakeHmacProvider())
        screenModel = AccountListScreenModel(fakeRepo, totpGenerator, fakeTimeProvider)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_emptyAccounts() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = screenModel.state.value
        assertTrue(state.accounts.isEmpty())
    }

    @Test
    fun whenAccountsEmitted_stateUpdates() = runTest {
        val account = OtpAccount(
            id = "1",
            issuer = "Google",
            accountName = "user@gmail.com",
            secret = "JBSWY3DPEHPK3PXP"
        )
        fakeRepo.emit(listOf(account))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertEquals(1, state.accounts.size)
        assertEquals("Google", state.accounts[0].account.issuer)
        assertEquals(6, state.accounts[0].code.length)
    }

    @Test
    fun remainingSeconds_calculatedFromTimeProvider() = runTest {
        fakeRepo.emit(emptyList())
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertEquals(15, state.remainingSeconds)
    }

    @Test
    fun deleteIntent_callsRepository() = runTest {
        val account = OtpAccount("1", "Google", "user", "JBSWY3DPEHPK3PXP")
        fakeRepo.emit(listOf(account))
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onIntent(AccountListIntent.DeleteAccount("1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("1", fakeRepo.lastDeletedId)
    }

    @Test
    fun refreshHotpIntent_incrementsCounter() = runTest {
        val account = OtpAccount(
            id = "1",
            issuer = "YubiKey",
            accountName = "user",
            secret = "JBSWY3DPEHPK3PXP",
            type = OtpType.HOTP,
            counter = 0
        )
        fakeRepo.emit(listOf(account))
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.onIntent(AccountListIntent.RefreshHotp("1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("1", fakeRepo.lastIncrementedId)
    }

    @Test
    fun hotpAccount_generatesCodeFromCounter() = runTest {
        val account = OtpAccount(
            id = "1",
            issuer = "YubiKey",
            accountName = "user",
            secret = "JBSWY3DPEHPK3PXP",
            type = OtpType.HOTP,
            counter = 5
        )
        fakeRepo.emit(listOf(account))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertEquals(1, state.accounts.size)
        assertEquals(OtpType.HOTP, state.accounts[0].account.type)
        assertEquals(6, state.accounts[0].code.length)
    }
}

private class FakeAccountRepository : AccountRepository {
    private val _accounts = MutableStateFlow<List<OtpAccount>>(emptyList())
    var lastDeletedId: String? = null
        private set
    var lastIncrementedId: String? = null
        private set

    fun emit(accounts: List<OtpAccount>) {
        _accounts.value = accounts
    }

    override fun observeAccounts(): Flow<List<OtpAccount>> = _accounts
    override suspend fun addAccount(account: OtpAccount) {
        _accounts.value = _accounts.value + account
    }
    override suspend fun deleteAccount(id: String) {
        lastDeletedId = id
        _accounts.value = _accounts.value.filter { it.id != id }
    }
    override suspend fun incrementCounter(id: String) {
        lastIncrementedId = id
        _accounts.value = _accounts.value.map { account ->
            if (account.id == id) account.copy(counter = account.counter + 1)
            else account
        }
    }
}

private class FakeTimeProvider(private val fixedTime: Long) : TimeProvider {
    override fun currentTimeMillis(): Long = fixedTime
}

private class FakeHmacProvider : HmacProvider {
    override fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        return ByteArray(20) { (it + 1).toByte() }
    }
}
