package com.milktrackerapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milktrackerapp.data.BottleRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun StatisticsTab(
    records: List<BottleRecord>,
    modifier: Modifier = Modifier
) {
    val groupedRecords = records.groupByDate()
    val sortedDates = groupedRecords.keys.sortedDescending()

    Column(
        modifier = modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "每日奶量记录",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF5C4033)
        )
        TrendChart(
            groupedRecords = groupedRecords,
            sortedDates = sortedDates
        )
    }
}

@Composable
fun TrendChart(
    groupedRecords: Map<String, List<BottleRecord>>,
    sortedDates: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        sortedDates.take(7).forEach { date ->
            val dayRecords = groupedRecords[date] ?: emptyList()
            val total = dayRecords.sumOf { it.amount }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                    hoveredElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(date),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF5C4033)
                        )
                        Text(
                            text = "总计: ${total}ml",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF5C4033)
                        )
                    }
                    
                    if (dayRecords.isNotEmpty()) {
                        LineChart(
                            records = dayRecords
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            dayRecords.forEach { record ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatTimeForStatistics(record.timestamp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF5C4033)
                                    )
                                    Text(
                                        text = "${record.amount}ml",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF5C4033)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart(
    records: List<BottleRecord>,
    modifier: Modifier = Modifier
) {
    val sortedRecords = records.sortedBy { it.timestamp }
    val maxAmount = sortedRecords.maxByOrNull { it.amount }?.amount ?: 100
    val chartHeight = 140.dp
    
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Box(
        modifier = modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .background(
                        color = primaryContainerColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
    ) {
        if (sortedRecords.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                // 绘制网格线
                for (i in 0..4) {
                    val y = (height) * i / 4
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }
                
                // 绘制折线和数据点
                for (index in sortedRecords.indices) {
                    val record = sortedRecords[index]
                    val x = (index.toFloat() / max(1, sortedRecords.size - 1)) * width
                    val y = height - (record.amount.toFloat() / maxAmount.toFloat()) * height
                    
                    if (index > 0) {
                        val prevRecord = sortedRecords[index - 1]
                        val prevX = ((index - 1).toFloat() / max(1, sortedRecords.size - 1)) * width
                        val prevY = height - (prevRecord.amount.toFloat() / maxAmount.toFloat()) * height
                        
                        drawLine(
                            color = primaryColor,
                            start = Offset(prevX, prevY),
                            end = Offset(x, y),
                            strokeWidth = 3f
                        )
                    }
                    
                    // 绘制数据点
                    drawCircle(
                        color = primaryColor,
                        center = Offset(x, y),
                        radius = 6f
                    )
                    
                    // 绘制数据点外圈
                    drawCircle(
                        color = surfaceColor,
                        center = Offset(x, y),
                        radius = 8f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun RecordTable(
    groupedRecords: Map<String, List<BottleRecord>>,
    sortedDates: List<String>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortedDates) { date ->
            val dayRecords = groupedRecords[date] ?: emptyList()
            val total = dayRecords.sumOf { it.amount }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
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
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(date),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5C4033)
                        )
                        Text(
                            text = "总计: ${total}ml",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5C4033)
                        )
                    }
                    dayRecords.forEach { record ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTimeForStatistics(record.timestamp),
                                fontSize = 14.sp,
                                color = Color(0xFF5C4033)
                            )
                            Text(
                                text = "${record.amount.toInt()}ml",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5C4033)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(dateStr: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = sdf.parse(dateStr) ?: return dateStr
    val calendar = Calendar.getInstance()
    calendar.time = date
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return "${month}月${day}日"
}

private fun formatTimeForStatistics(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun List<BottleRecord>.groupByDate(): Map<String, List<BottleRecord>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return this.groupBy { sdf.format(Date(it.timestamp)) }
}

private fun List<String>.sortedDescending(): List<String> {
    return this.sortedByDescending { it }
}