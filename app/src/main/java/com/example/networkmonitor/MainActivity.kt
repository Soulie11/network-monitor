package com.example.networkmonitor

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.networkmonitor.data.local.AppDatabase
import com.example.networkmonitor.data.repository.NetworkRepository
import com.example.networkmonitor.service.NetworkStatusTracker
import com.example.networkmonitor.service.NotificationHelper
import com.example.networkmonitor.ui.screens.MainScreen
import com.example.networkmonitor.ui.screens.MainViewModel
import com.example.networkmonitor.ui.theme.NetworkMonitorTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        val database = AppDatabase.getInstance(applicationContext)
        val tracker = NetworkStatusTracker(applicationContext)
        val repository = NetworkRepository(
            networkDao = database.networkLogDao(),
            tracker = tracker,
            notificationHelper = NotificationHelper(applicationContext)
        )

        repository.startMonitoring()

        val viewModel = MainViewModel(repository)

        setContent {
            NetworkMonitorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}