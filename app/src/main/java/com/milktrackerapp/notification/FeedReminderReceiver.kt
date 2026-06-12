package com.milktrackerapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.milktrackerapp.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeedReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMIND) return

        CoroutineScope(Dispatchers.IO).launch {
            val repo = ReminderRepository(context)
            val settings = repo.getSettings()

            if (settings.enabled) {
                NotificationHelper.show(context, settings.message)
            }
        }
    }

    companion object {
        const val ACTION_REMIND = "com.milktrackerapp.ACTION_FEED_REMIND"
        private const val REQUEST_CODE = 2001

        fun schedule(context: Context, delayMinutes: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            cancel(context)

            if (delayMinutes <= 0) return

            val triggerTime = System.currentTimeMillis() + delayMinutes * 60_000L
            val intent = Intent(context, FeedReminderReceiver::class.java).apply {
                action = ACTION_REMIND
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
            }
        }

        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, FeedReminderReceiver::class.java).apply {
                action = ACTION_REMIND
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
