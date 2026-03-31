package com.example.networkmonitor.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.networkmonitor.domain.model.ConnectionLog
import com.example.networkmonitor.domain.model.ConnectionState
import java.time.Instant

@Entity(tableName = "connection_logs")
data class ConnectionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val state: String,
    val transport: String,
    val isValidated: Boolean,
    val timestamp: Long
)

fun ConnectionLogEntity.toDomain(): ConnectionLog = ConnectionLog(
    id = id,
    state = ConnectionState.valueOf(state),
    transport = transport,
    isValidated = isValidated,
    timestamp = Instant.ofEpochMilli(timestamp)
)

fun ConnectionLog.toEntity(): ConnectionLogEntity = ConnectionLogEntity(
    id = id,
    state = state.name,
    transport = transport,
    isValidated = isValidated,
    timestamp = timestamp.toEpochMilli()
)
