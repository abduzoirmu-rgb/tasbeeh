package com.tasbeeh.app.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.tasbeeh.app.domain.model.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder) {
        if (!reminder.isEnabled) {
            cancel(reminder)
            return
        }
        val days = reminder.daysOfWeek
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
        val triggerAt = nextAlarmTime(reminder.timeHour, reminder.timeMinute, days)
        val showIntent = PendingIntent.getActivity(
            context, 0,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, showIntent),
            buildPendingIntent(reminder)
        )
    }

    fun cancel(reminder: Reminder) {
        alarmManager.cancel(buildPendingIntent(reminder))
    }

    fun scheduleAll(reminders: List<Reminder>) = reminders.forEach { schedule(it) }

    private fun buildPendingIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_FIRE
            putExtra(ReminderReceiver.EXTRA_ID,     reminder.id)
            putExtra(ReminderReceiver.EXTRA_TITLE,  reminder.title)
            putExtra(ReminderReceiver.EXTRA_HOUR,   reminder.timeHour)
            putExtra(ReminderReceiver.EXTRA_MINUTE, reminder.timeMinute)
            putExtra(ReminderReceiver.EXTRA_DAYS,   reminder.daysOfWeek)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        /** Returns the epoch-millis of the next occurrence of [hour]:[minute] on any of [days]. */
        fun nextAlarmTime(hour: Int, minute: Int, days: Set<Int>): Long {
            val now = Calendar.getInstance()
            val base = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (days.isEmpty()) {
                // One-shot: if already past, fire tomorrow
                if (base.timeInMillis <= now.timeInMillis) base.add(Calendar.DAY_OF_MONTH, 1)
                return base.timeInMillis
            }
            // Repeating: find nearest upcoming day
            for (offset in 0..7) {
                val candidate = (base.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
                if (candidate.timeInMillis > now.timeInMillis &&
                    calToAppDay(candidate.get(Calendar.DAY_OF_WEEK)) in days
                ) {
                    return candidate.timeInMillis
                }
            }
            // Fallback: next week same day
            return base.timeInMillis + 7L * 24 * 60 * 60 * 1000
        }

        // Calendar: 1=Sun, 2=Mon … 7=Sat  →  App: 1=Mon … 7=Sun
        private fun calToAppDay(calDay: Int): Int = when (calDay) {
            Calendar.MONDAY    -> 1
            Calendar.TUESDAY   -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY  -> 4
            Calendar.FRIDAY    -> 5
            Calendar.SATURDAY  -> 6
            else               -> 7
        }
    }
}
