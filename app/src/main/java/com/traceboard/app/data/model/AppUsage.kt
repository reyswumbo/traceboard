package com.traceboard.app.data.model

data class AppUsage(
    val packageName: String,
    val appName: String,
    val totalTime: Long,
    val lastTimeUsed: Long
)
