package com.traceboard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.traceboard.app.data.model.TrackedWord
import com.traceboard.app.data.model.WritingStats
import com.traceboard.app.data.repository.TrackedWordRepository
import com.traceboard.app.data.util.WritingAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WritingViewModel(
    app: Application,
    private val trackedWordRepository: TrackedWordRepository
) : AndroidViewModel(app) {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _stats = MutableStateFlow(WritingStats(0, 0, 0, 0))
    val stats: StateFlow<WritingStats> = _stats.asStateFlow()

    private val _trackedWords = MutableStateFlow<List<TrackedWord>>(emptyList())
    val trackedWords: StateFlow<List<TrackedWord>> = _trackedWords.asStateFlow()

    private val _trackedCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val trackedCounts: StateFlow<Map<String, Int>> = _trackedCounts.asStateFlow()

    init {
        viewModelScope.launch {
            trackedWordRepository.getAll().collect { words ->
                _trackedWords.value = words
                recalculate()
            }
        }
    }

    fun onTextChange(newText: String) {
        _text.value = newText
        recalculate()
    }

    private fun recalculate() {
        _stats.value = WritingAnalyzer.analyze(_text.value)
        _trackedCounts.value = _trackedWords.value.associate { word ->
            word.word to WritingAnalyzer.countOccurrences(_text.value, word.word)
        }
    }

    fun addTrackedWord(word: String) {
        viewModelScope.launch {
            trackedWordRepository.add(word)
        }
    }

    fun removeTrackedWord(word: TrackedWord) {
        viewModelScope.launch {
            trackedWordRepository.remove(word)
        }
    }

    fun resetCounts() {
        viewModelScope.launch {
            trackedWordRepository.resetAll()
        }
    }

    fun clearText() {
        _text.value = ""
        recalculate()
    }
}