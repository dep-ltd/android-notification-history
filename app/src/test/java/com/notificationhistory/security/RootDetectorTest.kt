package com.notificationhistory.security

import org.junit.Assert.assertFalse
import org.junit.Test

class RootDetectorTest {

    @Test
    fun rootChecks_doNotThrow_onJvm() {
        // Heuristic checks return false on a typical dev machine / CI JVM.
        assertFalse(RootDetector.isRooted())
    }
}
