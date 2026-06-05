package com.depsoftware.notifhistory.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.depsoftware.notifhistory.R

object NotificationLauncher {

    fun launchContentIntent(context: Context, intentUri: String, packageName: String? = null): Boolean {
        return try {
            val intent = Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (this.`package`.isNullOrBlank() && this.component == null && !packageName.isNullOrBlank()) {
                    setPackage(packageName)
                }
            }
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            showLaunchFailed(context)
            false
        } catch (_: SecurityException) {
            showLaunchFailed(context)
            false
        } catch (_: Exception) {
            showLaunchFailed(context)
            false
        }
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return run {
                showLaunchFailed(context)
                false
            }
        return try {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } catch (_: Exception) {
            showLaunchFailed(context)
            false
        }
    }

    private fun showLaunchFailed(context: Context) {
        Toast.makeText(context, R.string.action_launch_failed, Toast.LENGTH_SHORT).show()
    }
}
