package com.traceboard.app.data.util

import android.content.Context
import android.content.Intent
import android.provider.Settings

object AccessibilityUtils {

    val serviceComponent: String
        get() = "com.traceboard.app/com.traceboard.app.service.WritingAccessibilityService"

    fun isWritingServiceEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.equals(serviceComponent, ignoreCase = true) }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}