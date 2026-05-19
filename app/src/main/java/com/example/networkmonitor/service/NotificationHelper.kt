package com.example.networkmonitor.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.networkmonitor.MainActivity
import com.example.networkmonitor.domain.model.ConnectionLog
import com.example.networkmonitor.domain.model.ConnectionState

class NotificationHelper(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun showConnectionNotification(log: ConnectionLog) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(log.state.label)
            .setContentText(log.notificationText())
            .setStyle(NotificationCompat.BigTextStyle().bigText(log.notificationText()))
            .setContentIntent(openAppIntent())
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission can be revoked after the app starts on Android 13+.
        }
    }

    private fun ConnectionLog.notificationText(): String = when (state) {
        ConnectionState.AVAILABLE -> "Połączenie zostało przywrócone przez $transport."
        ConnectionState.LOST -> "Urządzenie utraciło połączenie z siecią."
        ConnectionState.LOSING -> "Połączenie przez $transport jest niestabilne."
        ConnectionState.UNAVAILABLE -> "Brak dostępnego połączenia z internetem."
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }

        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    companion object {
        const val CHANNEL_ID = "network_status_channel"
        private const val NOTIFICATION_ID = 1001

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
