package com.maoungedev.beauthy.di

import com.maoungedev.beauthy.core.crypto.HmacProvider
import com.maoungedev.beauthy.core.crypto.IosHmacProvider
import com.maoungedev.beauthy.core.time.IosTimeProvider
import com.maoungedev.beauthy.core.time.TimeProvider
import com.maoungedev.beauthy.data.storage.AccountStorage
import com.maoungedev.beauthy.data.storage.SecureFileAccountStorage
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    singleOf(::IosHmacProvider) bind HmacProvider::class
    singleOf(::IosTimeProvider) bind TimeProvider::class
    singleOf(::SecureFileAccountStorage) bind AccountStorage::class
}
