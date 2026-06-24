package com.tasbeeh.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.tasbeeh.app.data.local.datastore.SettingsDataStore
import com.tasbeeh.app.domain.model.Settings
import com.tasbeeh.app.domain.repository.SettingsRepository
import com.tasbeeh.app.presentation.localization.LocalStrings
import com.tasbeeh.app.presentation.localization.stringsFor
import com.tasbeeh.app.presentation.navigation.TasbeehNavHost
import com.tasbeeh.app.presentation.theme.TasbeehTheme
import com.tasbeeh.app.worker.ReminderWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = Settings()
            )
            val strings = stringsFor(settings.language)

            TasbeehTheme(darkTheme = settings.isDarkTheme) {
                CompositionLocalProvider(
                    LocalStrings provides strings
                ) {
                    TasbeehNavHost()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cancelReminder()
        saveLastOpenTime()
    }

    override fun onPause() {
        super.onPause()
        scheduleReminder()
    }

    private fun scheduleReminder() {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)
            .addTag(ReminderWorker.WORK_NAME)
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            ReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun cancelReminder() {
        WorkManager.getInstance(this).cancelUniqueWork(ReminderWorker.WORK_NAME)
    }

    private fun saveLastOpenTime() {
        CoroutineScope(Dispatchers.IO).launch {
            settingsDataStore.updateLastOpenTime(System.currentTimeMillis())
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
