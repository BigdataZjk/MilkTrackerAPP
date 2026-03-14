package com.milktrackerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTime by remember { mutableStateOf(getCurrentTime()) }
    var selectedAmount by remember { mutableStateOf<Int?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editAmount by remember { mutableStateOf("") }

    val presetAmounts = listOf(30, 60, 90, 120, 150, 180)

    if (showEditDialog) {
        EditPresetDialog(
            currentAmount = editAmount,
            onDismiss = { showEditDialog = false },
            onConfirm = { newAmount ->
                editAmount = newAmount
                showEditDialog = false
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "选择时间",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TimeSelector(
                    selectedTime = selectedTime,
                    onTimeChange = { selectedTime = it }
                )
                Text(
                    text = "选择奶量",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(presetAmounts.size) { index ->
                        val amount = presetAmounts[index]
                        AmountPreset(
                            amount = amount,
                            isSelected = selectedAmount == amount,
                            onClick = { selectedAmount = amount },
                            onLongClick = {
                                editAmount = amount.toString()
                                showEditDialog = true
                            }
                        )
                    }
                }
                TextButton(
                    onClick = {
                        selectedAmount?.let {
                            onConfirm(it)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAmount != null
                ) {
                    Text("确认")
                }
            }
        }
    }
}

@Composable
fun TimeSelector(
    selectedTime: String,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (hours, minutes) = selectedTime.split(":").map { it.toInt() }
    val hourOptions = (0..23).map { it.toString().padStart(2, '0') }
    val minuteOptions = (0..59 step 5).map { it.toString().padStart(2, '0') }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimePickerSection(
                title = "时",
                options = hourOptions,
                selectedValue = hours.toString().padStart(2, '0'),
                onSelect = { newHour ->
                    onTimeChange("$newHour:${minutes.toString().padStart(2, '0')}")
                },
                modifier = Modifier
            )
            Text(
                text = ":",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            TimePickerSection(
                title = "分",
                options = minuteOptions,
                selectedValue = minutes.toString().padStart(2, '0'),
                onSelect = { newMinute ->
                    onTimeChange("${hours.toString().padStart(2, '0')}:$newMinute")
                },
                modifier = Modifier
            )
        }
    }
}

@Composable
fun TimePickerSection(
    title: String,
    options: List<String>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(options.size) { index ->
                    val value = options[index]
                    TimeOption(
                        value = value,
                        isSelected = value == selectedValue,
                        onClick = { onSelect(value) }
                    )
                }
            }
        }
    }
}

@Composable
fun TimeOption(
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPresetDialog(
    currentAmount: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var amount by remember { mutableStateOf(currentAmount) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "编辑预设奶量",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("奶量 (ml)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                TextButton(
                    onClick = { onConfirm(amount) },
                    enabled = amount.isNotEmpty() && amount.toIntOrNull() != null
                ) {
                    Text("保存")
                }
            }
        }
    }
}

private fun getCurrentTime(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}