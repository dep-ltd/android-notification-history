package com.notificationhistory.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.notificationhistory.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        SECURE_PREFS_NAME_PRIVATE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun hasDatabasePassphrase(): Boolean =
        securePrefs.contains(KEY_DB_PASSPHRASE) || debugPassphraseBytes() != null

    fun ensureDatabasePassphrase() {
        if (securePrefs.contains(KEY_DB_PASSPHRASE)) return
        val bytes = ByteArray(PASSPHRASE_SIZE).also { SecureRandom().nextBytes(it) }
        securePrefs.edit()
            .putString(KEY_DB_PASSPHRASE, Base64.encodeToString(bytes, Base64.NO_WRAP))
            .apply()
    }

    fun getDatabasePassphraseBytes(): ByteArray {
        debugPassphraseBytes()?.let { return it }
        val encoded = securePrefs.getString(KEY_DB_PASSPHRASE, null)
            ?: throw IllegalStateException("Database passphrase is missing")
        return Base64.decode(encoded, Base64.NO_WRAP)
    }

    fun clearSecureStorage() {
        securePrefs.edit().clear().apply()
    }

    private fun debugPassphraseBytes(): ByteArray? {
        if (!BuildConfig.DEBUG) return null
        val key = BuildConfig.DEBUG_DB_ENCRYPTION_KEY
        if (key.isEmpty()) return null
        return try {
            Base64.decode(key, Base64.NO_WRAP)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    companion object {
        const val SECURE_PREFS_NAME = "secure_storage"
        private const val SECURE_PREFS_NAME_PRIVATE = SECURE_PREFS_NAME
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
        private const val PASSPHRASE_SIZE = 32

        @Volatile
        private var sqlCipherLoaded = false

        fun loadSqlCipher() {
            if (!sqlCipherLoaded) {
                System.loadLibrary("sqlcipher")
                sqlCipherLoaded = true
            }
        }
    }
}
