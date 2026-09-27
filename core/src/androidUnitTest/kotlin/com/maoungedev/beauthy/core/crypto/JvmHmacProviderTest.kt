@file:Suppress("DEPRECATION")

package com.maoungedev.beauthy.core.crypto

class JvmHmacProviderTest : HmacProviderContractTest() {
    override val provider: HmacProvider = JvmHmacProvider()
}
