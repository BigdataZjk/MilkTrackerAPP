package com.milktrackerapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.milktrackerapp.data.model.ReminderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReminderRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: Flow<ReminderSettings> = _settings.asStateFlow()

    suspend fun getSettings(): ReminderSettings = _settings.value

    suspend fun saveSettings(settings: ReminderSettings) {
        _settings.value = settings
        prefs.edit()
            .putBoolean(KEY_ENABLED, settings.enabled)
            .putInt(KEY_INTERVAL, settings.intervalMinutes)
            .putString(KEY_MESSAGE, settings.message)
            .putBoolean(KEY_AUTO_CLEAN_ENABLED, settings.autoCleanEnabled)
            .putInt(KEY_AUTO_CLEAN_DAYS, settings.autoCleanDays)
            .apply()
    }

    suspend fun updateInterval(minutes: Int) {
        saveSettings(_settings.value.copy(intervalMinutes = minutes))
    }

    suspend fun updateMessage(message: String) {
        saveSettings(_settings.value.copy(message = message))
    }

    suspend fun setEnabled(enabled: Boolean) {
        saveSettings(_settings.value.copy(enabled = enabled))
    }

    suspend fun setAutoCleanEnabled(enabled: Boolean) {
        saveSettings(_settings.value.copy(autoCleanEnabled = enabled))
    }

    suspend fun setAutoCleanDays(days: Int) {
        saveSettings(_settings.value.copy(autoCleanDays = days))
    }

    private fun load(): ReminderSettings = ReminderSettings(
        enabled = prefs.getBoolean(KEY_ENABLED, true),
        intervalMinutes = prefs.getInt(KEY_INTERVAL, 210),
        message = prefs.getString(KEY_MESSAGE, "芃芃饿了3个半小时了") ?: "芃芃饿了3个半小时了",
        autoCleanEnabled = prefs.getBoolean(KEY_AUTO_CLEAN_ENABLED, false),
        autoCleanDays = prefs.getInt(KEY_AUTO_CLEAN_DAYS, 30)
    )

    companion object {
        private const val PREFS_NAME = "milk_tracker_reminder"
        private const val KEY_ENABLED = "reminder_enabled"
        private const val KEY_INTERVAL = "reminder_interval"
        private const val KEY_MESSAGE = "reminder_message"
        private const val KEY_AUTO_CLEAN_ENABLED = "auto_clean_enabled"
        private const val KEY_AUTO_CLEAN_DAYS = "auto_clean_days"
    }
}
