package com.example.networkmonitor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.networkmonitor.domain.model.ConnectionLog
import com.example.networkmonitor.domain.model.ConnectionState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitor sieci") },
                actions = {
                    IconButton(onClick = viewModel::clearLogs) {
                        Icon(Icons.Default.Delete, contentDescription = "Wyczyść logi")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            StatusCard(currentLog = state.currentLog)
            Spacer(modifier = Modifier.height(16.dp))
            HistoryHeader(logCount = state.logs.size)
            Spacer(modifier = Modifier.height(8.dp))
            if (state.logs.isEmpty()) {
                EmptyLogsCard()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.logs,
                        key = { log -> log.id }
                    ) { log ->
                        LogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(currentLog: ConnectionLog?) {
    val state = currentLog?.state
    val statusColor = state.statusColor()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = statusColor.copy(alpha = 0.10f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = state.statusIcon(),
                    contentDescription = null,
                    tint = statusColor
                )
                Column {
                    Text(
                        text = "Aktualny stan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentLog?.let { formatTimestamp(it) } ?: "Oczekiwanie na odczyt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state?.label ?: "Brak danych",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = state?.description ?: "Monitorowanie rozpocznie się automatycznie.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            InfoLine(label = "Typ połączenia", value = currentLog?.transport ?: "-")
            InfoLine(
                label = "Dostęp do internetu",
                value = if (currentLog?.isValidated == true) "Tak" else "Nie"
            )
        }
    }
}

@Composable
private fun HistoryHeader(logCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Historia połączeń",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = formatLogCount(logCount),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyLogsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Brak zapisanych logów",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pierwszy wpis pojawi się po odczytaniu obecnego stanu sieci.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LogItem(log: ConnectionLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = log.state.statusIcon(),
                    contentDescription = null,
                    tint = log.state.statusColor()
                )
                Text(
                    text = log.state.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.state.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            InfoLine(label = "Transport", value = log.transport)
            InfoLine(
                label = "Internet zweryfikowany",
                value = if (log.isValidated) "Tak" else "Nie"
            )
            Text(
                text = formatTimestamp(log),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun ConnectionState?.statusIcon(): ImageVector = when (this) {
    ConnectionState.AVAILABLE -> Icons.Default.CheckCircle
    ConnectionState.LOSING -> Icons.Default.Info
    ConnectionState.LOST,
    ConnectionState.UNAVAILABLE -> Icons.Default.Warning
    null -> Icons.Default.Info
}

@Composable
private fun ConnectionState?.statusColor(): Color = when (this) {
    ConnectionState.AVAILABLE -> Color(0xFF168550)
    ConnectionState.LOSING -> Color(0xFF8A5A00)
    ConnectionState.LOST,
    ConnectionState.UNAVAILABLE -> MaterialTheme.colorScheme.error
    null -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun formatTimestamp(log: ConnectionLog): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        .withZone(ZoneId.systemDefault())
    return formatter.format(log.timestamp)
}

private fun formatLogCount(count: Int): String = when (count) {
    1 -> "1 zdarzenie"
    in 2..4 -> "$count zdarzenia"
    else -> "$count zdarzeń"
}
