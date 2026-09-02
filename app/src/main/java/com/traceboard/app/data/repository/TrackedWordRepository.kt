package com.traceboard.app.data.repository

import android.content.Context
import com.traceboard.app.data.model.TrackedWord
import kotlinx.coroutines.flow.Flow

class TrackedWordRepository(private val context: Context) {

    private val dao = TraceboardDatabase.getInstance(context).trackedWordDao()

    fun getAll(): Flow<List<TrackedWord>> = dao.getAll()

    suspend fun getWords(): List<TrackedWord> = dao.getWords()

    suspend fun incrementCount(word: String, amount: Int) {
        dao.incrementCount(word, amount)
    }

    suspend fun add(word: String) {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return
        dao.insert(TrackedWord(word = trimmed))
    }

    suspend fun update(word: TrackedWord) {
        dao.update(word)
    }

    suspend fun remove(word: TrackedWord) {
        dao.delete(word)
    }

    suspend fun resetCount(word: TrackedWord) {
        dao.update(word.copy(count = 0))
    }

    suspend fun resetAll() {
        dao.resetAllCounts()
    }
}
