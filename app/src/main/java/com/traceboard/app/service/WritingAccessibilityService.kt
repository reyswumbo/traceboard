package com.traceboard.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.traceboard.app.data.repository.TrackedWordRepository
import com.traceboard.app.data.util.WritingAnalyzer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WritingAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val repo by lazy { TrackedWordRepository(applicationContext) }
    private val lastText = HashMap<String, String>()

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return
        if (event.text == null || event.text.isEmpty()) return

        val text = event.text.joinToString("\n")
        val key = "${event.packageName}|${event.className}|${event.source?.viewIdResourceName ?: ""}"

        val delta = newDelta(key, text) ?: return
        if (delta.isNotBlank()) {
            scope.launch {
                val words = repo.getWords()
                if (words.isEmpty()) return@launch
                for (word in words) {
                    val n = WritingAnalyzer.countOccurrences(delta, word.word)
                    if (n > 0) repo.incrementCount(word.word, n)
                }
            }
        }
        lastText[key] = text
    }

    private fun newDelta(key: String, text: String): String? {
        val prev = lastText[key]
        return when {
            prev == null -> null
            text.startsWith(prev) -> text.substring(prev.length)
            else -> {
                lastText[key] = text
                null
            }
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}