package com.milktrackerapp.ui.record.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.milktrackerapp.data.model.BottleRecord
import com.milktrackerapp.ui.theme.Warning
import com.milktrackerapp.ui.theme.Danger
import com.milktrackerapp.ui.theme.Caution
import com.milktrackerapp.ui.theme.Safe
import com.milktrackerapp.ui.theme.Rule
import com.milktrackerapp.ui.theme.Tile
import com.milktrackerapp.ui.theme.Ink
import com.milktrackerapp.ui.theme.Slate
import com.milktrackerapp.ui.theme.Pebble
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayRecordList(
    records: List<BottleRecord>,
    onDelete: (String) -> Unit,
    onUpdate: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val todayRecords = records
        .filter { it.timestamp >= todayStart }
        .sortedByDescending { it.timestamp }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "今日记录",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            fontWeight = FontWeight.SemiBold
        )

        if (todayRecords.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Tile),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "今天还没有记录",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            todayRecords.forEachIndexed { index, record ->
                val prevRecord = if (index < todayRecords.size - 1) {
                    todayRecords[index + 1]
                } else null

                RecordRow(
                    record = record,
                    prevRecord = prevRecord,
                    onDelete = { onDelete(record.id) },
                    onUpdate = { onUpdate(record.id, it) }
                )
            }
        }
    }
}

@Composable
private fun RecordRow(
    record: BottleRecord,
    prevRecord: BottleRecord?,
    onDelete: () -> Unit,
    onUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showActions by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editAmount by remember { mutableStateOf(record.amount.toString()) }

    val intervalColor = if (prevRecord != null) {
        val hours = (prevRecord.timestamp - record.timestamp) / 3_600_000f
        when {
            hours >= 4 -> Danger
            hours >= 3 -> Warning
            hours >= 2 -> Caution
            else -> Safe
        }
    } else Pebble

    val intervalText = if (prevRecord != null) {
        val diff = prevRecord.timestamp - record.timestamp
        val h = diff / 3_600_000
        val m = (diff % 3_600_000) / 60_000
        "${h}h${m}m"
    } else "--"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showActions = true },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Tile),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp)),
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(60.dp)
            )
            // 奶量
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${record.amount}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = " ml",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate
                )
            }
            // 间隔
            Text(
                text = intervalText,
                style = MaterialTheme.typography.bodyMedium,
                color = intervalColor,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // 操作菜单
    if (showActions) {
        AlertDialog(
            onDismissRequest = { showActions = false },
            title = {
                Text(
                    text = "${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp))} — ${record.amount}ml",
                    color = Ink
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showActions = false
                    showEditDialog = true
                }) { Text("修改奶量", color = Navy) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showActions = false
                    onDelete()
                }) { Text("删除记录", color = MaterialTheme.colorScheme.error) }
            }
        )
    }

    // 编辑对话框
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("修改奶量", color = Ink) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = editAmount,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) editAmount = it },
                    label = { Text("奶量 (ml)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Navy,
                        focusedLabelColor = Navy
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = editAmount.toIntOrNull()
                    if (amount != null && amount > 0) {
                        onUpdate(amount)
                        showEditDialog = false
                    }
                }) { Text("保存", color = Navy) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("取消", color = Slate) }
            }
        )
    }
}

// 引入 Navy 用于此文件
private val Navy = com.milktrackerapp.ui.theme.Navy
