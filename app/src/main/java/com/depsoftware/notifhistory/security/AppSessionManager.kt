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

    private var lastPinUnlockAt: Long = 0L
    private var pinReentryHours: Int = DEFAULT_PIN_REENTRY_HOURS

    fun setPinReentryHours(hours: Int) {
        pinReentryHours = hours.coerceIn(0, MAX_PIN_REENTRY_HOURS)
    }

    fun requiresPinReentry(): Boolean {
        if (pinReentryHours <= 0) return false
        if (lastPinUnlockAt == 0L) return true
        val elapsed = System.currentTimeMillis() - lastPinUnlockAt
        return elapsed > pinReentryHours * 3_600_000L
    }

    fun unlockWithPin() {
        _isUnlocked.value = true
        lastPinUnlockAt = System.currentTimeMillis()
    }

    fun unlockWithBiometric() {
        if (requiresPinReentry()) return
        _isUnlocked.value = true
    }

    fun lock() {
        _isUnlocked.value = false
    }

    fun lockIfBackgrounded() {
        lock()
    }

    companion object {
        const val DEFAULT_PIN_REENTRY_HOURS = 48
        const val MAX_PIN_REENTRY_HOURS = 48
        val PIN_REENTRY_OPTIONS = listOf(0, 1, 8, 24, 48)
    }
}
