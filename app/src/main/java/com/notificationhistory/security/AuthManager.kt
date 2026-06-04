package com.notificationhistory.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.notificationhistory.data.DatabaseHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityManager: SecurityManager,
    private val databaseHolder: DatabaseHolder,
    private val wipeAllDataUseCase: WipeAllDataUseCase,
    private val sessionManager: AppSessionManager
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _requiresWipeScreen = MutableStateFlow(false)
    val requiresWipeScreen: StateFlow<Boolean> = _requiresWipeScreen.asStateFlow()

    private val authPrefs = createAuthSecurePrefs()

    fun getRemainingAttempts(): Int {
        val failed = authPrefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        return MAX_ATTEMPTS - failed
    }

    fun hasAcceptedWipePolicy(): Boolean = prefs.getBoolean(KEY_WIPE_CONSENT, false)

    fun acceptWipePolicy() {
        prefs.edit().putBoolean(KEY_WIPE_CONSENT, true).apply()
    }

    fun isPinSet(): Boolean = authPrefs.contains(KEY_PIN_HASH)

    fun isBiometricEnabled(): Boolean = authPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        authPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun setupPin(pin: String) {
        databaseHolder.recreateEncrypted()
        authPrefs.edit()
            .putString(KEY_PIN_HASH, hashPin(pin))
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .apply()
        sessionManager.unlock()
    }

    fun checkPin(enteredPin: String): Boolean {
        val storedHash = authPrefs.getString(KEY_PIN_HASH, null) ?: return false
        return if (storedHash == hashPin(enteredPin)) {
            resetAttempts()
            sessionManager.unlock()
            true
        } else {
            registerFailedAttempt()
            false
        }
    }

    fun registerBiometricFailure() {
        registerFailedAttempt()
    }

    fun registerBiometricSuccess() {
        resetAttempts()
        sessionManager.unlock()
    }

    fun acknowledgeWipeScreen() {
        _requiresWipeScreen.value = false
    }

    fun lockSession() {
        sessionManager.lock()
    }

    private fun registerFailedAttempt() {
        val current = authPrefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        authPrefs.edit().putInt(KEY_FAILED_ATTEMPTS, current).apply()
        sessionManager.lock()
        if (current >= MAX_ATTEMPTS) {
            performSecureWipe()
        }
    }

    private fun resetAttempts() {
        authPrefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).apply()
    }

    private fun performSecureWipe() {
        wipeAllDataUseCase()
        _requiresWipeScreen.value = true
        sessionManager.lock()
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun createAuthSecurePrefs(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                AUTH_SECURE_PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences(AUTH_SECURE_PREFS, Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        const val AUTH_SECURE_PREFS_NAME = "auth_secure_prefs"
        private const val AUTH_SECURE_PREFS = AUTH_SECURE_PREFS_NAME
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_WIPE_CONSENT = "wipe_policy_consent"
        const val MAX_ATTEMPTS = 10
    }
}
