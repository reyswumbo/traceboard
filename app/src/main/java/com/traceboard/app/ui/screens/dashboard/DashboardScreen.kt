package com.traceboard.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.traceboard.app.data.repository.SettingsRepository
import com.traceboard.app.ui.components.StatCard
import com.traceboard.app.ui.theme.ThemeMode
import com.traceboard.app.viewmodel.DashboardViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val trackedCounts by viewModel.trackedCountsToday.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()
    val currentThemeMode by settingsRepo.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.DEFAULT)
    var showThemeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Traceboard", style = MaterialTheme.typography.titleLarge)
                        Text(
                            state.greeting,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Filled.Palette, contentDescription = "Ubah tema")
                    }
                    IconButton(onClick = { viewModel.refreshDashboard() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Muat ulang")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Hari Ini", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = "Penggunaan layar",
                        value = state.screenTimeToday,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Kata ditulis",
                        value = com.traceboard.app.data.util.TimeFormatter.formatNumber(state.totalWords),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = "Item clipboard",
                        value = state.clipboardCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Status",
                        value = if (state.isRecording) "Merekam" else "Berhenti",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (!state.hasUsagePermission) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Text(
                            stringPermission(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            if (trackedCounts.isNotEmpty()) {
                item {
                    Text("Kata Terlacak", style = MaterialTheme.typography.titleMedium)
                }
                items(trackedCounts.entries.sortedByDescending { it.value }.take(5).toList()) { (word, count) ->
                    DashboardItem(
                        icon = Icons.Filled.Tag,
                        title = word,
                        value = "${count}x"
                    )
                }
            }
            item { androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp)) }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Pilih Tema") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentThemeMode == mode,
                                onClick = {
                                    scope.launch { settingsRepo.setThemeMode(mode) }
                                    showThemeDialog = false
                                }
                            )
                            Text(
                                text = when (mode) {
                                    ThemeMode.DEFAULT -> "Default"
                                    ThemeMode.LIQUID_INK -> "Liquid Ink"
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Tutup") }
            }
        )
    }
}

@Composable
private fun DashboardItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun stringPermission(): String =
    "Aktifkan Akses Penggunaan Aplikasi di Pengaturan agar statistik penggunaan ditampilkan dengan akurat."