package com.milktrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.milktrackerapp.data.BottleRecord
import com.milktrackerapp.data.BottleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BottleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BottleRepository = BottleRepository(application)

    private val _records = MutableStateFlow<List<BottleRecord>>(emptyList())
    val records: StateFlow<List<BottleRecord>> = _records

    private val _totalToday = MutableStateFlow(0)
    val totalToday: StateFlow<Int> = _totalToday

    private val _lastFeedTime = MutableStateFlow<Long?>(null)
    val lastFeedTime: StateFlow<Long?> = _lastFeedTime

    private val _breakDuration = MutableStateFlow(0L)
    val breakDuration: StateFlow<Long> = _breakDuration

    init {
        loadRecords()
    }

    fun loadRecords() {
        viewModelScope.launch {
            _records.value = repository.getAllRecords()
            _totalToday.value = repository.getTotalToday()
            _lastFeedTime.value = repository.getLastFeedTime()
            _breakDuration.value = repository.getBreakDuration()
        }
    }

    fun addRecord(amount: Int, timestamp: Long = System.currentTimeMillis(), note: String = "") {
        viewModelScope.launch {
            val record = BottleRecord(
                amount = amount, 
                timestamp = timestamp, 
                createdAt = System.currentTimeMillis(),
                note = note
            )
            repository.addRecord(record)
            loadRecords()
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            repository.deleteRecord(id)
            loadRecords()
        }
    }

    fun updateRecord(id: String, newAmount: Int) {
        viewModelScope.launch {
            repository.updateRecord(id, newAmount)
            loadRecords()
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.clearAllRecords()
            loadRecords()
        }
    }
}