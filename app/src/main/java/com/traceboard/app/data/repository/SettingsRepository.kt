package com.traceboard.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val keyRecording = booleanPreferencesKey("clipboard_recording")

    val isRecording: Flow<Boolean> = context.dataStore.data.map { it[keyRecording] ?: false }

    suspend fun setRecording(value: Boolean) {
        context.dataStore.edit { it[keyRecording] = value }
    }
}
