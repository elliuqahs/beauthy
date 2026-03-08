package com.maoungedev.beauthy.data.storage

interface AccountStorage {
    fun loadRawJson(): String?
    fun saveRawJson(json: String)
}
