package com.example.networkmonitor

import android.app.Application
import com.example.networkmonitor.service.NotificationHelper

class NetworkMonitorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
