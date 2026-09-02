package com.traceboard.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_words")
data class TrackedWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    var count: Int = 0
)
