package io.github.elliuqahs.beauthy.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import io.github.elliuqahs.beauthy.HmacAlgorithm
import io.github.elliuqahs.beauthy.Totp
import io.github.elliuqahs.beauthy.TotpCode
import io.github.elliuqahs.beauthy.coroutines.codes

/**
 * The current code of [totp] as Compose state, updated at the start of every second
 * while this call stays in the composition.
 *
 * Updates restart only when [totp] changes. [Totp] instances with the same settings are
 * equal, so creating one inline, as in `rememberTotpCode(Totp(secret))`, is safe.
 *
 * ```kotlin
 * @Composable
 * fun AccountRow(totp: Totp) {
 *     val current by rememberTotpCode(totp)
 *     Text(current.formatted())
 *     Text("${current.remainingSeconds}s")
 *     LinearProgressIndicator(progress = { current.progress })
 * }
 * ```
 */
@Composable
public fun rememberTotpCode(totp: Totp): State<TotpCode> {
    val initial = remember(totp) { totp.current() }
    return produceState(initialValue = initial, totp) {
        totp.codes().collect { value = it }
    }
}

/**
 * Like `rememberTotpCode(Totp(...))`, creating the [Totp] once for these parameters.
 *
 * @throws IllegalArgumentException if [secret] is not valid Base32, or [digits] or [period]
 *   is out of range. Validate user input with `Base32.isValid` before composing.
 */
@Composable
public fun rememberTotpCode(
    secret: String,
    algorithm: HmacAlgorithm = HmacAlgorithm.SHA1,
    digits: Int = 6,
    period: Int = 30,
    clockOffsetMillis: Long = 0
): State<TotpCode> {
    val totp = remember(secret, algorithm, digits, period, clockOffsetMillis) {
        Totp(secret, algorithm, digits, period, clockOffsetMillis)
    }
    return rememberTotpCode(totp)
}
