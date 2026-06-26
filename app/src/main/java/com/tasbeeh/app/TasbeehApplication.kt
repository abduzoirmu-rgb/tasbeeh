package com.tasbeeh.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.tasbeeh.app.data.assets.DuaDataLoader
import com.tasbeeh.app.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TasbeehApplication : Application() {

    @Inject lateinit var duaDataLoader: DuaDataLoader

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        appScope.launch { duaDataLoader.seedIfEmpty() }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ReminderWorker.CHANNEL_ID,
            "Напоминания о зикре",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Напоминает вернуться к зикру"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
