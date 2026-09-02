package com.traceboard.app.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.traceboard.app.data.repository.ClipboardRepository
import com.traceboard.app.data.repository.SettingsRepository
import com.traceboard.app.data.repository.TrackedWordRepository
import com.traceboard.app.data.repository.UsageRepository

class ViewModelFactory(
    private val app: Application,
    private val clipboardRepository: ClipboardRepository,
    private val trackedWordRepository: TrackedWordRepository,
    private val settingsRepository: SettingsRepository,
    private val usageRepository: UsageRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ClipboardViewModel::class.java) ->
                ClipboardViewModel(app, clipboardRepository, settingsRepository) as T
            modelClass.isAssignableFrom(WritingViewModel::class.java) ->
                WritingViewModel(app, trackedWordRepository) as T
            modelClass.isAssignableFrom(UsageViewModel::class.java) ->
                UsageViewModel(app, usageRepository) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(app, clipboardRepository, trackedWordRepository, usageRepository, settingsRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
