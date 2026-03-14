package com.milktrackerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.milktrackerapp.data.BottleRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RecordList(
    records: List<BottleRecord>,
    onDelete: (String) -> Unit,
    onUpdate: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedRecords = records.sortedByDescending { it.timestamp }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "今日记录",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF5C4033),
            fontWeight = FontWeight.SemiBold
        )
        if (sortedRecords.isEmpty()) {
            EmptyState()
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                sortedRecords.forEachIndexed { index, record ->
                    val prevRecord = if (index < sortedRecords.size - 1) {
                        sortedRecords[index + 1]
                    } else {
                        null
                    }
                    RecordItem(
                        record = record,
                        prevRecord = prevRecord,
                        onDelete = { onDelete(record.id) },
                        onUpdate = { id, amount -> onUpdate(id, amount) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordItem(
    record: BottleRecord,
    prevRecord: BottleRecord?,
    onDelete: () -> Unit,
    onUpdate: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editAmount by remember { mutableStateOf(record.amount.toString()) }

    if (showEditDialog) {
        EditAmountDialog(
            currentAmount = editAmount,
            onDismiss = { showEditDialog = false },
            onConfirm = { newAmount: String ->
                onUpdate(record.id, newAmount.toIntOrNull() ?: record.amount)
                showEditDialog = false
            }
        )
    } else {
        Card(
            modifier = modifier
                    .fillMaxWidth()
                    .clickable {
                        showMenu = true
                    },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                hoveredElevation = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimeForRecordList(record.timestamp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF5C4033),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(90.dp)
                )
                Box(
                    modifier = Modifier.width(110.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${record.amount}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF5C4033),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "ml",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5C4033),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (prevRecord != null) {
                    val intervalResult = formatInterval(prevRecord.timestamp, record.timestamp)
                    val intervalText = intervalResult.first
                    val intervalColor = intervalResult.second
                    Text(
                        text = intervalText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = intervalColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                } else {
                    // 首条记录，添加占位文本以保持对齐
                    Text(
                        text = "00:00:00",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.0f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }

    if (showMenu) {
        ActionMenu(
            onDismiss = { showMenu = false },
            onEdit = {
                showMenu = false
                showEditDialog = true
            },
            onDelete = {
                showMenu = false
                onDelete()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionMenu(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuButton(onClick = onEdit, text = "修改奶量")
                MenuButton(onClick = onDelete, text = "删除记录", color = MaterialTheme.colorScheme.error)
                Divider(
                    color = Color(0xFFFFE4E1),
                    thickness = 1.dp
                )
                MenuButton(onClick = onDismiss, text = "取消", color = Color(0xFFF5F3FF))
            }
        }
    }
}

@Composable
fun MenuButton(
    onClick: () -> Unit,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
            containerColor = color,
            contentColor = if (color == MaterialTheme.colorScheme.primary || color == MaterialTheme.colorScheme.error) {
                Color.White
            } else {
                Color(0xFF5C4033)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = if (color == MaterialTheme.colorScheme.primary || color == MaterialTheme.colorScheme.error) {
                Color.White
            } else {
                Color(0xFF5C4033)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAmountDialog(
    currentAmount: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var amount by remember { mutableStateOf(currentAmount) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "修改奶量",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF5C4033),
                    fontWeight = FontWeight.SemiBold
                )
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("奶量 (ml)", color = Color(0xFF8B7355)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFF0F5),
                        unfocusedContainerColor = Color(0xFFFFFEFC),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color(0xFF8B7355),
                        focusedTextColor = Color(0xFF5C4033),
                        unfocusedTextColor = Color(0xFF5C4033),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color(0xFFFFE4E1)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(56.dp),
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            containerColor = Color(0xFFFFFAFF),
                            contentColor = Color(0xFF5C4033)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "取消",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5C4033)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        onClick = { onConfirm(amount) },
                        enabled = amount.isNotEmpty() && amount.toIntOrNull() != null,
                        modifier = Modifier.height(56.dp),
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color(0xFFFFFAFF),
                            contentColor = Color.White,
                            disabledContentColor = Color(0xFF8B7355)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "保存",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (amount.isNotEmpty() && amount.toIntOrNull() != null) Color.White else Color(0xFF8B7355)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无记录",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

fun formatTimeForRecordList(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatInterval(prevTime: Long, currentTime: Long): Pair<String, Color> {
    val diff = currentTime - prevTime
    val hours = diff / (1000 * 60 * 60)
    val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
    val totalHours = hours + minutes.toFloat() / 60
    
    val color = when {
        totalHours >= 4 -> Color(0xFFDC2626) // 深红色，高对比度
        totalHours >= 3.5 -> Color(0xFFF97316) // 橙红色，高对比度
        totalHours >= 3 -> Color(0xFFEAB308) // 深黄色，高对比度
        totalHours >= 2.5 -> Color(0xFF16A34A) // 深绿色，高对比度
        else -> Color(0xFF1F2937) // 深灰色，默认颜色
    }
    
    return Pair("${hours}h${minutes}m", color)
}

fun isToday(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis
    return timestamp >= todayStart
}