package com.traceboard.app.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.traceboard.app.data.model.ClipboardItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Insert
    suspend fun insert(item: ClipboardItem): Long

    @Update
    suspend fun update(item: ClipboardItem)

    @Delete
    suspend fun delete(item: ClipboardItem)

    @Query("SELECT * FROM clipboard_items WHERE folderId IS NULL ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items ORDER BY timestamp DESC")
    fun getAllAll(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE folderId = :folderId ORDER BY timestamp DESC")
    fun getForFolder(folderId: Long): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE folderId IS NULL AND text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE folderId = :folderId AND text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchInFolder(folderId: Long, query: String): Flow<List<ClipboardItem>>

    @Insert
    suspend fun insertAll(items: List<ClipboardItem>)

    @Query("DELETE FROM clipboard_items WHERE folderId IS NULL")
    suspend fun clearAll()

    @Query("DELETE FROM clipboard_items")
    suspend fun clear()

    @Query("DELETE FROM clipboard_items WHERE folderId = :folderId")
    suspend fun deleteByFolder(folderId: Long)
}