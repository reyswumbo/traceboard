package com.traceboard.app.ui.screens.writing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.traceboard.app.data.model.TrackedWord
import com.traceboard.app.ui.components.StatCard
import com.traceboard.app.viewmodel.WritingViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WritingScreen(viewModel: WritingViewModel) {
    val textChanged by viewModel.text.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val trackedWords by viewModel.trackedWords.collectAsStateWithLifecycle()
    val trackedCounts by viewModel.trackedCounts.collectAsStateWithLifecycle()

    var showAddWordDialog by remember { mutableStateOf(false) }
    var newWord by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menulis") },
                actions = {
                    IconButton(onClick = { viewModel.resetCounts() }) {
                        Icon(Icons.Filled.Replay, contentDescription = "Reset hitungan")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = textChanged,
                onValueChange = { viewModel.onTextChange(it) },
                label = { Text("Tulis teksmu di sini…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                minLines = 5
            )

            Text(
                text = "Statistik Menulis",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(label = "Kata", value = stats.wordCount.toString(), modifier = Modifier.weight(1f))
                StatCard(label = "Karakter", value = stats.charCount.toString(), modifier = Modifier.weight(1f))
                StatCard(label = "Huruf", value = stats.letterCount.toString(), modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kata Terlacak",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp)
                )
                IconButton(onClick = { showAddWordDialog = true; newWord = "" }) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah kata terlacak")
                }
            }

            if (trackedWords.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Belum ada kata terlacak. Tambahkan kata yang ingin dipantau kemunculannya (huruf besar/kecil tidak memengaruhi).",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trackedWords.forEach { word ->
                        TrackedWordRow(word = word, count = trackedCounts[word.word] ?: 0, onRemove = {
                            viewModel.removeTrackedWord(word)
                        })
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))
        }
    }

    if (showAddWordDialog) {
        AlertDialog(
            onDismissRequest = { showAddWordDialog = false },
            title = { Text("Tambah Kata Terlacak") },
            text = {
                OutlinedTextField(
                    value = newWord,
                    onValueChange = { newWord = it },
                    label = { Text("Contoh: maaf") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addTrackedWord(newWord)
                    newWord = ""
                    showAddWordDialog = false
                }) { Text("Tambah") }
            },
            dismissButton = {
                TextButton(onClick = { showAddWordDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun TrackedWordRow(
    word: TrackedWord,
    count: Int,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = word.word,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )
            Text(
                text = "${count}x",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus kata", modifier = Modifier.size(20.dp))
            }
        }
    }
}