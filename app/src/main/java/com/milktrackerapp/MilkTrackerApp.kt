package com.milktrackerapp

import android.app.Application
import com.milktrackerapp.data.repository.BottleRepository
import com.milktrackerapp.data.repository.ReminderRepository
import com.milktrackerapp.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MilkTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)

        // 启动时执行一次自动清理（每天只运行一次）
        CoroutineScope(Dispatchers.IO).launch {
            val reminderRepo = ReminderRepository(this@MilkTrackerApp)
            val settings = reminderRepo.getSettings()
            if (!settings.autoCleanEnabled) return@launch

            val prefs = getSharedPreferences("auto_clean_check", MODE_PRIVATE)
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            if (prefs.getString("last_clean_date", null) == today) return@launch

            BottleRepository(this@MilkTrackerApp).clearRecordsOlderThan(settings.autoCleanDays)
            prefs.edit().putString("last_clean_date", today).apply()
        }
    }
}
