package com.maoungedev.beauthy.core.clipboard

import platform.UIKit.UIPasteboard

class IosClipboardService : ClipboardService {

    override fun copy(text: String, label: String) {
        UIPasteboard.generalPasteboard.string = text
    }

    override fun clear() {
        UIPasteboard.generalPasteboard.string = ""
    }
}
