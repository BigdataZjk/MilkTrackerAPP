package com.milktrackerapp.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milktrackerapp.ui.record.components.TopInfoCard
import com.milktrackerapp.ui.record.components.QuickRecordPanel
import com.milktrackerapp.ui.record.components.TodayRecordList
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun RecordScreen(
    modifier: Modifier = Modifier,
    viewModel: RecordViewModel = viewModel()
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val totalToday by viewModel.totalToday.collectAsStateWithLifecycle()
    val countToday by viewModel.countToday.collectAsStateWithLifecycle()
    val lastRecord by viewModel.lastRecord.collectAsStateWithLifecycle()
    val presetAmounts by viewModel.presetAmounts.collectAsStateWithLifecycle()

    // 是否展开时间选择器
    var showTimePicker by remember { mutableStateOf(false) }
    // 选中的小时和分钟 — 初始为当前时间
    var selectedHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }

    // 实时追踪当前时间（不展开选择器时每10秒更新一次）
    LaunchedEffect(showTimePicker) {
        while (!showTimePicker) {
            val now = Calendar.getInstance()
            selectedHour = now.get(Calendar.HOUR_OF_DAY)
            selectedMinute = now.get(Calendar.MINUTE)
            delay(10_000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TopInfoCard(
            lastRecord = lastRecord,
            totalToday = totalToday,
            countToday = countToday
        )

        QuickRecordPanel(
            presetAmounts = presetAmounts,
            showTimePicker = showTimePicker,
            selectedHour = selectedHour,
            selectedMinute = selectedMinute,
            onToggleTimePicker = {
                // 展开时刷新为当前时间
                if (!showTimePicker) {
                    val now = Calendar.getInstance()
                    selectedHour = now.get(Calendar.HOUR_OF_DAY)
                    selectedMinute = now.get(Calendar.MINUTE)
                }
                showTimePicker = !showTimePicker
            },
            onHourChange = { selectedHour = it },
            onMinuteChange = { selectedMinute = it },
            onPresetAdd = { viewModel.addPresetAmount(it) },
            onPresetUpdate = { index, amount -> viewModel.updatePresetAmount(index, amount) },
            onPresetDelete = { viewModel.removePresetAmount(it) },
            onRecord = { amount ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, selectedHour)
                cal.set(Calendar.MINUTE, selectedMinute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val timestamp = cal.timeInMillis
                val now = System.currentTimeMillis()
                val finalTimestamp = if (timestamp > now) now else timestamp
                viewModel.addRecord(amount, finalTimestamp)
            }
        )

        TodayRecordList(
            records = records,
            onDelete = { viewModel.deleteRecord(it) },
            onUpdate = { id, amount -> viewModel.updateRecord(id, amount) }
        )
    }
}
