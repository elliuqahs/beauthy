package com.maoungedev.beauthy.presentation.add

import com.maoungedev.beauthy.core.time.TimeProvider
import com.maoungedev.beauthy.domain.model.OtpAccount
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class AddAccountScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeAccountRepository
    private lateinit var screenModel: AddAccountScreenModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeAccountRepository()
        screenModel = AddAccountScreenModel(
            repository = fakeRepo,
            timeProvider = object : TimeProvider {
                override fun currentTimeMillis(): Long = 1000L
            }
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitIntent_validInput_emitsSuccess() = runTest {
        screenModel.onIntent(AddAccountIntent.Submit("Google", "user@gmail.com", "JBSWY3DPEHPK3PXP"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertIs<AddAccountSideEffect.Success>(screenModel.sideEffect.value)
        assertNotNull(fakeRepo.lastAdded)
        assertEquals("Google", fakeRepo.lastAdded!!.issuer)
    }

    @Test
    fun submitIntent_emptyIssuer_emitsValidationError() = runTest {
        screenModel.onIntent(AddAccountIntent.Submit("", "user@gmail.com", "JBSWY3DPEHPK3PXP"))
        testDispatcher.scheduler.advanceUntilIdle()

        val effect = screenModel.sideEffect.value
        assertIs<AddAccountSideEffect.ValidationError>(effect)
        assertEquals(ValidationErrorType.ISSUER_EMPTY, effect.type)
    }

    @Test
    fun submitIntent_emptyAccountName_emitsValidationError() = runTest {
        screenModel.onIntent(AddAccountIntent.Submit("Google", "", "JBSWY3DPEHPK3PXP"))
        testDispatcher.scheduler.advanceUntilIdle()

        val effect = screenModel.sideEffect.value
        assertIs<AddAccountSideEffect.ValidationError>(effect)
        assertEquals(ValidationErrorType.ACCOUNT_EMPTY, effect.type)
    }

    @Test
    fun submitIntent_emptySecret_emitsValidationError() = runTest {
        screenModel.onIntent(AddAccountIntent.Submit("Google", "user@gmail.com", ""))
        testDispatcher.scheduler.advanceUntilIdle()

        val effect = screenModel.sideEffect.value
        assertIs<AddAccountSideEffect.ValidationError>(effect)
        assertEquals(ValidationErrorType.SECRET_EMPTY, effect.type)
    }

    @Test
    fun submitIntent_shortSecret_emitsValidationError() = runTest {
        screenModel.onIntent(AddAccountIntent.Submit("Google", "user@gmail.com", "ABCD"))
        testDispatcher.scheduler.advanceUntilIdle()

        val effect = screenModel.sideEffect.value
        assertIs<AddAccountSideEffect.ValidationError>(effect)
        assertEquals(ValidationErrorType.SECRET_SHORT, effect.type)
    }

    @Test
    fun submitIntent_invalidBase32_emitsValidationError() = runTest {
        screenModel.onIntent(AddAccountIntent.Submit("Google", "user@gmail.com", "INVALID!KEY!12345"))
        testDispatcher.scheduler.advanceUntilIdle()

        val effect = screenModel.sideEffect.value
        assertIs<AddAccountSideEffect.ValidationError>(effect)
        assertEquals(ValidationErrorType.SECRET_INVALID, effect.type)
    }

    @Test
    fun submitIntent_trimsAndUppercasesSecret() = runTest {
        screenModel.onIntent(AddAccountIntent.Submit("Google", "user@gmail.com", "  jbswy3dp ehpk3pxp  "))
        testDispatcher.scheduler.advanceUntilIdle()

        assertIs<AddAccountSideEffect.Success>(screenModel.sideEffect.value)
        assertEquals("JBSWY3DPEHPK3PXP", fakeRepo.lastAdded!!.secret)
    }

    @Test
    fun consumeEventIntent_setsToIdle() {
        screenModel.onIntent(AddAccountIntent.Submit("", "", ""))
        assertIs<AddAccountSideEffect.ValidationError>(screenModel.sideEffect.value)

        screenModel.onIntent(AddAccountIntent.ConsumeEvent)
        assertIs<AddAccountSideEffect.Idle>(screenModel.sideEffect.value)
    }
}

private class FakeAccountRepository : AccountRepository {
    private val _accounts = MutableStateFlow<List<OtpAccount>>(emptyList())
    var lastAdded: OtpAccount? = null
        private set

    override fun observeAccounts(): Flow<List<OtpAccount>> = _accounts
    override suspend fun addAccount(account: OtpAccount) {
        lastAdded = account
        _accounts.value = _accounts.value + account
    }
    override suspend fun deleteAccount(id: String) {
        _accounts.value = _accounts.value.filter { it.id != id }
    }
    override suspend fun incrementCounter(id: String) {
        _accounts.value = _accounts.value.map { account ->
            if (account.id == id) account.copy(counter = account.counter + 1)
            else account
        }
    }
}
