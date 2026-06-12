package com.milktrackerapp.ui.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.milktrackerapp.data.model.BottleRecord
import com.milktrackerapp.ui.theme.Tile
import com.milktrackerapp.ui.theme.Ink
import com.milktrackerapp.ui.theme.Slate
import com.milktrackerapp.ui.theme.Pebble
import com.milktrackerapp.ui.theme.Navy
import com.milktrackerapp.ui.theme.NavyMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryRecordList(
    records: List<BottleRecord>,
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var deletingId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "历史记录",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            fontWeight = FontWeight.SemiBold
        )

        if (records.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Tile),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "该时段没有记录",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // 按日期分组显示
            val groupedRecords = records.groupBy {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
            }
            groupedRecords.forEach { (dateStr, dayRecords) ->
                val total = dayRecords.sumOf { it.amount }
                val formattedDate = SimpleDateFormat("MM月dd日", Locale.getDefault()).format(
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)!!
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Tile),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.titleSmall,
                                color = Ink,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "合计 ${total}ml · ${dayRecords.size}次",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate
                            )
                        }
                        dayRecords.sortedByDescending { it.timestamp }.forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        .format(Date(record.timestamp)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${record.amount}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Ink,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = " ml",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate
                                    )
                                }
                                TextButton(
                                    onClick = { deletingId = record.id },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 8.dp, vertical = 2.dp
                                    )
                                ) {
                                    Text(
                                        text = "删除",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 分页控制器
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onPageChange(currentPage - 1) },
                    enabled = currentPage > 0
                ) {
                    Text("上一页", color = if (currentPage > 0) Navy else Pebble)
                }

                Text(
                    text = "${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                TextButton(
                    onClick = { onPageChange(currentPage + 1) },
                    enabled = currentPage < totalPages - 1
                ) {
                    Text("下一页", color = if (currentPage < totalPages - 1) Navy else Pebble)
                }
            }
        }
    }

    // 删除确认
    if (deletingId != null) {
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = { Text("删除记录", color = Ink) },
            text = { Text("确定删除这条记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    deletingId?.let { onDelete(it) }
                    deletingId = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }) { Text("取消", color = Slate) }
            }
        )
    }
}
