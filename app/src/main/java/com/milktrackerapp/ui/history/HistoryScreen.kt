package com.milktrackerapp.ui.history

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milktrackerapp.ui.history.components.ChartView
import com.milktrackerapp.ui.history.components.StatsOverview
import com.milktrackerapp.ui.history.components.HistoryRecordList
import com.milktrackerapp.ui.history.components.ReminderSettingsCard
import com.milktrackerapp.ui.theme.Tile
import com.milktrackerapp.ui.theme.Ink
import com.milktrackerapp.ui.theme.Slate
import com.milktrackerapp.ui.theme.Pebble
import com.milktrackerapp.ui.theme.Rule
import com.milktrackerapp.ui.theme.Navy
import com.milktrackerapp.ui.theme.NavyMuted

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val dailyTotals by viewModel.dailyTotals.collectAsStateWithLifecycle()
    val avgDailyAmount by viewModel.avgDailyAmount.collectAsStateWithLifecycle()
    val avgDailyCount by viewModel.avgDailyCount.collectAsStateWithLifecycle()
    val totalToday by viewModel.totalToday.collectAsStateWithLifecycle()
    val pagedRecords by viewModel.pagedRecords.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val totalPages by viewModel.totalPages.collectAsStateWithLifecycle()
    val reminderSettings by viewModel.reminderSettings.collectAsStateWithLifecycle()
    val chartDays by viewModel.chartDays.collectAsStateWithLifecycle()
    val clearDays by viewModel.clearDays.collectAsStateWithLifecycle()
    val totalRecordCount by viewModel.totalRecordCount.collectAsStateWithLifecycle()
    val totalRecordDays by viewModel.totalRecordDays.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var dialogDaysInput by remember { mutableStateOf(clearDays.toString()) }

    // 进入页面时执行一次自动清理
    LaunchedEffect(Unit) { viewModel.performAutoCleanIfNeeded() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. 统计概览
        StatsOverview(
            totalToday = totalToday,
            avgDailyAmount = avgDailyAmount,
            avgDailyCount = avgDailyCount
        )

        // 2. 喂奶提醒
        ReminderSettingsCard(
            settings = reminderSettings,
            onEnabledChange = { viewModel.setReminderEnabled(it) },
            onIntervalChange = { viewModel.updateReminderInterval(it) },
            onMessageChange = { viewModel.updateReminderMessage(it) }
        )

        // 3. 数据管理
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Tile),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("数据管理", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text(" ${totalRecordCount}条(${totalRecordDays}天)", style = MaterialTheme.typography.labelSmall, color = Pebble)
                    }
                }

                // 自动清理
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("自动清理", style = MaterialTheme.typography.labelLarge, color = Ink, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = reminderSettings.autoCleanEnabled,
                        onCheckedChange = { viewModel.setAutoCleanEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Tile, checkedTrackColor = Navy, uncheckedThumbColor = Tile, uncheckedTrackColor = Pebble.copy(alpha = 0.3f))
                    )
                }

                if (reminderSettings.autoCleanEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("每天自动删除 ${reminderSettings.autoCleanDays} 天前的数据", style = MaterialTheme.typography.bodySmall, color = Slate)
                        TextButton(onClick = { dialogDaysInput = reminderSettings.autoCleanDays.toString(); showSettingsDialog = true }) {
                            Text("${reminderSettings.autoCleanDays}", color = Navy, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = Rule, thickness = 1.dp)

                // 手动清理
                Text("手动清理", style = MaterialTheme.typography.labelLarge, color = Ink, fontWeight = FontWeight.Medium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("清除 ${clearDays} 天前的数据", style = MaterialTheme.typography.bodySmall, color = Slate)
                    TextButton(onClick = { dialogDaysInput = clearDays.toString(); showSettingsDialog = true }) {
                        Text("${clearDays}", color = Navy, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { dialogDaysInput = clearDays.toString(); showClearConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyMuted, contentColor = Navy),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("执行清除", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                }
            }
        }

        // 4. 趋势图
        ChartView(
            dailyTotals = dailyTotals,
            selectedDays = chartDays,
            onDaysChange = { viewModel.setChartDays(it) }
        )

        // 5. 历史记录
        HistoryRecordList(
            records = pagedRecords,
            currentPage = currentPage,
            totalPages = totalPages,
            onPageChange = { viewModel.setPage(it) },
            onDelete = { viewModel.deleteRecord(it) }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // "设置" 弹窗
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("设置保留天数", color = Ink) },
            text = {
                OutlinedTextField(
                    value = dialogDaysInput,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) dialogDaysInput = it },
                    label = { Text("天数") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Navy, focusedLabelColor = Navy)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val days = dialogDaysInput.toIntOrNull() ?: 30
                    viewModel.setClearDays(days)
                    showSettingsDialog = false
                }) { Text("确认", color = Navy) }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("取消", color = Slate) }
            }
        )
    }

    // "清除" 确认弹窗
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("清除历史数据", color = Ink) },
            text = {
                Text(
                    text = "将删除 ${clearDays} 天之前的所有记录，此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearRecordsOlderThan(clearDays)
                    showClearConfirmDialog = false
                    Toast.makeText(context, "已清除 ${clearDays} 天前的记录", Toast.LENGTH_SHORT).show()
                }) { Text("确认清除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) { Text("取消", color = Slate) }
            }
        )
    }
}
