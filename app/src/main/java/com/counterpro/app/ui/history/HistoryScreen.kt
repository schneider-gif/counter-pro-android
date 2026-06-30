package com.counterpro.app.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.counterpro.app.data.repository.CountRepository
import com.counterpro.app.domain.model.CountEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(private val repo: CountRepository) : ViewModel() {
    val history = repo.getHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun delete(id: Long) = viewModelScope.launch { repo.delete(id) }
    fun clearAll() = viewModelScope.launch { repo.clearAll() }
}

@Composable
fun HistoryScreen(vm: HistoryViewModel) {
    val history by vm.history.collectAsState()
    val fmt = remember { SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()) }

    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum registro ainda", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { vm.clearAll() }) { Text("Limpar tudo") }
        }
        LazyColumn {
            items(history, key = { it.id }) { entry ->
                HistoryItem(entry, fmt, onDelete = { vm.delete(entry.id) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun HistoryItem(entry: CountEntry, fmt: SimpleDateFormat, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(entry.label) },
        supportingContent = { Text(fmt.format(Date(entry.timestamp))) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.count.toString(), style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Deletar")
                }
            }
        }
    )
}
