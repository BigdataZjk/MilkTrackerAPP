package com.milktrackerapp.data

import java.util.UUID

data class BottleRecord(
    val id: String = UUID.randomUUID().toString(),
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class AppSettings(
    val lastFeedTime: Long? = null,
    val breakDuration: Long = 0
)