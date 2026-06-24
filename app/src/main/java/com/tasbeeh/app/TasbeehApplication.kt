package com.tasbeeh.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.tasbeeh.app.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TasbeehApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
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
