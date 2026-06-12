package com.milktrackerapp.ui.record.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milktrackerapp.ui.theme.Rule
import com.milktrackerapp.ui.theme.Tile
import com.milktrackerapp.ui.theme.Ink
import com.milktrackerapp.ui.theme.Slate
import com.milktrackerapp.ui.theme.Pebble
import com.milktrackerapp.ui.theme.Navy
import com.milktrackerapp.ui.theme.NavyMuted
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun QuickRecordPanel(
    presetAmounts: List<Int>,
    showTimePicker: Boolean,
    selectedHour: Int,
    selectedMinute: Int,
    onToggleTimePicker: () -> Unit,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onPresetAdd: (Int) -> Unit,
    onPresetUpdate: (Int, Int) -> Unit,
    onPresetDelete: (Int) -> Unit,
    onRecord: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAmountIndex by remember { mutableStateOf<Int?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf(-1) }
    var editingAmount by remember { mutableStateOf("") }
    var deletingIndex by remember { mutableStateOf(-1) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Tile),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "记录喂奶",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold
                )
                // 动态时间显示 — 格式: "xx时xx分"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyMuted, RoundedCornerShape(12.dp))
                        .clickable { onToggleTimePicker() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${selectedHour}时${selectedMinute.toString().padStart(2, '0')}分",
                        style = MaterialTheme.typography.labelLarge,
                        color = Navy,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 时间选择器（带动画展开/收起）
            AnimatedVisibility(
                visible = showTimePicker,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                WheelTimePicker(
                    hour = selectedHour,
                    minute = selectedMinute,
                    onHourChange = onHourChange,
                    onMinuteChange = onMinuteChange
                )
            }

            // 预设奶量按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                presetAmounts.forEachIndexed { index, amount ->
                    PresetChip(
                        amount = amount,
                        isSelected = selectedAmountIndex == index,
                        onClick = { selectedAmountIndex = index },
                        onLongClick = {
                            deletingIndex = index
                            showDeleteDialog = true
                        }
                    )
                }
                PresetChip(
                    amount = -1,
                    isSelected = false,
                    onClick = {
                        editingIndex = presetAmounts.size
                        editingAmount = ""
                        showEditDialog = true
                    },
                    onLongClick = {}
                )
            }

            // 确认按钮
            Button(
                onClick = {
                    selectedAmountIndex?.let { idx ->
                        if (idx < presetAmounts.size) {
                            onRecord(presetAmounts[idx])
                            selectedAmountIndex = null
                        }
                    }
                },
                enabled = selectedAmountIndex != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Navy,
                    disabledContainerColor = NavyMuted,
                    disabledContentColor = Pebble
                ),
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) {
                Text(
                    text = if (selectedAmountIndex != null) {
                        "${presetAmounts[selectedAmountIndex!!]}ml — 确认记录"
                    } else {
                        "选择奶量后点击记录"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // 编辑预设奶量对话框
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("添加预设奶量", color = Ink) },
            text = {
                OutlinedTextField(
                    value = editingAmount,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) editingAmount = it },
                    label = { Text("奶量 (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Navy,
                        focusedLabelColor = Navy
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = editingAmount.toIntOrNull()
                    if (amount != null && amount > 0) {
                        if (editingIndex == presetAmounts.size) {
                            onPresetAdd(amount)
                        } else {
                            onPresetUpdate(editingIndex, amount)
                        }
                        showEditDialog = false
                    }
                }) { Text("保存", color = Navy) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("取消", color = Slate) }
            }
        )
    }

    // 删除预设奶量对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除预设奶量", color = Ink) },
            text = { Text("确定删除 ${presetAmounts.getOrNull(deletingIndex)}ml 这个预设吗？") },
            confirmButton = {
                TextButton(onClick = {
                    if (deletingIndex in presetAmounts.indices) {
                        if (selectedAmountIndex == deletingIndex) {
                            selectedAmountIndex = null
                        } else if (selectedAmountIndex != null && selectedAmountIndex!! > deletingIndex) {
                            selectedAmountIndex = selectedAmountIndex!! - 1
                        }
                        onPresetDelete(deletingIndex)
                    }
                    showDeleteDialog = false
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消", color = Slate) }
            }
        )
    }
}

@Composable
private fun PresetChip(
    amount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAddButton = amount == -1

    Box(
        modifier = modifier
            .size(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (isSelected) {
                    Modifier.background(Navy, RoundedCornerShape(18.dp))
                } else if (isAddButton) {
                    Modifier.border(1.5.dp, Navy.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .background(NavyMuted, RoundedCornerShape(18.dp))
                } else {
                    Modifier.border(1.dp, Rule, RoundedCornerShape(18.dp))
                        .background(Tile, RoundedCornerShape(18.dp))
                }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { if (!isAddButton) onLongClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isAddButton) {
            Text(text = "+", fontSize = 28.sp, color = Navy, fontWeight = FontWeight.Light)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$amount",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Tile else Ink
                )
                Text(
                    text = "ml",
                    fontSize = 11.sp,
                    color = if (isSelected) Tile.copy(alpha = 0.85f) else Pebble
                )
            }
        }
    }
}

// ── 滚轮时间选择器 ──

@Composable
private fun WheelTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        SnapWheel(
            value = hour,
            onValueChange = onHourChange,
            min = 0,
            max = 23,
            label = "时",
            isEnabled = { h ->
                val now = java.util.Calendar.getInstance()
                h <= now.get(java.util.Calendar.HOUR_OF_DAY)
            }
        )
        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            modifier = Modifier.padding(horizontal = 12.dp).padding(top = 48.dp)
        )
        SnapWheel(
            value = minute,
            onValueChange = onMinuteChange,
            min = 0,
            max = 59,
            label = "分",
            isEnabled = { m ->
                val now = java.util.Calendar.getInstance()
                val curH = now.get(java.util.Calendar.HOUR_OF_DAY)
                val curM = now.get(java.util.Calendar.MINUTE)
                hour < curH || (hour == curH && m <= curM)
            }
        )
    }
}

@Composable
private fun SnapWheel(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    label: String,
    isEnabled: (Int) -> Boolean
) {
    val items = (min..max).toList()
    val initialIndex = items.indexOf(value).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val density = LocalDensity.current

    // 每个 item 的高度（含间距）
    val itemHeightDp = 44.dp
    val itemHeightPx = with(density) { itemHeightDp.toPx() }

    // 容器半高，用于计算居中偏移
    val containerHeightDp = 180.dp
    val containerHeightPx = with(density) { containerHeightDp.toPx() }
    val halfContainerPx = containerHeightPx / 2

    // 自动选中可视区域中间偏上的项目
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) return@snapshotFlow null
            // 找到最接近容器中心的 item
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2
            layoutInfo.visibleItemsInfo.minByOrNull {
                val itemCenter = it.offset + it.size / 2
                abs(itemCenter - viewportCenter).toFloat()
            }?.index
        }.collect { centerIndex ->
            if (centerIndex != null && centerIndex in items.indices) {
                val newValue = items[centerIndex]
                if (newValue != value) {
                    onValueChange(newValue)
                }
            }
        }
    }

    // 当外部 value 变化时（比如另一滚轮导致该滚轮需要调整），同步滚动
    LaunchedEffect(value) {
        val targetIndex = items.indexOf(value)
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate)

        Box(
            modifier = Modifier
                .width(72.dp)
                .height(containerHeightDp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // 中间高亮指示条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeightDp)
                    .align(Alignment.Center)
                    .background(Navy.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(
                    top = containerHeightDp / 2 - itemHeightDp / 2,
                    bottom = containerHeightDp / 2 - itemHeightDp / 2
                )
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    val enabled = isEnabled(item)
                    val isCenter = item == value
                    Box(
                        modifier = Modifier
                            .height(itemHeightDp)
                            .width(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.toString().padStart(2, '0'),
                            fontSize = if (isCenter) 24.sp else 16.sp,
                            fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                !enabled -> Pebble
                                isCenter -> Navy
                                else -> Ink
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
