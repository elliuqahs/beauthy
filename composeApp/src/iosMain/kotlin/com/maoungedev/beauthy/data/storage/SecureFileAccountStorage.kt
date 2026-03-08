package com.maoungedev.beauthy.data.storage

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
class SecureFileAccountStorage : AccountStorage {

    init {
        ensureDirectoryExists()
        migrateFromUserDefaults()
    }

    override fun loadRawJson(): String? {
        val path = getFilePath()
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(path)) return null
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun saveRawJson(json: String) {
        val path = getFilePath()
        (json as NSString).writeToFile(path, true, NSUTF8StringEncoding, null)
        NSFileManager.defaultManager.setAttributes(
            mapOf<Any?, Any?>(NSFileProtectionKey to NSFileProtectionComplete),
            ofItemAtPath = path,
            error = null
        )
    }

    private fun getFilePath(): String {
        val dir = getAppSupportDirectory()
        return "$dir/$FILE_NAME"
    }

    private fun getAppSupportDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory,
            NSUserDomainMask,
            true
        )
        return paths.first() as String
    }

    private fun ensureDirectoryExists() {
        val dir = getAppSupportDirectory()
        NSFileManager.defaultManager.createDirectoryAtPath(
            dir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }

    private fun migrateFromUserDefaults() {
        val defaults = NSUserDefaults.standardUserDefaults
        val oldData = defaults.stringForKey(OLD_DEFAULTS_KEY)
        if (oldData != null) {
            if (loadRawJson() == null) {
                saveRawJson(oldData)
            }
            defaults.removeObjectForKey(OLD_DEFAULTS_KEY)
        }
    }

    companion object {
        private const val FILE_NAME = "beauthy_accounts.dat"
        private const val OLD_DEFAULTS_KEY = "otp_accounts"
    }
}
