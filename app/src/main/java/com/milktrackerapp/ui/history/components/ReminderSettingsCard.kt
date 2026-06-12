package com.milktrackerapp.ui.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milktrackerapp.data.model.ReminderSettings
import com.milktrackerapp.ui.theme.Tile
import com.milktrackerapp.ui.theme.Ink
import com.milktrackerapp.ui.theme.Slate
import com.milktrackerapp.ui.theme.Pebble
import com.milktrackerapp.ui.theme.Navy

@Composable
fun ReminderSettingsCard(
    settings: ReminderSettings,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var localMessage by remember(settings.message) { mutableStateOf(settings.message) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Tile),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 标题 + 开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "喂奶提醒",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Tile,
                        checkedTrackColor = Navy,
                        uncheckedThumbColor = Tile,
                        uncheckedTrackColor = Pebble.copy(alpha = 0.3f)
                    )
                )
            }

            if (settings.enabled) {
                // 间隔滑块
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "提醒间隔",
                            style = MaterialTheme.typography.labelMedium,
                            color = Slate
                        )
                        Text(
                            text = formatMinutes(settings.intervalMinutes),
                            style = MaterialTheme.typography.labelMedium,
                            color = Navy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Slider(
                        value = settings.intervalMinutes.toFloat(),
                        onValueChange = { onIntervalChange(it.toInt()) },
                        valueRange = 20f..480f,
                        steps = 91, // 5分钟一档
                        colors = SliderDefaults.colors(
                            thumbColor = Navy,
                            activeTrackColor = Navy,
                            inactiveTrackColor = Navy.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("20min", fontSize = 10.sp, color = Pebble)
                        Text("8h", fontSize = 10.sp, color = Pebble)
                    }
                }

                // 文案输入
                OutlinedTextField(
                    value = localMessage,
                    onValueChange = { localMessage = it },
                    label = { Text("提醒文案") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Navy,
                        focusedLabelColor = Navy
                    ),
                    // 在失焦时保存
                    supportingText = {
                        Text(
                            text = "自定义提醒时显示的文字内容",
                            style = MaterialTheme.typography.labelSmall,
                            color = Pebble
                        )
                    }
                )

                // 预设文案快捷选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("芃芃饿了3个半小时了", "芃芃该喝奶啦").forEach { preset ->
                        androidx.compose.material3.FilterChip(
                            selected = localMessage == preset,
                            onClick = {
                                localMessage = preset
                                onMessageChange(preset)
                            },
                            label = {
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 保存文案
                androidx.compose.material3.TextButton(
                    onClick = { onMessageChange(localMessage) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("保存文案", color = Navy, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0 && m > 0) "${h}小时${m}分钟"
    else if (h > 0) "${h}小时"
    else "${m}分钟"
}
