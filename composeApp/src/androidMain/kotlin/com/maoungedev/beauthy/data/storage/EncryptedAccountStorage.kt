package com.maoungedev.beauthy.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedAccountStorage(context: Context) : AccountStorage {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        migrateFromPlainStorage(context)
    }

    override fun loadRawJson(): String? = prefs.getString(KEY_ACCOUNTS, null)

    override fun saveRawJson(json: String) {
        prefs.edit().putString(KEY_ACCOUNTS, json).apply()
    }

    private fun migrateFromPlainStorage(context: Context) {
        val oldPrefs = context.getSharedPreferences(OLD_PREFS_NAME, Context.MODE_PRIVATE)
        val oldData = oldPrefs.getString(OLD_KEY_ACCOUNTS, null)
        if (oldData != null) {
            if (loadRawJson() == null) {
                saveRawJson(oldData)
            }
            oldPrefs.edit().clear().apply()
        }
    }

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "beauthy_secure_accounts"
        private const val KEY_ACCOUNTS = "accounts"
        private const val OLD_PREFS_NAME = "otp_accounts"
        private const val OLD_KEY_ACCOUNTS = "accounts"
    }
}
