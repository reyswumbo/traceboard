package com.traceboard.app.ui.screens.usage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.traceboard.app.data.model.AppUsage
import com.traceboard.app.data.repository.StorageInfo
import com.traceboard.app.data.util.TimeFormatter
import com.traceboard.app.ui.components.EmptyState
import com.traceboard.app.viewmodel.UsagePeriod
import com.traceboard.app.viewmodel.UsageViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UsageScreen(viewModel: UsageViewModel) {
    val period by viewModel.period.collectAsStateWithLifecycle()
    val appUsage by viewModel.appUsage.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val lastUpdated by viewModel.lastUpdated.collectAsStateWithLifecycle()
    val batteryLevel by viewModel.batteryLevel.collectAsStateWithLifecycle()
    val storage by viewModel.storage.collectAsStateWithLifecycle()
    var selectedApp by remember { mutableStateOf<AppUsage?>(null) }

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Penggunaan") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Muat ulang", modifier = Modifier.size(20.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UsagePeriod.entries.forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { if (period != p) viewModel.selectPeriod(p) },
                        label = {
                            Text(
                                p.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            if (!hasPermission) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Akses penggunaan belum aktif",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "Aktifkan «Akses Penggunaan Aplikasi» untuk melihat statistik pemakaian aplikasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Button(
                        onClick = { viewModel.openUsageSettings() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) { Text("Buka Pengaturan") }
                }
            } else if (appUsage.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Insights,
                    title = "Belum ada data penggunaan",
                    description = "Gunakan aplikasi terlebih dahulu, lalu muat ulang untuk melihat datanya."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        val total = appUsage.sumOf { it.totalTime }
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(
                                    text = "Total waktu penggunaan",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = TimeFormatter.formatPrecise(total),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Terakhir diperbarui: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale("id", "ID")).format(java.util.Date(lastUpdated))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    items(appUsage.take(15)) { usage ->
                        AppUsageRow(usage = usage) {
                            selectedApp = usage
                        }
                    }

                    item {
                        Text(
                            text = "Battery & Penyimpanan",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.BatteryFull, contentDescription = null)
                                    Text(
                                        text = if (batteryLevel >= 0) "$batteryLevel%" else "Tidak tersedia",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Storage, contentDescription = null)
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(style = MaterialTheme.typography.labelSmall, text = "Penyimpanan")
                                        Text(style = MaterialTheme.typography.titleMedium, text = storage?.let { formatStorage(it) } ?: "—")
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Text(
                                text = "Statistik penggunaan per aplikasi diperoleh dari Akses Penggunaan Aplikasi Android. Statistik battery per aplikasi tidak tersedia lewat API standar; aplikasi tidak membuat data palsu.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    selectedApp?.let { usage ->
        ModalBottomSheet(onDismissRequest = { selectedApp = null }) {
            AppUsageDetail(usage = usage)
            androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp))
        }
    }
}

private fun formatStorage(info: StorageInfo): String {
    val used = info.totalBytes - info.freeBytes
    return "${formatBytes(used)} terpakai / ${formatBytes(info.totalBytes)}"
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= (1L shl 30) -> "%.1f GB".format(bytes / (1024.0 * 1024 * 1024)).replace(",", ".")
        bytes >= (1L shl 20) -> "%.1f MB".format(bytes / (1024.0 * 1024)).replace(",", ".")
        else -> "$bytes B"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppUsageRow(usage: AppUsage, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = TimeFormatter.formatPrecise(usage.totalTime),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = usage.appName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Terakhir aktif: ${java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale("id", "ID")).format(java.util.Date(usage.lastTimeUsed))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppUsageDetail(usage: AppUsage) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text(style = MaterialTheme.typography.titleLarge, text = usage.appName)
        Text(style = MaterialTheme.typography.bodySmall, text = usage.packageName, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Total waktu", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = TimeFormatter.formatPrecise(usage.totalTime), style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}