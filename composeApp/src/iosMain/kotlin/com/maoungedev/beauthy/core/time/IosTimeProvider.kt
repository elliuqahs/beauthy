package com.maoungedev.beauthy.core.time

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSinceReferenceDate

class IosTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long {
        val sinceRef = NSDate().timeIntervalSinceReferenceDate
        return ((sinceRef + EPOCH_TO_REF_OFFSET) * 1000).toLong()
    }

    companion object {
        private const val EPOCH_TO_REF_OFFSET = 978307200.0
    }
}
