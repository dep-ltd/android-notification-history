package com.notificationhistory.service

import android.app.Application
import android.app.Notification
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.notificationhistory.util.MediaStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationParserTest {

    private val parser = NotificationParser(MediaStorage(ApplicationProvider.getApplicationContext<Application>()))

    @Test
    fun parseExtras_readsExtraTitleAndText() {
        val extras = Bundle().apply {
            putString(Notification.EXTRA_TITLE, "Test title")
            putCharSequence(Notification.EXTRA_TEXT, "Test body")
        }

        val fields = parser.parseExtras(extras)

        assertEquals("Test title", fields.title)
        assertEquals("Test body", fields.text)
    }

    @Test
    fun parseExtras_returnsNullWhenMissing() {
        val fields = parser.parseExtras(Bundle.EMPTY)

        assertNull(fields.title)
        assertNull(fields.text)
    }

    @Test
    fun stableKey_combinesPackageAndKey() {
        assertEquals(
            "com.example.app:0|com.example.app|123",
            NotificationParser.stableKey("com.example.app", "0|com.example.app|123")
        )
    }
}
