package com.maoungedev.beauthy.presentation.list

import com.maoungedev.beauthy.core.clipboard.ClipboardService
import com.maoungedev.beauthy.core.crypto.HmacAlgorithm
import com.maoungedev.beauthy.core.crypto.HmacProvider
import com.maoungedev.beauthy.core.crypto.TotpGenerator
import com.maoungedev.beauthy.core.time.TimeProvider
import com.maoungedev.beauthy.domain.model.OtpAccount
import com.maoungedev.beauthy.domain.model.OtpType
import com.maoungedev.beauthy.domain.repository.AccountRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountListScreenModelTest {

    private fun screenModelTest(
        fixedTime: Long = 15_000L,
        block: suspend TestScope.(FakeAccountRepository, AccountListScreenModel, FakeClipboardService) -> Unit
    ) = runTest {
        val fakeRepo = FakeAccountRepository()
        val fakeTimeProvider = FakeTimeProvider(fixedTime)
        val fakeClipboard = FakeClipboardService()
        val totpGenerator = TotpGenerator(FakeHmacProvider())
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val screenModel = AccountListScreenModel(
            repository = fakeRepo,
            totpGenerator = totpGenerator,
            timeProvider = fakeTimeProvider,
            clipboardService = fakeClipboard,
            scope = testScope
        )
        testScheduler.runCurrent()
        block(fakeRepo, screenModel, fakeClipboard)
        testScope.cancel()
    }

    @Test
    fun initialState_emptyAccounts() = screenModelTest { _, screenModel, _ ->
        val state = screenModel.state.value
        assertTrue(state.accounts.isEmpty())
    }

    @Test
    fun whenAccountsEmitted_stateUpdates() = screenModelTest { fakeRepo, screenModel, _ ->
        val account = OtpAccount(
            id = "1",
            issuer = "Google",
            accountName = "user@gmail.com",
            secret = "JBSWY3DPEHPK3PXP"
        )
        fakeRepo.emit(listOf(account))
        testScheduler.runCurrent()

        val state = screenModel.state.value
        assertEquals(1, state.accounts.size)
        assertEquals("Google", state.accounts[0].account.issuer)
        assertEquals(6, state.accounts[0].code.length)
    }

    @Test
    fun remainingSeconds_calculatedFromTimeProvider() = screenModelTest { fakeRepo, screenModel, _ ->
        fakeRepo.emit(emptyList())
        testScheduler.runCurrent()

        val state = screenModel.state.value
        assertEquals(15, state.remainingSeconds)
    }

    @Test
    fun deleteIntent_callsRepository() = screenModelTest { fakeRepo, screenModel, _ ->
        val account = OtpAccount("1", "Google", "user", "JBSWY3DPEHPK3PXP")
        fakeRepo.emit(listOf(account))
        testScheduler.runCurrent()

        screenModel.onIntent(AccountListIntent.DeleteAccount("1"))
        testScheduler.runCurrent()

        assertEquals("1", fakeRepo.lastDeletedId)
    }

    @Test
    fun refreshHotpIntent_incrementsCounter() = screenModelTest { fakeRepo, screenModel, _ ->
        val account = OtpAccount(
            id = "1",
            issuer = "YubiKey",
            accountName = "user",
            secret = "JBSWY3DPEHPK3PXP",
            type = OtpType.HOTP,
            counter = 0
        )
        fakeRepo.emit(listOf(account))
        testScheduler.runCurrent()

        screenModel.onIntent(AccountListIntent.RefreshHotp("1"))
        testScheduler.runCurrent()

        assertEquals("1", fakeRepo.lastIncrementedId)
    }

    @Test
    fun hotpAccount_generatesCodeFromCounter() = screenModelTest { fakeRepo, screenModel, _ ->
        val account = OtpAccount(
            id = "1",
            issuer = "YubiKey",
            accountName = "user",
            secret = "JBSWY3DPEHPK3PXP",
            type = OtpType.HOTP,
            counter = 5
        )
        fakeRepo.emit(listOf(account))
        testScheduler.runCurrent()

        val state = screenModel.state.value
        assertEquals(1, state.accounts.size)
        assertEquals(OtpType.HOTP, state.accounts[0].account.type)
        assertEquals(6, state.accounts[0].code.length)
    }

    @Test
    fun copyCode_copiesToClipboard() = screenModelTest { _, screenModel, fakeClipboard ->
        screenModel.onIntent(AccountListIntent.CopyCode("123 456"))
        testScheduler.runCurrent()

        assertEquals("123456", fakeClipboard.lastCopied)
        assertEquals(AccountListSideEffect.CodeCopied, screenModel.sideEffect.value)
    }

    @Test
    fun searchQuery_filtersAccounts() = screenModelTest { fakeRepo, screenModel, _ ->
        fakeRepo.emit(listOf(
            OtpAccount("1", "Google", "user@gmail.com", "JBSWY3DPEHPK3PXP"),
            OtpAccount("2", "GitHub", "dev@github.com", "JBSWY3DPEHPK3PXP"),
            OtpAccount("3", "Amazon", "user@amazon.com", "JBSWY3DPEHPK3PXP")
        ))
        testScheduler.runCurrent()

        screenModel.onIntent(AccountListIntent.SearchQuery("git"))
        val state = screenModel.state.value
        assertEquals(1, state.accounts.size)
        assertEquals("GitHub", state.accounts[0].account.issuer)
        assertEquals("git", state.searchQuery)
    }

    @Test
    fun toggleSort_cyclesThroughSortOrders() = screenModelTest { fakeRepo, screenModel, _ ->
        fakeRepo.emit(listOf(
            OtpAccount("1", "Google", "user", "JBSWY3DPEHPK3PXP"),
            OtpAccount("2", "Amazon", "user", "JBSWY3DPEHPK3PXP"),
            OtpAccount("3", "GitHub", "user", "JBSWY3DPEHPK3PXP")
        ))
        testScheduler.runCurrent()

        // First toggle: ISSUER_ASC
        screenModel.onIntent(AccountListIntent.ToggleSort)
        var state = screenModel.state.value
        assertEquals(SortOrder.ISSUER_ASC, state.sortOrder)
        assertEquals("Amazon", state.accounts[0].account.issuer)
        assertEquals("GitHub", state.accounts[1].account.issuer)
        assertEquals("Google", state.accounts[2].account.issuer)

        // Second toggle: ISSUER_DESC
        screenModel.onIntent(AccountListIntent.ToggleSort)
        state = screenModel.state.value
        assertEquals(SortOrder.ISSUER_DESC, state.sortOrder)
        assertEquals("Google", state.accounts[0].account.issuer)

        // Third toggle: NONE (original order)
        screenModel.onIntent(AccountListIntent.ToggleSort)
        state = screenModel.state.value
        assertEquals(SortOrder.NONE, state.sortOrder)
    }

    @Test
    fun consumeSideEffect_resetsToIdle() = screenModelTest { _, screenModel, _ ->
        screenModel.onIntent(AccountListIntent.CopyCode("123456"))
        testScheduler.runCurrent()
        assertEquals(AccountListSideEffect.CodeCopied, screenModel.sideEffect.value)

        screenModel.onIntent(AccountListIntent.ConsumeSideEffect)
        assertEquals(AccountListSideEffect.Idle, screenModel.sideEffect.value)
    }
}

private class FakeClipboardService : ClipboardService {
    var lastCopied: String? = null
        private set
    var cleared: Boolean = false
        private set

    override fun copy(text: String, label: String) {
        lastCopied = text
    }

    override fun clear() {
        cleared = true
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
        _accounts.value += account
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
    override fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray {
        val size = when (algorithm) {
            HmacAlgorithm.SHA1 -> 20
            HmacAlgorithm.SHA256 -> 32
            HmacAlgorithm.SHA512 -> 64
        }
        return ByteArray(size) { (it + 1).toByte() }
    }
}
