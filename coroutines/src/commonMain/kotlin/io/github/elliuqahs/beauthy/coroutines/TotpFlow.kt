package io.github.elliuqahs.beauthy.coroutines

import io.github.elliuqahs.beauthy.Totp
import io.github.elliuqahs.beauthy.TotpCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A live TOTP code: emits [Totp.current] immediately and then at the start of every
 * second, so a countdown shown from [TotpCode.remainingSeconds] ticks in step with the
 * clock and the code changes as soon as a new period begins.
 *
 * ```kotlin
 * totp.codes().collect { current ->
 *     render(current.formatted(), current.remainingSeconds)
 * }
 * ```
 *
 * The flow is cold and never completes; collection stops when its coroutine is cancelled.
 */
public fun Totp.codes(): Flow<TotpCode> = tickingCodes { current() }

/**
 * Emits [snapshot] now and again at every following whole second of its
 * [TotpCode.timestampMillis].
 */
internal fun tickingCodes(snapshot: () -> TotpCode): Flow<TotpCode> = flow {
    while (true) {
        val code = snapshot()
        emit(code)
        delay(MILLIS_PER_SECOND - code.timestampMillis % MILLIS_PER_SECOND)
    }
}

private const val MILLIS_PER_SECOND = 1000L
