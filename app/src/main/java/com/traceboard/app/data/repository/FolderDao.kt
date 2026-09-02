package com.traceboard.app.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.traceboard.app.data.model.ClipboardFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert
    suspend fun insert(folder: ClipboardFolder): Long

    @Update
    suspend fun update(folder: ClipboardFolder)

    @Delete
    suspend fun delete(folder: ClipboardFolder)

    @Query("SELECT * FROM clipboard_folders ORDER BY createdAt")
    fun getAll(): Flow<List<ClipboardFolder>>
}