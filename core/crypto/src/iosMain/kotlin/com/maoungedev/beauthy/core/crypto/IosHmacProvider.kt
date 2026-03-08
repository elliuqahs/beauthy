package com.maoungedev.beauthy.core.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA1_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA1

class IosHmacProvider : HmacProvider {

    @OptIn(ExperimentalForeignApi::class)
    override fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val result = ByteArray(CC_SHA1_DIGEST_LENGTH)
        key.usePinned { keyPin ->
            data.usePinned { dataPin ->
                result.usePinned { resultPin ->
                    CCHmac(
                        kCCHmacAlgSHA1,
                        keyPin.addressOf(0),
                        key.size.toULong(),
                        dataPin.addressOf(0),
                        data.size.toULong(),
                        resultPin.addressOf(0)
                    )
                }
            }
        }
        return result
    }
}
