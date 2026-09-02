package com.traceboard.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.traceboard.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val keyRecording = intPreferencesKey("clipboard_recording")
    private val keyTheme = intPreferencesKey("theme_mode")

    val isRecording: Flow<Boolean> = context.dataStore.data.map { (it[keyRecording] ?: 0) == 1 }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        try { ThemeMode.entries[it[keyTheme] ?: 0] } catch (_: Exception) { ThemeMode.DEFAULT }
    }

    suspend fun setRecording(value: Boolean) {
        context.dataStore.edit { it[keyRecording] = if (value) 1 else 0 }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[keyTheme] = mode.ordinal }
    }
}
