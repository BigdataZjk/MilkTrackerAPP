package com.milktrackerapp.data.model

data class ReminderSettings(
    val id: Int = 1,
    val enabled: Boolean = true,
    val intervalMinutes: Int = 210,
    val message: String = "芃芃饿了3个半小时了",
    val autoCleanEnabled: Boolean = false,
    val autoCleanDays: Int = 30
)
