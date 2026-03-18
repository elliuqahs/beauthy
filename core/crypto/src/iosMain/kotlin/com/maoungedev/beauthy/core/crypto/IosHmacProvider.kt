package com.maoungedev.beauthy.core.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA1_DIGEST_LENGTH
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.CC_SHA512_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA1
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.CoreCrypto.kCCHmacAlgSHA512

public class IosHmacProvider : HmacProvider {

    @OptIn(ExperimentalForeignApi::class)
    override fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray {
        val (ccAlg, digestLength) = when (algorithm) {
            HmacAlgorithm.SHA1 -> kCCHmacAlgSHA1 to CC_SHA1_DIGEST_LENGTH
            HmacAlgorithm.SHA256 -> kCCHmacAlgSHA256 to CC_SHA256_DIGEST_LENGTH
            HmacAlgorithm.SHA512 -> kCCHmacAlgSHA512 to CC_SHA512_DIGEST_LENGTH
        }

        val result = ByteArray(digestLength)
        key.usePinned { keyPin ->
            data.usePinned { dataPin ->
                result.usePinned { resultPin ->
                    CCHmac(
                        ccAlg,
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
