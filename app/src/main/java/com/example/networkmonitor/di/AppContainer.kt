package com.example.networkmonitor.di

import android.content.Context
import com.example.networkmonitor.data.local.AppDatabase
import com.example.networkmonitor.data.repository.NetworkRepository
import com.example.networkmonitor.service.NetworkStatusTracker
import com.example.networkmonitor.service.NotificationHelper

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(appContext)
    }

    private val tracker: NetworkStatusTracker by lazy {
        NetworkStatusTracker(appContext)
    }

    private val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(appContext)
    }

    val networkRepository: NetworkRepository by lazy {
        NetworkRepository(
            networkDao = database.networkLogDao(),
            tracker = tracker,
            notificationHelper = notificationHelper
        )
    }
}
