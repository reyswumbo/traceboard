package com.traceboard.app.viewmodel

import android.app.Application
import android.content.ClipboardManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.traceboard.app.data.model.ClipboardItem
import com.traceboard.app.data.repository.BackupManager
import com.traceboard.app.data.repository.ClipboardRepository
import com.traceboard.app.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val _items = MutableStateFlow<List<ClipboardItem>>(emptyList())

    val items: StateFlow<List<ClipboardItem>> = combine(_searchQuery, _items) { query, all ->
        if (query.isBlank()) all else all.filter { it.text.contains(query, ignoreCase = true) }
    }.asStateFlow()

    val isRecording: StateFlow<Boolean> = settingsRepository.isRecording

    private var pollJob: Job? = null

    init {
        viewModelScope.launch {
            clipboardRepository.getAll().collect { _items.value = it }
        }
        viewModelScope.launch {
            settingsRepository.isRecording.collect { recording ->
                if (recording) startPollingInternal() else pollJob?.cancel()
            }
        }
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

    private fun startPollingInternal() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val cm = app.getSystemService(ClipboardManager::class.java)
                clipboardRepository.addFromClipboard(cm)
                delay(1500)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveItem(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return@launch
            clipboardRepository.insert(ClipboardItem(
                text = trimmed,
                textLength = trimmed.length,
                wordCount = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            ))
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
            clipboardRepository.clear()
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