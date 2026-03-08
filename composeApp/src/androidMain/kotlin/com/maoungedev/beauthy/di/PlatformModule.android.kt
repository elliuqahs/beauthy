package com.maoungedev.beauthy.di

import com.maoungedev.beauthy.core.crypto.HmacProvider
import com.maoungedev.beauthy.core.crypto.JvmHmacProvider
import com.maoungedev.beauthy.core.time.SystemTimeProvider
import com.maoungedev.beauthy.core.time.TimeProvider
import com.maoungedev.beauthy.data.storage.AccountStorage
import com.maoungedev.beauthy.data.storage.EncryptedAccountStorage
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    singleOf(::JvmHmacProvider) bind HmacProvider::class
    singleOf(::SystemTimeProvider) bind TimeProvider::class
    single<AccountStorage> { EncryptedAccountStorage(get()) }
}
