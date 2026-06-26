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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tasbeeh.app.data.local.datastore.SettingsDataStore
import com.tasbeeh.app.domain.model.Settings
import com.tasbeeh.app.domain.repository.SettingsRepository
import com.tasbeeh.app.presentation.localization.LocalStrings
import com.tasbeeh.app.presentation.localization.stringsFor
import com.tasbeeh.app.presentation.navigation.TasbeehNavHost
import com.tasbeeh.app.presentation.splash.SplashScreen
import com.tasbeeh.app.presentation.theme.TasbeehTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var settingsDataStore: SettingsDataStore
    @Inject lateinit var reminderScheduler: com.tasbeeh.app.worker.ReminderScheduler
    @Inject lateinit var getRemindersUseCase: com.tasbeeh.app.domain.usecase.GetRemindersUseCase

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        rescheduleReminders()

        setContent {
            val settings by settingsRepository.getSettings().collectAsStateWithLifecycle(
                initialValue = Settings()
            )
            val strings = stringsFor(settings.language)
            var splashDone by remember { mutableStateOf(false) }

            TasbeehTheme(darkTheme = settings.isDarkTheme) {
                CompositionLocalProvider(LocalStrings provides strings) {
                    if (splashDone) {
                        TasbeehNavHost()
                    } else {
                        SplashScreen(onFinished = { splashDone = true })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        saveLastOpenTime()
    }

    private fun saveLastOpenTime() {
        CoroutineScope(Dispatchers.IO).launch {
            settingsDataStore.updateLastOpenTime(System.currentTimeMillis())
        }
    }

    private fun rescheduleReminders() {
        CoroutineScope(Dispatchers.IO).launch {
            val reminders = getRemindersUseCase().first()
            reminderScheduler.scheduleAll(reminders)
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
