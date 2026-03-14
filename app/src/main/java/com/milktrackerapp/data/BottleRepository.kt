package com.milktrackerapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class BottleRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    fun getAllRecords(): List<BottleRecord> {
        val json = prefs.getString(KEY_RECORDS, null)
        return if (json != null) {
            val type = object : TypeToken<List<BottleRecord>>() {}.type
            val records: List<BottleRecord> = gson.fromJson(json, type)
            // 优先按喝奶时间降序排列，如果时间相同则按创建时间降序排列
            records.sortedWith(compareByDescending<BottleRecord> { it.timestamp }.thenByDescending { it.createdAt })
        } else {
            emptyList()
        }
    }

    fun addRecord(record: BottleRecord) {
        val records = getAllRecords().toMutableList()
        records.add(record)
        // 优先按喝奶时间降序保存，如果时间相同则按创建时间降序保存
        val sortedRecords = records.sortedWith(compareByDescending<BottleRecord> { it.timestamp }.thenByDescending { it.createdAt })
        saveRecords(sortedRecords)
        // 重新计算并更新最后一次记奶时间
        updateLastFeedTime()
    }

    fun deleteRecord(id: String) {
        val records = getAllRecords().toMutableList()
        records.removeIf { it.id == id }
        saveRecords(records)
        // 重新计算并更新最后一次记奶时间
        updateLastFeedTime()
    }

    fun updateRecord(id: String, newAmount: Int) {
        val records = getAllRecords().toMutableList()
        val index = records.indexOfFirst { it.id == id }
        if (index != -1) {
            records[index] = records[index].copy(amount = newAmount)
            saveRecords(records)
            // 重新计算并更新最后一次记奶时间
            updateLastFeedTime()
        }
    }

    fun getTotalToday(): Int {
        val records = getAllRecords()
        val todayStart = getTodayStart()
        return records.filter { it.timestamp >= todayStart }.sumOf { it.amount }
    }

    private fun getTodayStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getLastFeedTime(): Long? {
        return prefs.getLong(KEY_LAST_FEED_TIME, 0).takeIf { it > 0 }
    }

    fun getBreakDuration(): Long {
        val lastFeedTime = getLastFeedTime() ?: return 0
        val currentTime = System.currentTimeMillis()
        return currentTime - lastFeedTime
    }

    private fun updateLastFeedTime() {
        val records = getAllRecords()
        val lastTimestamp = records.maxByOrNull { it.timestamp }?.timestamp ?: 0
        prefs.edit().putLong(KEY_LAST_FEED_TIME, lastTimestamp).apply()
    }

    private fun saveRecords(records: List<BottleRecord>) {
        val json = gson.toJson(records)
        prefs.edit().putString(KEY_RECORDS, json).apply()
    }

    fun clearAllRecords() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "bottle_records"
        private const val KEY_RECORDS = "records"
        private const val KEY_LAST_FEED_TIME = "last_feed_time"
    }
}