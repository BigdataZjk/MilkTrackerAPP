package com.milktrackerapp.ui.record

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.milktrackerapp.data.model.BottleRecord
import com.milktrackerapp.data.repository.BottleRepository
import com.milktrackerapp.data.repository.ReminderRepository
import com.milktrackerapp.notification.FeedReminderReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val bottleRepo = BottleRepository(application)
    private val reminderRepo = ReminderRepository(application)
    private val presetPrefs: SharedPreferences =
        application.getSharedPreferences("preset_amounts", Context.MODE_PRIVATE)

    val records: StateFlow<List<BottleRecord>> = bottleRepo.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalToday = MutableStateFlow(0)
    val totalToday: StateFlow<Int> = _totalToday

    private val _countToday = MutableStateFlow(0)
    val countToday: StateFlow<Int> = _countToday

    private val _lastRecord = MutableStateFlow<BottleRecord?>(null)
    val lastRecord: StateFlow<BottleRecord?> = _lastRecord

    private val _presetAmounts = MutableStateFlow(loadPresets())
    val presetAmounts: StateFlow<List<Int>> = _presetAmounts

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            _totalToday.value = bottleRepo.getTotalToday()
            _countToday.value = bottleRepo.getCountToday()
            _lastRecord.value = bottleRepo.getLastRecord()
        }
    }

    fun addRecord(amount: Int, timestamp: Long) {
        viewModelScope.launch {
            bottleRepo.addRecord(amount, timestamp)
            refreshStats()
            scheduleReminder()
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            bottleRepo.deleteRecord(id)
            refreshStats()
        }
    }

    fun updateRecord(id: String, amount: Int) {
        viewModelScope.launch {
            bottleRepo.updateRecord(id, amount)
            refreshStats()
        }
    }

    fun savePresetAmounts(amounts: List<Int>) {
        _presetAmounts.value = amounts
        presetPrefs.edit().putString("preset_amounts", amounts.joinToString(",")).apply()
    }

    fun addPresetAmount(amount: Int) {
        savePresetAmounts(_presetAmounts.value.toMutableList().apply { add(amount) })
    }

    fun updatePresetAmount(index: Int, amount: Int) {
        savePresetAmounts(_presetAmounts.value.toMutableList().apply { set(index, amount) })
    }

    fun removePresetAmount(index: Int) {
        savePresetAmounts(_presetAmounts.value.toMutableList().apply { removeAt(index) })
    }

    private fun loadPresets(): List<Int> {
        val raw = presetPrefs.getString("preset_amounts", null)
        return if (raw != null) {
            raw.split(",").mapNotNull { it.toIntOrNull() }
        } else {
            listOf(30, 60, 90, 120, 150, 180)
        }
    }

    private suspend fun scheduleReminder() {
        val settings = reminderRepo.getSettings()
        if (settings.enabled) {
            FeedReminderReceiver.schedule(getApplication(), settings.intervalMinutes)
        }
    }
}
