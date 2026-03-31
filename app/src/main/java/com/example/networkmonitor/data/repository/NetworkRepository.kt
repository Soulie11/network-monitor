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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class NetworkRepository(
    private val networkDao: NetworkLogDao,
    private val tracker: NetworkStatusTracker,
    private val notificationHelper: NotificationHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val logs: Flow<List<ConnectionLog>> = networkDao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    fun startMonitoring() {
        scope.launch {
            tracker.observeNetwork().collect { log ->
                networkDao.insert(log.toEntity())
                if (log.state == ConnectionState.AVAILABLE || log.state == ConnectionState.LOST) {
                    notificationHelper.showConnectionNotification(log)
                }
            }
        }
    }

    fun clearLogs() {
        scope.launch { networkDao.clearAll() }
    }
}
