@file:Suppress("DEPRECATION")

package com.maoungedev.beauthy.core.crypto

class IosHmacProviderTest : HmacProviderContractTest() {
    override val provider: HmacProvider = IosHmacProvider()
}
