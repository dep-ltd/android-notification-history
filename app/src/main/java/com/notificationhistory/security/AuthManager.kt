package com.notificationhistory.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityManager: SecurityManager
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val MAX_ATTEMPTS = 10

    fun getRemainingAttempts(): Int {
        val failed = prefs.getInt("failed_attempts", 0)
        return MAX_ATTEMPTS - failed
    }

    fun checkPin(enteredPin: String): Boolean {
        val storedHash = prefs.getString("pin_hash", null) ?: return false
        val enteredHash = hashPin(enteredPin)
        
        return if (storedHash == enteredHash) {
            resetAttempts()
            true
        } else {
            incrementAttempts()
            false
        }
    }

    fun setPin(pin: String) {
        prefs.edit().putString("pin_hash", hashPin(pin)).apply()
        resetAttempts()
    }

    fun isPinSet(): Boolean = prefs.contains("pin_hash")

    private fun incrementAttempts() {
        val current = prefs.getInt("failed_attempts", 0) + 1
        prefs.edit().putInt("failed_attempts", current).apply()
        if (current >= MAX_ATTEMPTS) {
            performSecureWipe()
        }
    }

    private fun resetAttempts() {
        prefs.edit().putInt("failed_attempts", 0).apply()
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun performSecureWipe() {
        // Delete Key
        securityManager.deleteKey()
        
        // Delete Database
        context.deleteDatabase("notification_history.db")
        
        // Delete Files
        context.filesDir.deleteRecursively()
        
        // Clear Prefs
        prefs.edit().clear().apply()
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
