package com.tasbeeh.app.worker

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tasbeeh.app.MainActivity
import com.tasbeeh.app.domain.repository.ReminderRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReminderEntryPoint {
        fun reminderRepository(): ReminderRepository
        fun reminderScheduler(): ReminderScheduler
    }

    companion object {
        const val ACTION_FIRE  = "com.tasbeeh.app.REMINDER_FIRE"
        const val EXTRA_ID     = "r_id"
        const val EXTRA_TITLE  = "r_title"
        const val EXTRA_HOUR   = "r_hour"
        const val EXTRA_MINUTE = "r_minute"
        const val EXTRA_DAYS   = "r_days"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FIRE                   -> handleFire(context, intent)
            Intent.ACTION_BOOT_COMPLETED  -> rescheduleAll(context)
        }
    }

    // -----------------------------------------------------------------------

    private fun handleFire(context: Context, intent: Intent) {
        val id     = intent.getIntExtra(EXTRA_ID, 0)
        val title  = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val hour   = intent.getIntExtra(EXTRA_HOUR, 0)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
        val days   = intent.getStringExtra(EXTRA_DAYS) ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val nm = context.getSystemService(NotificationManager::class.java)
            if (nm?.areNotificationsEnabled() == false) return
        }

        showNotification(context, id, title)

        // Reschedule next occurrence for repeating reminders
        val daySet = days.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        if (daySet.isNotEmpty()) {
            scheduleNextOccurrence(context, id, title, hour, minute, days, daySet)
        }
    }

    private fun scheduleNextOccurrence(
        context: Context,
        id: Int,
        title: String,
        hour: Int,
        minute: Int,
        daysRaw: String,
        daySet: Set<Int>
    ) {
        val nextTime = ReminderScheduler.nextAlarmTime(hour, minute, daySet)
        val fireIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_ID,     id)
            putExtra(EXTRA_TITLE,  title)
            putExtra(EXTRA_HOUR,   hour)
            putExtra(EXTRA_MINUTE, minute)
            putExtra(EXTRA_DAYS,   daysRaw)
        }
        val pending = PendingIntent.getBroadcast(
            context, id, fireIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val showIntent = PendingIntent.getActivity(
            context, 0,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(nextTime, showIntent), pending)
    }

    private fun showNotification(context: Context, id: Int, title: String) {
        val openApp = PendingIntent.getActivity(
            context, id,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val displayTitle = title.ifBlank { "Тасбех • Tasbeeh" }
        val notification = NotificationCompat.Builder(context, ReminderWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(displayTitle)
            .setContentText("Время для зикра 🤲")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("سُبْحَانَ اللَّهِ\n$displayTitle 🤲")
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, notification)
    }

    // -----------------------------------------------------------------------

    private fun rescheduleAll(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ep = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    ReminderEntryPoint::class.java
                )
                val reminders = ep.reminderRepository().getReminders().first()
                val scheduler = ep.reminderScheduler()
                scheduler.scheduleAll(reminders)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
