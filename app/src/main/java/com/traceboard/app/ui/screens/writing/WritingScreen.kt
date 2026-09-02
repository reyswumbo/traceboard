package com.traceboard.app.ui.screens.writing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.traceboard.app.data.model.TrackedWord
import com.traceboard.app.data.util.AccessibilityUtils
import com.traceboard.app.ui.components.EmptyState
import com.traceboard.app.viewmodel.WritingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingScreen(viewModel: WritingViewModel) {
    val trackedWords by viewModel.trackedWords.collectAsStateWithLifecycle(initialValue = emptyList<TrackedWord>())
    val context = LocalContext.current
    var accessibilityEnabled by remember {
        mutableStateOf(AccessibilityUtils.isWritingServiceEnabled(context))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityEnabled = AccessibilityUtils.isWritingServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
            if (!accessibilityEnabled) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TouchApp, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text(
                                text = "Aktifkan layanan aksesibilitas",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Text(
                            text = "Kata-kata yang kamu ketik di aplikasi mana pun akan otomatis dihitung (tanpa tombol Mulai).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Button(
                            onClick = { AccessibilityUtils.openAccessibilitySettings(context) },
                            modifier = Modifier.padding(top = 10.dp)
                        ) { Text("Buka Pengaturan Aksesibilitas") }
                    }
                }
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
                EmptyState(
                    icon = Icons.Filled.Tag,
                    title = "Belum ada kata terlacak",
                    description = "Tambahkan kata yang ingin dipantau. Saat kamu mengetik kata itu di aplikasi mana pun, hitungannya bertambah otomatis."
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trackedWords.forEach { word ->
                        TrackedWordRow(word = word, onRemove = {
                            viewModel.removeTrackedWord(word)
                        })
                    }
                }
            }

            Text(
                text = if (accessibilityEnabled) {
                    "Mengetik di aplikasi mana pun akan menambah hitungan kata yang kamu lacak. Huruf besar/kecil tidak memengaruhi."
                } else {
                    "Setelah layanan aksesibilitas aktif, setiap kata yang kamu ketik akan dihitung otomatis — tanpa perlu menekan tombol Mulai."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
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
                text = "${word.count}x",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus kata", modifier = Modifier.size(20.dp))
            }
        }
    }
}