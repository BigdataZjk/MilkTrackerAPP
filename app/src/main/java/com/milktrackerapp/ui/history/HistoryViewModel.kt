package com.milktrackerapp.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.milktrackerapp.data.model.BottleRecord
import com.milktrackerapp.data.model.ReminderSettings
import com.milktrackerapp.data.repository.BottleRepository
import com.milktrackerapp.data.repository.ReminderRepository
import com.milktrackerapp.notification.FeedReminderReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val bottleRepo = BottleRepository(application)
    private val reminderRepo = ReminderRepository(application)

    val reminderSettings: StateFlow<ReminderSettings> = reminderRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReminderSettings())

    // 图表
    private val _chartDays = MutableStateFlow(7)
    val chartDays: StateFlow<Int> = _chartDays

    private val _dailyTotals = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val dailyTotals: StateFlow<List<Pair<String, Int>>> = _dailyTotals

    private val _avgDailyAmount = MutableStateFlow(0)
    val avgDailyAmount: StateFlow<Int> = _avgDailyAmount

    private val _avgDailyCount = MutableStateFlow(0f)
    val avgDailyCount: StateFlow<Float> = _avgDailyCount

    private val _totalToday = MutableStateFlow(0)
    val totalToday: StateFlow<Int> = _totalToday

    // 全局统计
    private val _totalRecordCount = MutableStateFlow(0)
    val totalRecordCount: StateFlow<Int> = _totalRecordCount

    private val _totalRecordDays = MutableStateFlow(0)
    val totalRecordDays: StateFlow<Int> = _totalRecordDays

    // 分页
    companion object {
        const val PAGE_SIZE = 10
    }

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    private val _pagedRecords = MutableStateFlow<List<BottleRecord>>(emptyList())
    val pagedRecords: StateFlow<List<BottleRecord>> = _pagedRecords

    // 清除旧数据的天数
    private val _clearDays = MutableStateFlow(30)
    val clearDays: StateFlow<Int> = _clearDays

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val days = _chartDays.value
            _dailyTotals.value = bottleRepo.getDailyTotals(days)
            _avgDailyAmount.value = bottleRepo.getAvgDailyAmount(days)
            _avgDailyCount.value = bottleRepo.getAvgDailyCount(days)
            _totalToday.value = bottleRepo.getTotalToday()
            _totalRecordCount.value = bottleRepo.getTotalCount()
            _totalRecordDays.value = bottleRepo.getUniqueDayCount()

            // 加载所有记录用于分页
            val todayStart = getDayStart(0)
            val todayEnd = getDayStart(1)
            val allToday = bottleRepo.getRecordsByDateRange(todayStart, todayEnd)
            val total = allToday.size
            _totalPages.value = ((total + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
            val page = _currentPage.value.coerceIn(0, _totalPages.value - 1)
            _currentPage.value = page
            _pagedRecords.value = allToday.drop(page * PAGE_SIZE).take(PAGE_SIZE)
        }
    }

    fun setPage(page: Int) {
        _currentPage.value = page
        refresh()
    }

    fun setChartDays(days: Int) {
        _chartDays.value = days
        refresh()
    }

    fun updateReminderInterval(minutes: Int) {
        viewModelScope.launch {
            reminderRepo.updateInterval(minutes)
            val settings = reminderRepo.getSettings()
            if (settings.enabled) {
                FeedReminderReceiver.cancel(getApplication())
                FeedReminderReceiver.schedule(getApplication(), minutes)
            }
        }
    }

    fun updateReminderMessage(message: String) {
        viewModelScope.launch { reminderRepo.updateMessage(message) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            reminderRepo.setEnabled(enabled)
            if (enabled) {
                val settings = reminderRepo.getSettings()
                FeedReminderReceiver.schedule(getApplication(), settings.intervalMinutes)
            } else {
                FeedReminderReceiver.cancel(getApplication())
            }
        }
    }

    fun setAutoCleanEnabled(enabled: Boolean) {
        viewModelScope.launch { reminderRepo.setAutoCleanEnabled(enabled) }
    }

    fun setAutoCleanDays(days: Int) {
        viewModelScope.launch { reminderRepo.setAutoCleanDays(days) }
    }

    fun performAutoCleanIfNeeded() {
        viewModelScope.launch {
            val settings = reminderRepo.getSettings()
            if (!settings.autoCleanEnabled) return@launch
            // 每天只执行一次
            val prefs = getApplication<android.app.Application>()
                .getSharedPreferences("auto_clean_check", android.content.Context.MODE_PRIVATE)
            val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
                .format(java.util.Date())
            val lastRun = prefs.getString("last_clean_date", null)
            if (lastRun == today) return@launch
            bottleRepo.clearRecordsOlderThan(settings.autoCleanDays)
            prefs.edit().putString("last_clean_date", today).apply()
            refresh()
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            bottleRepo.deleteRecord(id)
            // 删除后可能总页数减少，修正当前页
            val todayStart = getDayStart(0)
            val todayEnd = getDayStart(1)
            val allToday = bottleRepo.getRecordsByDateRange(todayStart, todayEnd)
            val newTotal = ((allToday.size + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
            if (_currentPage.value >= newTotal) {
                _currentPage.value = (newTotal - 1).coerceAtLeast(0)
            }
            refresh()
        }
    }

    fun setClearDays(days: Int) {
        _clearDays.value = days
    }

    fun clearRecordsOlderThan(days: Int) {
        viewModelScope.launch {
            bottleRepo.clearRecordsOlderThan(days)
            _currentPage.value = 0
            refresh()
        }
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
}
