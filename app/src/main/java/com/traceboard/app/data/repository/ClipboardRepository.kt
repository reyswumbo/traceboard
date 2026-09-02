package com.traceboard.app.data.repository

import android.content.ClipboardManager
import android.content.Context
import com.traceboard.app.data.model.ClipboardFolder
import com.traceboard.app.data.model.ClipboardItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ClipboardRepository(private val context: Context) {

    private val dao = TraceboardDatabase.getInstance(context).clipboardDao()
    private val folderDao = TraceboardDatabase.getInstance(context).folderDao()

    suspend fun addFromClipboard(clipboard: ClipboardManager?) {
        if (clipboard == null || !clipboard.hasPrimaryClip()) return
        val clip = clipboard.primaryClip ?: return
        if (clip.itemCount == 0) return
        val text = clip.getItemAt(0).coerceToText(context)?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return
        if (dao.getAll().first().any { it.text == text }) return
        dao.insert(ClipboardItem(
            text = text,
            textLength = text.length,
            wordCount = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size,
            timestamp = System.currentTimeMillis(),
            folderId = null
        ))
    }

    suspend fun addItemToFolder(text: String, folderId: Long) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        dao.insert(ClipboardItem(
            text = trimmed,
            textLength = trimmed.length,
            wordCount = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }.size,
            folderId = folderId
        ))
    }

    suspend fun insert(item: ClipboardItem) {
        dao.insert(item)
    }

    suspend fun insertAll(items: List<ClipboardItem>) {
        dao.insertAll(items)
    }

    suspend fun update(item: ClipboardItem) {
        dao.update(item.copy(textLength = item.text.length))
    }

    suspend fun delete(item: ClipboardItem) {
        dao.delete(item)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    fun getFolders(): Flow<List<ClipboardFolder>> = folderDao.getAll()

    suspend fun createFolder(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        folderDao.insert(ClipboardFolder(name = trimmed))
    }

    suspend fun deleteFolder(folder: ClipboardFolder) {
        dao.deleteByFolder(folder.id)
        folderDao.delete(folder)
    }

    fun getAll(): Flow<List<ClipboardItem>> = dao.getAll()

    fun getAllAll(): Flow<List<ClipboardItem>> = dao.getAllAll()

    fun getForFolder(folderId: Long): Flow<List<ClipboardItem>> = dao.getForFolder(folderId)
}