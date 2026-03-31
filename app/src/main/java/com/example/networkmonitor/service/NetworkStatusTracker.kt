package com.example.networkmonitor.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.networkmonitor.domain.model.ConnectionLog
import com.example.networkmonitor.domain.model.ConnectionState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant

class NetworkStatusTracker(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun observeNetwork(): Flow<ConnectionLog> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                trySend(currentConnectionLog(ConnectionState.AVAILABLE))
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val state = if (
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ) {
                    ConnectionState.AVAILABLE
                } else {
                    ConnectionState.UNAVAILABLE
                }

                trySend(currentConnectionLog(state))
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                trySend(currentConnectionLog(ConnectionState.LOSING))
            }

            override fun onLost(network: Network) {
                val activeNetwork = connectivityManager.activeNetwork
                val state = if (activeNetwork == null) {
                    ConnectionState.LOST
                } else {
                    ConnectionState.AVAILABLE
                }
                trySend(currentConnectionLog(state))
            }

            override fun onUnavailable() {
                trySend(currentConnectionLog(ConnectionState.UNAVAILABLE))
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        trySend(currentConnectionLog())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    private fun currentConnectionLog(
        fallbackState: ConnectionState = ConnectionState.UNAVAILABLE
    ): ConnectionLog {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }

        val isValidated =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

        val finalState = when {
            activeNetwork == null -> ConnectionState.UNAVAILABLE
            isValidated -> ConnectionState.AVAILABLE
            else -> fallbackState
        }

        return ConnectionLog(
            state = finalState,
            transport = capabilities.transportLabel(),
            isValidated = isValidated,
            timestamp = Instant.now()
        )
    }

    private fun NetworkCapabilities?.transportLabel(): String {
        if (this == null) return "Brak aktywnej sieci"
        return when {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Dane komórkowe"
            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
            else -> "Inne"
        }
    }
}