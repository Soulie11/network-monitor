package com.example.networkmonitor.domain.model

import java.time.Instant

data class ConnectionLog(
    val id: Int = 0,
    val state: ConnectionState,
    val transport: String,
    val isValidated: Boolean,
    val timestamp: Instant
)
