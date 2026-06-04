package com.depsoftware.notifhistory.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSessionManager @Inject constructor() {
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private var lastUnlockAt: Long = 0L
    private var lockTimeoutMinutes: Int = 0

    fun setLockTimeoutMinutes(minutes: Int) {
        lockTimeoutMinutes = minutes.coerceIn(0, 60)
    }

    fun unlock() {
        _isUnlocked.value = true
        lastUnlockAt = System.currentTimeMillis()
    }

    fun lock() {
        _isUnlocked.value = false
        lastUnlockAt = 0L
    }

    fun lockIfBackgrounded() {
        if (lockTimeoutMinutes == 0) {
            lock()
        }
    }

    fun lockIfTimedOut() {
        if (!_isUnlocked.value) return
        if (lockTimeoutMinutes <= 0) return
        val elapsed = System.currentTimeMillis() - lastUnlockAt
        if (elapsed > lockTimeoutMinutes * 60_000L) {
            lock()
        }
    }
}
