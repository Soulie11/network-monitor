package com.example.networkmonitor.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkmonitor.data.repository.NetworkRepository
import com.example.networkmonitor.domain.model.ConnectionLog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel(
    private val repository: NetworkRepository
) : ViewModel() {

    var uiState by mutableStateOf(MainUiState())
        private set

    init {
        repository.startMonitoring()
        repository.logs
            .onEach { logs ->
                uiState = uiState.copy(
                    currentLog = logs.firstOrNull(),
                    logs = logs
                )
            }
            .launchIn(viewModelScope)
    }

    fun clearLogs() {
        repository.clearLogs()
    }
}

data class MainUiState(
    val currentLog: ConnectionLog? = null,
    val logs: List<ConnectionLog> = emptyList()
)
