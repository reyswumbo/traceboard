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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.traceboard.app.ui.components.StatCard
import com.traceboard.app.viewmodel.DashboardViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val trackedCounts by viewModel.trackedCountsToday.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshDashboard()
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
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