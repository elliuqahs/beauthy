package com.maoungedev.beauthy.di

import com.maoungedev.beauthy.core.crypto.TotpGenerator
import com.maoungedev.beauthy.data.repository.AccountRepositoryImpl
import com.maoungedev.beauthy.domain.repository.AccountRepository
import com.maoungedev.beauthy.presentation.add.AddAccountScreenModel
import com.maoungedev.beauthy.presentation.list.AccountListScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    singleOf(::TotpGenerator)
    singleOf(::AccountRepositoryImpl) bind AccountRepository::class
    factory { AccountListScreenModel(get(), get(), get(), get()) }
    factoryOf(::AddAccountScreenModel)
}
