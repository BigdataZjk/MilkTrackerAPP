package com.milktrackerapp.ui.record.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milktrackerapp.data.model.BottleRecord
import com.milktrackerapp.ui.theme.Warning
import com.milktrackerapp.ui.theme.Danger
import com.milktrackerapp.ui.theme.Caution
import com.milktrackerapp.ui.theme.Safe
import com.milktrackerapp.ui.theme.Pebble
import com.milktrackerapp.ui.theme.Ink
import com.milktrackerapp.ui.theme.Slate
import com.milktrackerapp.ui.theme.Navy
import kotlinx.coroutines.delay

@Composable
fun TopInfoCard(
    lastRecord: BottleRecord?,
    totalToday: Int,
    countToday: Int,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(lastRecord) {
        while (true) {
            elapsedSeconds = if (lastRecord != null) {
                (System.currentTimeMillis() - lastRecord.timestamp) / 1000
            } else 0
            delay(1000)
        }
    }

    val (elapsedText, elapsedColor) = formatElapsed(elapsedSeconds)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 首行：上次喂奶 + 间隔时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "上次喂奶",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate
                    )
                    Text(
                        text = if (lastRecord != null) {
                            "${lastRecord.amount}ml"
                        } else {
                            "暂无记录"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "距上次",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate
                    )
                    Text(
                        text = elapsedText,
                        style = MaterialTheme.typography.titleLarge,
                        color = elapsedColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 次行：今日统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "今日已喝", value = "${totalToday}ml", accent = true)
                StatItem(label = "今日次数", value = "${countToday}次", accent = false)
                StatItem(
                    label = "单次均值",
                    value = if (countToday > 0) "${totalToday / countToday}ml" else "--",
                    accent = false
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, accent: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (accent) Navy else Ink,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatElapsed(totalSeconds: Long): Pair<String, androidx.compose.ui.graphics.Color> {
    if (totalSeconds <= 0) return "--" to Slate
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val color = when {
        hours >= 4 -> Danger
        hours >= 3 -> Warning
        hours >= 2 -> Caution
        else -> Safe
    }

    val text = if (hours > 0) {
        "${hours}时${minutes}分"
    } else if (minutes > 0) {
        "${minutes}分${seconds}秒"
    } else {
        "${seconds}秒"
    }

    return text to color
}
