package com.traceboard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.traceboard.app.data.model.TrackedWord
import com.traceboard.app.data.repository.TrackedWordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WritingViewModel(
    app: Application,
    private val trackedWordRepository: TrackedWordRepository
) : AndroidViewModel(app) {

    val trackedWords: StateFlow<List<TrackedWord>> = trackedWordRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}