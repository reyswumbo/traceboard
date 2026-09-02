package com.traceboard.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.traceboard.app.data.model.ClipboardItem
import com.traceboard.app.data.model.TraceboardBackup
import java.io.InputStream
import java.io.OutputStream

class BackupManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun export(clipboardRepository: ClipboardRepository, output: OutputStream): Boolean {
        val items = kotlin.runCatching {
            var result = emptyList<ClipboardItem>()
            kotlinx.coroutines.runBlocking {
                kotlinx.coroutines.flow.first(clipboardRepository.getAll()).let { result = it }
            }
            result
        }.getOrDefault(emptyList())

        val backup = TraceboardBackup(clipboardEntries = items)
        val json = gson.toJson(backup)
        return runCatching {
            output.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            true
        }.getOrDefault(false)
    }

    fun import(input: InputStream): List<ClipboardItem>? {
        return runCatching {
            val json = input.bufferedReader().use { it.readText() }
            val backup = gson.fromJson(json, TraceboardBackup::class.java)
            (backup.clipboardEntries ?: return@runCatching null)
                .filter { it.text.isNotBlank() }
        }.getOrNull()
    }
}