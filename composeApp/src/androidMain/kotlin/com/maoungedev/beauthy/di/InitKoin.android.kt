package com.maoungedev.beauthy.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

@Composable
actual fun koinPlatformConfig(): KoinApplication.() -> Unit {
    val context = LocalContext.current.applicationContext
    return { androidContext(context) }
}
