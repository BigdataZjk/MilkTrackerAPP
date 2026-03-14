package com.milktrackerapp.ui.components

import com.milktrackerapp.ui.theme.PrimaryDark
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Shadow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.SharedPreferences
import android.content.Context
import java.util.Calendar
import kotlin.math.min
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun RecordInputModule(
    onAddRecord: (Int, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("preset_amounts", Context.MODE_PRIVATE) }
    
    var selectedHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var selectedAmountIndex by remember { mutableStateOf<Int?>(null) }

    var presetAmounts by remember {
        mutableStateOf(loadPresetAmounts(prefs))
    }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf(-1) }
    var deletingIndex by remember { mutableStateOf(-1) }
    var editingAmount by remember { mutableStateOf("" ) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            hoveredElevation = 12.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "记录喂奶",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF5C4033),
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(
                    onClick = {
                        selectedHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        selectedMinute = Calendar.getInstance().get(Calendar.MINUTE)
                    },
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "当前时间",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5C4033)
                    )
                }
            }

            TimeInput(
                selectedHour = selectedHour,
                selectedMinute = selectedMinute,
                onHourChange = { selectedHour = it },
                onMinuteChange = { selectedMinute = it }
            )

            Text(
                text = "选择奶量",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF5C4033),
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                presetAmounts.forEachIndexed { index, amount ->
                    AmountPreset(
                        amount = amount,
                        isSelected = selectedAmountIndex == index,
                        onClick = { 
                            selectedAmountIndex = index 
                        },
                        onLongClick = {
                            deletingIndex = index
                            showDeleteDialog = true
                        }
                    )
                }
                // 添加+号按钮，用于添加新的预设奶量
                AmountPreset(
                    amount = -1, // 使用-1表示添加按钮
                    isSelected = false,
                    onClick = {
                        // 打开编辑对话框，添加新的预设奶量
                        editingIndex = presetAmounts.size
                        editingAmount = ""
                        showEditDialog = true
                    },
                    onLongClick = {}
                )
            }

            // 编辑预设奶量对话框
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("编辑预设奶量", style = MaterialTheme.typography.titleLarge) },
                    text = {
                        OutlinedTextField(
                            value = editingAmount,
                            onValueChange = { 
                                // 只允许输入正整数
                                if (it.isEmpty() || it.all { char -> char.isDigit() })
                                    editingAmount = it 
                            },
                            label = { Text("奶量 (ml)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val newAmount = editingAmount.toIntOrNull()
                                if (newAmount != null && newAmount > 0) {
                                    val newPresets = presetAmounts.toMutableList()
                                    if (editingIndex == newPresets.size) {
                                        // 添加新的预设奶量
                                        newPresets.add(newAmount)
                                    } else {
                                        // 修改现有预设奶量
                                        newPresets[editingIndex] = newAmount
                                    }
                                    presetAmounts = newPresets
                                    
                                    // 保存到SharedPreferences
                                    savePresetAmounts(prefs, newPresets)
                                }
                                showEditDialog = false
                            }
                        ) {
                            Text("保存", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("取消", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                )
            }

            // 删除预设奶量对话框
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("删除预设奶量", style = MaterialTheme.typography.titleLarge) },
                    text = {
                        Text("确定要删除这个预设奶量吗？", style = MaterialTheme.typography.bodyMedium)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (deletingIndex >= 0 && deletingIndex < presetAmounts.size) {
                                    val newPresets = presetAmounts.toMutableList()
                                    newPresets.removeAt(deletingIndex)
                                    presetAmounts = newPresets
                                    
                                    // 保存到SharedPreferences
                                    savePresetAmounts(prefs, newPresets)
                                    
                                    // 如果删除的是当前选中的奶量，清除选择
                                    if (selectedAmountIndex == deletingIndex) {
                                        selectedAmountIndex = null
                                    } else if (selectedAmountIndex != null && selectedAmountIndex!! > deletingIndex) {
                                        // 如果删除的是当前选中奶量之前的项，调整选中索引
                                        selectedAmountIndex = selectedAmountIndex!! - 1
                                    }
                                }
                                showDeleteDialog = false
                            }
                        ) {
                            Text("删除", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("取消", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                androidx.compose.material3.Button(
                    onClick = {
                        selectedAmountIndex?.let { index ->
                            val amount = presetAmounts[index]
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                            calendar.set(Calendar.MINUTE, selectedMinute)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            
                            val selectedTimestamp = calendar.timeInMillis
                            val currentTimestamp = System.currentTimeMillis()
                            
                            // 只允许添加当前时间或过去时间的记录
                            if (selectedTimestamp <= currentTimestamp) {
                                onAddRecord(amount, selectedTimestamp)
                                selectedAmountIndex = null
                            }
                        }
                    },
                    enabled = selectedAmountIndex != null,
                    modifier = Modifier.height(56.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp)
                ) {
                    Text(
                        "确认记录",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TimeInput(
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "选择时间",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF5C4033),
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimePickerWheel(
                value = selectedHour,
                onValueChange = { hour ->
                    onHourChange(hour)
                },
                label = "时",
                minValue = 0,
                maxValue = 23,
                isEnabled = { hour ->
                    // 禁用未来的小时 - 每次都获取最新时间
                    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    hour <= currentHour
                }
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            TimePickerWheel(
                value = selectedMinute,
                onValueChange = { minute ->
                    onMinuteChange(minute)
                },
                label = "分",
                minValue = 0,
                maxValue = 59,
                isEnabled = { minute ->
                    // 禁用未来的分钟 - 每次都获取最新时间
                    val calendar = java.util.Calendar.getInstance()
                    val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                    val currentMinute = calendar.get(java.util.Calendar.MINUTE)
                    selectedHour < currentHour || (selectedHour == currentHour && minute <= currentMinute)
                }
            )
        }
    }
}

private fun loadPresetAmounts(prefs: SharedPreferences): List<Int> {
    val amountsString = prefs.getString("preset_amounts", null)
    return if (amountsString != null) {
        amountsString.split(",").mapNotNull { it.toIntOrNull() }
    } else {
        listOf(30, 60, 90, 120, 150, 180)
    }
}

private fun savePresetAmounts(prefs: SharedPreferences, amounts: List<Int>) {
    prefs.edit().putString("preset_amounts", amounts.joinToString(",")).apply()
}

@Composable
fun TimePickerWheel(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    minValue: Int,
    maxValue: Int,
    isEnabled: (Int) -> Boolean = { true },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // 滑动选择器
        Box(
            modifier = Modifier
                .size(100.dp, 160.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            // 中间选中区域指示器
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.Center)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
            
            // 滚轮选择器
            val items = (minValue..maxValue).toList()
            val totalItems = items.size
            
            // 使用LazyColumn实现平滑滚动
            val listState = androidx.compose.foundation.lazy.rememberLazyListState(
                initialFirstVisibleItemIndex = value - minValue,
                initialFirstVisibleItemScrollOffset = -28
            )
            
            // 计算当前中间位置的项目索引
            val centerIndex = remember { mutableStateOf(value - minValue) }
            val isProgrammaticScroll = remember { mutableStateOf(false) }
            
            // 监听value的变化，自动滚动到对应位置
            androidx.compose.runtime.LaunchedEffect(value) {
                val targetIndex = value - minValue
                if (targetIndex >= 0 && targetIndex < totalItems) {
                    isProgrammaticScroll.value = true
                    centerIndex.value = targetIndex
                    listState.animateScrollToItem(
                        index = targetIndex,
                        scrollOffset = -28
                    )
                    isProgrammaticScroll.value = false
                }
            }
            
            // 监听滚动状态，更新中间位置的项目
            androidx.compose.runtime.LaunchedEffect(listState) {
                snapshotFlow { listState.layoutInfo }
                    .collect { layoutInfo ->
                        val visibleItems = layoutInfo.visibleItemsInfo
                        if (visibleItems.isNotEmpty()) {
                            // 找到中间位置的项目
                            val containerCenter = layoutInfo.viewportEndOffset / 2
                            var closestItem = visibleItems[0]
                            var minDistance = Math.abs(closestItem.offset + closestItem.size / 2 - containerCenter)
                            
                            for (item in visibleItems) {
                                val itemCenter = item.offset + item.size / 2
                                val distance = Math.abs(itemCenter - containerCenter)
                                if (distance < minDistance) {
                                    minDistance = distance
                                    closestItem = item
                                }
                            }
                            
                            if (closestItem.index != centerIndex.value) {
                                centerIndex.value = closestItem.index
                            }
                        }
                    }
            }
            
            // 滚动结束后自动对齐到最近的数字（齿轮拨动效果）
            androidx.compose.runtime.LaunchedEffect(listState.isScrollInProgress) {
                if (!listState.isScrollInProgress) {
                    val layoutInfo = listState.layoutInfo
                    val visibleItems = layoutInfo.visibleItemsInfo
                    
                    if (visibleItems.isNotEmpty()) {
                        val containerCenter = layoutInfo.viewportEndOffset / 2
                        var closestItem = visibleItems[0]
                        var minDistance = Math.abs(closestItem.offset + closestItem.size / 2 - containerCenter)
                        
                        for (item in visibleItems) {
                            val itemCenter = item.offset + item.size / 2
                            val distance = Math.abs(itemCenter - containerCenter)
                            if (distance < minDistance) {
                                minDistance = distance
                                closestItem = item
                            }
                        }
                        
                        // 始终滚动到最近的数字，无论是否正在滚动
                        isProgrammaticScroll.value = true
                        centerIndex.value = closestItem.index
                        listState.animateScrollToItem(
                            index = closestItem.index,
                            scrollOffset = -28
                        )
                        // 同步更新value
                        val newValue = items[closestItem.index]
                        if (newValue != value) {
                            onValueChange(newValue)
                        }
                    }
                }
            }
            
            // 滚轮布局
            androidx.compose.foundation.lazy.LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 40.dp,
                    bottom = 40.dp
                )
            ) {
                items(totalItems) { index ->
                    val itemValue = items[index]
                    val isCenter = index == centerIndex.value
                    
                    val enabled = isEnabled(itemValue)
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .width(80.dp)
                            .clickable(enabled = enabled) {
                                if (enabled) {
                                    onValueChange(itemValue)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = itemValue.toString().padStart(2, '0'),
                            fontSize = if (isCenter) 36.sp else 20.sp,
                            fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                            color = if (!enabled) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // 禁用状态为灰色
                            } else if (isCenter) {
                                PrimaryDark // 使用更深的粉色
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmountPreset(
    amount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = 90.dp, height = 90.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else if (amount == -1) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.White
            }
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp,
            hoveredElevation = 8.dp
        ),
        border = if (!isSelected && amount != -1) {
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (amount == -1) {
                // 显示+号按钮
                Text(
                    text = "+",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "${amount}",
                    style = MaterialTheme.typography.displayMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        Color(0xFF5C4033)
                    },
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ml",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f)
                    } else {
                        Color(0xFF8B7355)
                    }
                )
            }
        }
    }
}