package com.maoungedev.beauthy.di

import androidx.compose.runtime.Composable
import org.koin.core.KoinApplication

@Composable
expect fun koinPlatformConfig(): KoinApplication.() -> Unit
