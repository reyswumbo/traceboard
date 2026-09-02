package com.traceboard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.traceboard.app.data.repository.ClipboardRepository
import com.traceboard.app.data.repository.SettingsRepository
import com.traceboard.app.data.repository.TrackedWordRepository
import com.traceboard.app.data.repository.UsageRepository
import com.traceboard.app.data.util.TimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DashboardState(
    val greeting: String = "Selamat datang",
    val totalWords: Long = 0,
    val clipboardCount: Int = 0,
    val screenTimeToday: String = "0m",
    val isRecording: Boolean = false,
    val hasUsagePermission: Boolean = false
)

class DashboardViewModel(
    app: Application,
    private val clipboardRepository: ClipboardRepository,
    private val trackedWordRepository: TrackedWordRepository,
    private val usageRepository: UsageRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _trackedCountsToday = MutableStateFlow<Map<String, Int>>(emptyMap())
    val trackedCountsToday: StateFlow<Map<String, Int>> = _trackedCountsToday.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.isRecording.collect { recording ->
                _state.value = _state.value.copy(isRecording = recording)
            }
        }
        viewModelScope.launch {
            clipboardRepository.getAll().collect { items ->
                val totalWords = items.sumOf { it.wordCount.toLong() }
                _state.value = _state.value.copy(
                    clipboardCount = items.size,
                    totalWords = totalWords
                )
                refreshGreeting()
            }
        }
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            val screenTime = usageRepository.getAppUsage(24L * 3600 * 1000).sumOf { it.totalTime }
            val hasPerm = usageRepository.hasUsagePermission()
            _state.value = _state.value.copy(
                screenTimeToday = TimeFormatter.formatCompact(screenTime),
                hasUsagePermission = hasPerm
            )
            val totals = mutableMapOf<String, Int>()
            val words = trackedWordRepository.getAll().first()
            val allItems = clipboardRepository.getAll().first()
            for (word in words) {
                var count = 0
                for (item in allItems) {
                    count += com.traceboard.app.data.util.WritingAnalyzer
                        .countOccurrences(item.text, word.word)
                }
                totals[word.word] = count
            }
            _trackedCountsToday.value = totals
        }
    }

    private fun refreshGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Selamat pagi"
            in 12..14 -> "Selamat siang"
            in 15..17 -> "Selamat sore"
            else -> "Selamat malam"
        }
        _state.value = _state.value.copy(greeting = "$greeting 👋")
    }
}