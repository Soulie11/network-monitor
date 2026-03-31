package com.example.networkmonitor.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.networkmonitor.domain.model.ConnectionLog
import com.example.networkmonitor.domain.model.ConnectionState

class NotificationHelper(private val context: Context) {

    fun showConnectionNotification(log: ConnectionLog) {
        val content = when (log.state) {
            ConnectionState.AVAILABLE -> "Połączenie zostało przywrócone (${log.transport})."
            ConnectionState.LOST -> "Urządzenie utraciło połączenie z siecią."
            ConnectionState.LOSING -> "Połączenie może zostać utracone."
            ConnectionState.UNAVAILABLE -> "Brak dostępnej sieci."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Monitor sieci")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(log.timestamp.toEpochMilli().toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "network_status_channel"

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Zmiany połączenia",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Powiadomienia o utracie lub odzyskaniu połączenia z siecią"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
