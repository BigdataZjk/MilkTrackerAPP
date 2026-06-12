package com.milktrackerapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milktrackerapp.data.model.BottleRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.UUID

class BottleRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _records = MutableStateFlow<List<BottleRecord>>(emptyList())
    val allRecords: Flow<List<BottleRecord>> = _records.asStateFlow()

    init {
        _records.value = loadAll()
    }

    suspend fun addRecord(amount: Int, timestamp: Long, note: String = "") {
        val record = BottleRecord(
            id = UUID.randomUUID().toString(),
            amount = amount,
            timestamp = timestamp,
            createdAt = System.currentTimeMillis(),
            note = note
        )
        val records = _records.value.toMutableList()
        records.add(0, record)
        _records.value = records
        saveAll(records)
    }

    suspend fun deleteRecord(id: String) {
        val records = _records.value.toMutableList()
        records.removeAll { it.id == id }
        _records.value = records
        saveAll(records)
    }

    suspend fun updateRecord(id: String, amount: Int) {
        val records = _records.value.map {
            if (it.id == id) it.copy(amount = amount) else it
        }
        _records.value = records
        saveAll(records)
    }

    suspend fun getLastRecord(): BottleRecord? = _records.value.firstOrNull()

    suspend fun getTotalToday(): Int {
        val todayStart = getDayStart(0)
        return _records.value.filter { it.timestamp >= todayStart }.sumOf { it.amount }
    }

    suspend fun getCountToday(): Int {
        val todayStart = getDayStart(0)
        return _records.value.count { it.timestamp >= todayStart }
    }

    suspend fun getDailyTotals(days: Int): List<Pair<String, Int>> {
        val results = mutableListOf<Pair<String, Int>>()
        for (i in days - 1 downTo 0) {
            val dayStart = getDayStart(-i)
            val dayEnd = getDayStart(-i + 1)
            val total = _records.value.filter { it.timestamp in dayStart until dayEnd }.sumOf { it.amount }
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val label = "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
            results.add(label to total)
        }
        return results
    }

    suspend fun getRecordsByDateRange(start: Long, end: Long): List<BottleRecord> =
        _records.value.filter { it.timestamp in start until end }.sortedByDescending { it.timestamp }

    suspend fun getAvgDailyAmount(days: Int): Int {
        val totals = getDailyTotals(days)
        if (totals.isEmpty()) return 0
        return totals.sumOf { it.second } / days
    }

    suspend fun getAvgDailyCount(days: Int): Float {
        if (days <= 0) return 0f
        var totalCount = 0
        for (i in 0 until days) {
            val dayStart = getDayStart(-i)
            val dayEnd = getDayStart(-i + 1)
            totalCount += _records.value.count { it.timestamp in dayStart until dayEnd }
        }
        return totalCount.toFloat() / days
    }

    suspend fun deleteAll() {
        _records.value = emptyList()
        prefs.edit().clear().apply()
    }

    suspend fun getTotalCount(): Int = _records.value.size

    suspend fun getUniqueDayCount(): Int {
        val fmt = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        return _records.value.map { fmt.format(java.util.Date(it.timestamp)) }.distinct().size
    }

    suspend fun clearRecordsOlderThan(days: Int) {
        val cutoff = System.currentTimeMillis() - days * 86_400_000L
        val remaining = _records.value.filter { it.timestamp >= cutoff }
        _records.value = remaining
        saveAll(remaining)
    }

    private fun getDayStart(dayOffset: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun loadAll(): List<BottleRecord> {
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<BottleRecord>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveAll(records: List<BottleRecord>) {
        prefs.edit().putString(KEY_RECORDS, gson.toJson(records)).apply()
    }

    companion object {
        private const val PREFS_NAME = "milk_tracker_data"
        private const val KEY_RECORDS = "records"
    }
}
