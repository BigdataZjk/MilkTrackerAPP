package com.milktrackerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopInfoCard(
    lastFeedTime: Long?,
    lastAmount: Int?,
    totalToday: Int,
    modifier: Modifier = Modifier
) {
    // 动态时间差状态
    val timeDifference = remember { mutableStateOf(0L) }
    
    // 每秒更新时间差
    LaunchedEffect(lastFeedTime) {
        if (lastFeedTime != null) {
            while (true) {
                timeDifference.value = System.currentTimeMillis() - lastFeedTime
                delay(1000) // 每秒更新一次
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            hoveredElevation = 12.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "上次喝奶时间",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B7355)
                    )
                    Text(
                        text = lastFeedTime?.let { formatTime(it) } ?: "暂无记录",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color(0xFF5C4033),
                        fontWeight = FontWeight.Bold
                    )
                    lastAmount?.let { amount ->
                        Text(
                            text = "${amount}ml",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF5C4033),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    lastFeedTime?.let { _ ->
                        val timeResult = formatTimeDifference(timeDifference.value)
                        val timeText = timeResult.first
                        val timeColor = timeResult.second
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.titleMedium,
                            color = timeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(start = 32.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "今日已喝",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B7355)
                    )
                    Text(
                        text = "${totalToday}ml",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatBreakDuration(lastFeedTime: Long): String {
    val duration = System.currentTimeMillis() - lastFeedTime
    val hours = duration / (1000 * 60 * 60)
    val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)
    return "断奶: ${hours}h${minutes}m"
}

private fun formatTimeDifference(duration: Long): Pair<String, Color> {
    val absDuration = Math.abs(duration)
    val hours = absDuration / (1000 * 60 * 60)
    val minutes = (absDuration % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (absDuration % (1000 * 60)) / 1000
    val totalHours = hours + minutes.toFloat() / 60
    
    val color = when {
        totalHours >= 4 -> Color(0xFFDC2626) // 深红色，高对比度
        totalHours >= 3.5 -> Color(0xFFF97316) // 橙红色，高对比度
        totalHours >= 3 -> Color(0xFFEAB308) // 深黄色，高对比度
        totalHours >= 2.5 -> Color(0xFF16A34A) // 深绿色，高对比度
        else -> Color(0xFF1F2937) // 深灰色，默认颜色
    }
    
    return Pair("${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}", color)
}