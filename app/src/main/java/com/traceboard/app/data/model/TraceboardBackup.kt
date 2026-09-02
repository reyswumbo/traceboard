package com.traceboard.app.data.model

data class TraceboardBackup(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val clipboardEntries: List<ClipboardItem>? = emptyList()
)