package io.github.elliuqahs.beauthy

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.kCCHmacAlgSHA1
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.CoreCrypto.kCCHmacAlgSHA512
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
internal actual fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray {
    val ccAlg = when (algorithm) {
        HmacAlgorithm.SHA1 -> kCCHmacAlgSHA1
        HmacAlgorithm.SHA256 -> kCCHmacAlgSHA256
        HmacAlgorithm.SHA512 -> kCCHmacAlgSHA512
    }
    val result = ByteArray(algorithm.digestLength)
    key.usePinned { keyPin ->
        data.usePinned { dataPin ->
            result.usePinned { resultPin ->
                CCHmac(
                    ccAlg,
                    if (key.isEmpty()) null else keyPin.addressOf(0),
                    key.size.toULong(),
                    if (data.isEmpty()) null else dataPin.addressOf(0),
                    data.size.toULong(),
                    resultPin.addressOf(0)
                )
            }
        }
    }
    return result
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun secureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    if (size == 0) return bytes
    val status = bytes.usePinned { pin ->
        SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pin.addressOf(0))
    }
    check(status == errSecSuccess) { "SecRandomCopyBytes failed with status $status" }
    return bytes
}

internal actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()
