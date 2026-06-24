package com.tasbeeh.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tasbeeh.app.MainActivity

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        createChannelIfNeeded(context)
        sendNotification(context)
        return Result.success()
    }

    private fun createChannelIfNeeded(ctx: Context) {
        val manager = ctx.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о зикре",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминает читать зикр"
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(ctx: Context) {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val manager = ctx.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("التسبيح • Тасбех • Tasbeeh")
            .setContentText("Не забудьте о зикре сегодня 🤲")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("سُبْحَانَ اللَّهِ\nНе забудьте о зикре сегодня 🤲")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "tasbeeh_reminder"
        const val CHANNEL_ID = "tasbeeh_reminder_channel"
        const val NOTIFICATION_ID = 1001
    }
}
