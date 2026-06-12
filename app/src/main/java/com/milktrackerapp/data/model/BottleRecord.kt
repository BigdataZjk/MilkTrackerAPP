package com.milktrackerapp.data.model

data class BottleRecord(
    val id: String,
    val amount: Int,
    val timestamp: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = ""
)
