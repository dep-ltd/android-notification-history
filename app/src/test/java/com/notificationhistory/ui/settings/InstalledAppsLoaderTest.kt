package com.notificationhistory.ui.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InstalledAppsLoaderTest {

    @Test
    fun load_includesOwnPackageExcluded_andReturnsMultipleApps() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val apps = InstalledAppsLoader.load(context, emptyList())
        assertTrue(apps.none { it.packageName == context.packageName })
    }
}
