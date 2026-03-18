package com.maoungedev.beauthy.core.clipboard

interface ClipboardService {
    fun copy(text: String, label: String = "OTP")
    fun clear()
}
