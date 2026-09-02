package com.traceboard.app.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.provider.Settings
import com.traceboard.app.data.model.AppUsage

class UsageRepository(private val context: Context) {

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    fun getAppUsage(periodMillis: Long): List<AppUsage> {
        if (!hasUsagePermission()) return emptyList()
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - periodMillis
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end)
            ?: return emptyList()
        val pm = context.packageManager

        return stats
            .filter { it.totalTimeInForeground > 0 || it.totalTimeVisible > 0 }
            .map {
                AppUsage(
                    packageName = it.packageName,
                    appName = appLabel(pm, it.packageName),
                    totalTime = if (it.totalTimeVisible > 0) it.totalTimeVisible else it.totalTimeInForeground,
                    lastTimeUsed = it.lastTimeUsed
                )
            }
            .sortedByDescending { it.totalTime }
    }

    fun getSessionTime(packageName: String): Long {
        if (!hasUsagePermission()) return 0L
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 5 * 60 * 60 * 1000L,
            System.currentTimeMillis()
        ) ?: return 0L
        return stats.filter { it.packageName == packageName }
            .sumOf { if (it.totalTimeVisible > 0) it.totalTimeVisible else it.totalTimeInForeground }
    }

    fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return -1
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun getStorageInfo(): StorageInfo {
        val dataPath = Environment.getDataDirectory()
        val stat = StatFs(dataPath.path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return StorageInfo(total, free)
    }

    private fun appLabel(pm: PackageManager, pkg: String): String {
        return runCatching {
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info).toString()
        }.getOrDefault(pkg)
    }
}

data class StorageInfo(
    val totalBytes: Long,
    val freeBytes: Long
)
