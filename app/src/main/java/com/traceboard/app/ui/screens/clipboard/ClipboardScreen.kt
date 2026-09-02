package com.traceboard.app.ui.screens.clipboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.traceboard.app.data.model.ClipboardFolder
import com.traceboard.app.data.model.ClipboardItem
import com.traceboard.app.ui.components.EmptyState
import com.traceboard.app.viewmodel.ClipboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardScreen(viewModel: ClipboardViewModel) {
    val items by viewModel.items.collectAsStateWithLifecycle(initialValue = emptyList<ClipboardItem>())
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList<ClipboardFolder>())
    val selectedFolder by viewModel.selectedFolder.collectAsStateWithLifecycle(initialValue = null)
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle(initialValue = false)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editingItem by remember { mutableStateOf<ClipboardItem?>(null) }
    var editText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var folderToDelete by remember { mutableStateOf<ClipboardFolder?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val ok = viewModel.exportToUri(it)
                snackbarHostState.showSnackbar(if (ok) "Ekspor berhasil" else "Gagal mengekspor")
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val ok = viewModel.importFromUri(it)
                snackbarHostState.showSnackbar(
                    if (ok) "Impor berhasil" else "Gagal mengimpor atau file tidak valid"
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Clipboard") })
        },
        bottomBar = {
            androidx.compose.material3.Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(onClick = { if (isRecording) viewModel.stopRecording() else viewModel.startRecording() }) {
                            Icon(Icons.Filled.FiberManualRecord, contentDescription = null, tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = if (isRecording) "Merekam" else "Mulai",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row {
                        IconButton(onClick = { exportLauncher.launch("traceboard-backup.json") }) {
                            Icon(Icons.Filled.FileDownload, contentDescription = "Ekspor")
                        }
                        IconButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }) {
                            Icon(Icons.Filled.FileUpload, contentDescription = "Impor")
                        }
                        IconButton(onClick = {
                            showAddDialog = true
                            editText = ""
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Tambah")
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.setSearchQuery(it)
                },
                placeholder = { Text("Cari teks tersimpan…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            FolderBar(
                folders = folders,
                selectedFolder = selectedFolder,
                onSelect = { viewModel.selectFolder(it) },
                onAddFolder = {
                    newFolderName = ""
                    showFolderDialog = true
                }
            )

            if (selectedFolder == null) {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        scope.launch { snackbarHostState.showSnackbar("Semua item dihapus") }
                    },
                    enabled = items.isNotEmpty(),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Hapus semua", modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                val folder = folders.firstOrNull { it.id == selectedFolder }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addCurrentClipboardToFolder(selectedFolder) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Tambah item dari clipboard", modifier = Modifier.padding(start = 6.dp))
                    }
                    TextButton(onClick = { folderToDelete = folder }) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Hapus folder")
                    }
                }
            }

            if (items.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.ContentPaste,
                    title = if (selectedFolder == null) "Belum ada item clipboard"
                    else "Folder kosong",
                    description = if (selectedFolder == null)
                        "Tekan Mulai untuk merekam teks yang disalin, atau tambahkan secara manual."
                    else "Tekan «Tambah item dari clipboard» untuk menyimpan teks yang sedang disalin ke folder ini."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ClipboardItemCard(
                            item = item,
                            onCopy = {
                                viewModel.copyToClipboard(item)
                                scope.launch { snackbarHostState.showSnackbar("Disalin ke clipboard") }
                            },
                            onEdit = {
                                editingItem = item
                                editText = item.text
                            },
                            onDelete = {
                                viewModel.deleteItem(item)
                                scope.launch { snackbarHostState.showSnackbar("Item dihapus") }
                            }
                        )
                    }
                }
            }
        }
    }

    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Edit Teks") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateItem(editingItem!!, editText)
                    editingItem = null
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) { Text("Batal") }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (selectedFolder == null) "Tambah Teks" else "Tambah Teks ke Folder") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveItem(editText)
                    editText = ""
                    showAddDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("Buat Folder Baru") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Contoh: facebook") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createFolder(newFolderName)
                    newFolderName = ""
                    showFolderDialog = false
                }) { Text("Buat") }
            },
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) { Text("Batal") }
            }
        )
    }

    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Hapus Folder") },
            text = { Text("Folder \"${folder.name}\" beserta semua item di dalamnya akan dihapus. Item di folder Semua tidak terpengaruh.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFolder(folder)
                    folderToDelete = null
                    scope.launch { snackbarHostState.showSnackbar("Folder dihapus") }
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun FolderBar(
    folders: List<ClipboardFolder>,
    selectedFolder: Long?,
    onSelect: (Long?) -> Unit,
    onAddFolder: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedFolder == null,
            onClick = { onSelect(null) },
            label = { Text("Semua") }
        )
        folders.forEach { folder ->
            FilterChip(
                selected = selectedFolder == folder.id,
                onClick = { onSelect(folder.id) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(folder.name, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            )
        }
        FilledTonalIconButton(onClick = onAddFolder) {
            Icon(Icons.Filled.Add, contentDescription = "Buat folder")
        }
    }
}

@Composable
private fun ClipboardItemCard(
    item: ClipboardItem,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.wordCount} kata · ${dateFormat.format(Date(item.timestamp))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus", modifier = Modifier.size(18.dp))
                }
                Button(onClick = onCopy) { Text("Salin") }
            }
        }
    }
}