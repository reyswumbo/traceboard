package com.traceboard.app.data.util

import java.util.Locale

object TimeFormatter {

    fun format(durationMillis: Long): String {
        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return buildString {
            if (hours > 0) append("${hours}h ")
            append("${minutes}m ${seconds}s")
        }
    }

    fun formatPrecise(durationMillis: Long): String {
        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return when {
            hours > 0 -> "$hours jam $minutes menit $seconds detik"
            minutes > 0 -> "$minutes menit $seconds detik"
            else -> "$seconds detik"
        }
    }

    fun formatCompact(durationMillis: Long): String {
        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    fun formatNumber(value: Long): String = String.format(Locale.US, "%,d", value)
}
