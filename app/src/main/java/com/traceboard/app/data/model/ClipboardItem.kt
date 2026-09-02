package com.traceboard.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_items")
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val textLength: Int,
    val wordCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
