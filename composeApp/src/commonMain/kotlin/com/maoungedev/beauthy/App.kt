package com.maoungedev.beauthy

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.maoungedev.beauthy.di.commonModule
import com.maoungedev.beauthy.di.koinPlatformConfig
import com.maoungedev.beauthy.di.platformModule
import com.maoungedev.beauthy.presentation.splash.SplashScreen
import org.koin.compose.KoinApplication

@Composable
fun App() {
    val platformConfig = koinPlatformConfig()
    KoinApplication(application = {
        platformConfig()
        modules(commonModule, platformModule())
    }) {
        MaterialTheme {
            Navigator(SplashScreen()) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
