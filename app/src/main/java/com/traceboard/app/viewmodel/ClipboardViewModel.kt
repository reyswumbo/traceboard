package com.traceboard.app.viewmodel

import android.app.Application
import android.content.ClipboardManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.traceboard.app.data.model.ClipboardFolder
import com.traceboard.app.data.model.ClipboardItem
import com.traceboard.app.data.repository.BackupManager
import com.traceboard.app.data.repository.ClipboardRepository
import com.traceboard.app.data.repository.SettingsRepository
import com.traceboard.app.service.ClipboardRecordingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClipboardViewModel(
    private val app: Application,
    private val clipboardRepository: ClipboardRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(app) {

    private val backupManager = BackupManager(app)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFolder = MutableStateFlow<Long?>(null)

    val selectedFolder: StateFlow<Long?> = _selectedFolder.asStateFlow()
    val folders: Flow<List<ClipboardFolder>> = clipboardRepository.getFolders()

    private val scopeItems: Flow<List<ClipboardItem>> = _selectedFolder
        .flatMapLatest { folderId ->
            if (folderId == null) clipboardRepository.getAll()
            else clipboardRepository.getForFolder(folderId)
        }

    val items: Flow<List<ClipboardItem>> = combine(_searchQuery, scopeItems) { query, all ->
        if (query.isBlank()) all else all.filter { it.text.contains(query, ignoreCase = true) }
    }

    val isRecording: Flow<Boolean> = settingsRepository.isRecording

    init {
        viewModelScope.launch {
            settingsRepository.isRecording.collect { recording ->
                if (recording) runCatching { ClipboardRecordingService.start(app) }
                else ClipboardRecordingService.stop(app)
            }
        }
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolder.value = folderId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startRecording() {
        viewModelScope.launch {
            settingsRepository.setRecording(true)
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            settingsRepository.setRecording(false)
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            clipboardRepository.createFolder(name)
        }
    }

    fun deleteFolder(folder: ClipboardFolder) {
        viewModelScope.launch(Dispatchers.IO) {
            clipboardRepository.deleteFolder(folder)
            if (_selectedFolder.value == folder.id) _selectedFolder.value = null
        }
    }

    fun saveItem(text: String) {
        val folderId = _selectedFolder.value
        viewModelScope.launch(Dispatchers.IO) {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return@launch
            if (folderId == null) {
                clipboardRepository.insert(ClipboardItem(
                    text = trimmed,
                    textLength = trimmed.length,
                    wordCount = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                ))
            } else {
                clipboardRepository.addItemToFolder(trimmed, folderId)
            }
        }
    }

    fun addCurrentClipboardToFolder(folderId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val cm = app.getSystemService(ClipboardManager::class.java)
            val clip = cm?.primaryClip
            val text = if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).coerceToText(app)?.toString()?.trim().orEmpty()
            } else {
                ""
            }
            if (text.isEmpty()) return@launch
            clipboardRepository.addItemToFolder(text, folderId)
        }
    }

    fun updateItem(item: ClipboardItem, newText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val trimmed = newText.trim()
            if (trimmed.isEmpty()) return@launch
            clipboardRepository.update(item.copy(text = trimmed))
        }
    }

    fun deleteItem(item: ClipboardItem) {
        viewModelScope.launch(Dispatchers.IO) {
            clipboardRepository.delete(item)
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            clipboardRepository.clearAll()
        }
    }

    fun copyToClipboard(item: ClipboardItem) {
        val cm = app.getSystemService(ClipboardManager::class.java)
        cm.setPrimaryClip(android.content.ClipData.newPlainText("Traceboard", item.text))
    }

    suspend fun exportToUri(uri: android.net.Uri): Boolean = withContext(Dispatchers.IO) {
        val output = app.contentResolver.openOutputStream(uri) ?: return@withContext false
        backupManager.export(clipboardRepository, output)
    }

    suspend fun importFromUri(uri: android.net.Uri): Boolean = withContext(Dispatchers.IO) {
        val input = app.contentResolver.openInputStream(uri) ?: return@withContext false
        val imported = backupManager.import(input) ?: return@withContext false
        val existing = clipboardRepository.getAll().first()
        val existingSet = existing.map { it.text }.toSet()
        val toInsert = imported.filter { it.text !in existingSet }
        if (toInsert.isEmpty()) return@withContext false
        clipboardRepository.insertAll(toInsert)
        true
    }
}