package com.example.networkmonitor.data.repository

import com.example.networkmonitor.data.local.NetworkLogDao
import com.example.networkmonitor.data.local.toDomain
import com.example.networkmonitor.data.local.toEntity
import com.example.networkmonitor.domain.model.ConnectionLog
import com.example.networkmonitor.domain.model.ConnectionState
import com.example.networkmonitor.service.NetworkStatusTracker
import com.example.networkmonitor.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class NetworkRepository(
    private val networkDao: NetworkLogDao,
    private val tracker: NetworkStatusTracker,
    private val notificationHelper: NotificationHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitoringJob: Job? = null

    private val _currentLog = MutableStateFlow<ConnectionLog?>(null)
    val currentLog: StateFlow<ConnectionLog?> = _currentLog.asStateFlow()

    val logs: Flow<List<ConnectionLog>> = networkDao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    fun startMonitoring() {
        if (monitoringJob?.isActive == true) return

        monitoringJob = scope.launch {
            var previousLog = networkDao.latest()?.toDomain()

            tracker.observeNetwork()
                .distinctUntilChanged { old, new -> old.hasSameStatusAs(new) }
                .collect { log ->
                    _currentLog.value = log

                    if (previousLog?.hasSameStatusAs(log) == true) {
                        return@collect
                    }

                    networkDao.insert(log.toEntity())
                    networkDao.trimToLatest(MAX_LOG_COUNT)

                    if (previousLog != null && shouldNotify(previousLog, log)) {
                        notificationHelper.showConnectionNotification(log)
                    }

                    previousLog = log
                }
        }
    }

    fun clearLogs() {
        scope.launch { networkDao.clearAll() }
    }

    private fun ConnectionLog.hasSameStatusAs(other: ConnectionLog): Boolean =
        state == other.state &&
            transport == other.transport &&
            isValidated == other.isValidated

    private fun shouldNotify(previousLog: ConnectionLog?, currentLog: ConnectionLog): Boolean {
        val previousState = previousLog?.state ?: return false
        return previousState.isConnected != currentLog.state.isConnected
    }

    companion object {
        private const val MAX_LOG_COUNT = 200
    }
}
