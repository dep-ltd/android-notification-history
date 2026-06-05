package com.depsoftware.notifhistory.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationIntentExtractorTest {

    @Test
    fun extractContentIntentUri_serializesContentIntent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val targetIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/in-app"))
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            targetIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, "test")
            .setContentTitle("Title")
            .setContentText("Body")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()

        val uri = NotificationIntentExtractor.extractContentIntentUri(notification)

        assertNotNull(uri)
        assertTrue(uri!!.contains("example.com"))
    }
}
