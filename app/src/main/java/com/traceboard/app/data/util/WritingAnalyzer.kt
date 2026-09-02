package com.traceboard.app.data.util

import com.traceboard.app.data.model.WritingStats

object WritingAnalyzer {

    fun analyze(text: String): WritingStats {
        val trimmed = text.trim()
        return if (trimmed.isEmpty()) {
            WritingStats(0, 0, 0, 0)
        } else {
            val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            WritingStats(
                wordCount = words,
                charCount = text.length,
                letterCount = text.count { it.isLetter() },
                spaceCount = text.count { it == ' ' }
            )
        }
    }

    fun countOccurrences(text: String, trackedWord: String): Int {
        if (trackedWord.isBlank() || text.isBlank()) return 0
        val pattern = Regex("(?i)${Regex.escape(trackedWord.trim())}")
        return pattern.findAll(text).count()
    }
}
