package com.traceboard.app.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.traceboard.app.data.model.TrackedWord
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedWordDao {
    @Insert
    suspend fun insert(word: TrackedWord)

    @Update
    suspend fun update(word: TrackedWord)

    @Delete
    suspend fun delete(word: TrackedWord)

    @Query("SELECT * FROM tracked_words ORDER BY count DESC")
    fun getAll(): Flow<List<TrackedWord>>

    @Query("UPDATE tracked_words SET count = 0")
    suspend fun resetAllCounts()
}
