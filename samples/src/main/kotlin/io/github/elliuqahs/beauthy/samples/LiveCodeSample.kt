package io.github.elliuqahs.beauthy.samples

import io.github.elliuqahs.beauthy.Totp
import io.github.elliuqahs.beauthy.coroutines.codes
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking

/**
 * A live code with countdown using beauthy-sdk-coroutines. Prints three ticks.
 */
fun liveCodeSample() = runBlocking {
    Totp(secret = "JBSWY3DPEHPK3PXP").codes().take(3).collect { current ->
        println("${current.formatted()}  (${current.remainingSeconds}s)")
    }
}
