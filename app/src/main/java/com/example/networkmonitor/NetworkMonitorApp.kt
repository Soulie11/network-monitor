package com.example.networkmonitor

import android.app.Application
import com.example.networkmonitor.di.AppContainer
import com.example.networkmonitor.service.NotificationHelper

class NetworkMonitorApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)
    }
}
