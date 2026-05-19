package com.example.networkmonitor.domain.model

enum class ConnectionState(
    val label: String,
    val description: String,
    val isConnected: Boolean
) {
    AVAILABLE(
        label = "Połączono",
        description = "Urządzenie ma dostęp do internetu.",
        isConnected = true
    ),
    LOST(
        label = "Utracono połączenie",
        description = "Aktywna sieć została zerwana.",
        isConnected = false
    ),
    LOSING(
        label = "Połączenie niestabilne",
        description = "System zgłasza ryzyko utraty połączenia.",
        isConnected = true
    ),
    UNAVAILABLE(
        label = "Brak połączenia",
        description = "Brak dostępnej sieci z internetem.",
        isConnected = false
    )
}
