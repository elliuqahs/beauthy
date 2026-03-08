package com.maoungedev.beauthy.di

import androidx.compose.runtime.Composable
import org.koin.core.KoinApplication

@Composable
actual fun koinPlatformConfig(): KoinApplication.() -> Unit = {}
