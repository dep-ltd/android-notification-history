package com.depsoftware.notifhistory.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

internal object InstalledAppsLoader {

    fun load(
        context: Context,
        extraPackages: Collection<String> = emptyList()
    ): List<InstalledAppRow> {
        val pm = context.packageManager
        val ownPackage = context.packageName
        val byPackage = linkedMapOf<String, InstalledAppRow>()

        fun addPackage(packageName: String) {
            if (packageName == ownPackage || packageName in byPackage) return
            val label = resolveLabel(pm, packageName) ?: return
            byPackage[packageName] = InstalledAppRow(
                packageName = packageName,
                label = label,
                isBlacklisted = false
            )
        }

        for (info in queryInstalledApplications(pm)) {
            addPackage(info.packageName)
        }

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveFlags = resolveListFlags()
        pm.queryIntentActivities(launcherIntent, resolveFlags).forEach { resolve ->
            addPackage(resolve.activityInfo.packageName)
        }

        extraPackages.forEach { addPackage(it) }

        return byPackage.values.sortedBy { it.label.lowercase() }
    }

    private fun queryInstalledApplications(pm: PackageManager): List<ApplicationInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    PackageManager.MATCH_ALL.toLong() or
                        PackageManager.MATCH_DISABLED_COMPONENTS.toLong()
                )
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(
                PackageManager.GET_META_DATA or
                    PackageManager.MATCH_DISABLED_COMPONENTS
            )
        }
    }

    private fun resolveListFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.MATCH_ALL or PackageManager.MATCH_DISABLED_COMPONENTS
        } else {
            @Suppress("DEPRECATION")
            PackageManager.MATCH_DEFAULT_ONLY
        }
    }

    private fun resolveLabel(pm: PackageManager, packageName: String): String? {
        return try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            }
            pm.getApplicationLabel(info).toString().trim().takeIf { it.isNotEmpty() }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
