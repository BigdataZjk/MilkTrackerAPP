package com.milktrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milktrackerapp.ui.components.AddRecordDialog
import com.milktrackerapp.ui.components.BottomTabBar
import com.milktrackerapp.ui.components.RecordList
import com.milktrackerapp.ui.components.RecordInputModule
import com.milktrackerapp.ui.components.StatisticsTab
import com.milktrackerapp.ui.components.TopInfoCard
import com.milktrackerapp.ui.theme.MilkTrackerAppTheme
import com.milktrackerapp.viewmodel.BottleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MilkTrackerAppTheme {
                MilkTrackerApp()
            }
        }
    }
}

@Composable
fun MilkTrackerApp() {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val viewModel: BottleViewModel = viewModel()

    val records by viewModel.records.collectAsStateWithLifecycle()
    val lastFeedTime by viewModel.lastFeedTime.collectAsStateWithLifecycle()
    val totalToday by viewModel.totalToday.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (selectedTab == 0) {
                    item {
                        TopInfoCard(
                            lastFeedTime = lastFeedTime,
                            lastAmount = records.firstOrNull()?.amount,
                            totalToday = totalToday
                        )
                    }
                    
                    item {
                        RecordInputModule(
                            onAddRecord = { amount, timestamp ->
                                viewModel.addRecord(amount, timestamp)
                            }
                        )
                    }
                }

                item {
                    when (selectedTab) {
                        0 -> {
                            RecordList(
                                records = records,
                                onDelete = { viewModel.deleteRecord(it) },
                                onUpdate = { id, amount -> viewModel.updateRecord(id, amount) }
                            )
                        }
                        1 -> {
                            StatisticsTab(
                                records = records
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    )

    if (showAddDialog) {
        AddRecordDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount ->
                viewModel.addRecord(amount)
                showAddDialog = false
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空数据") },
            text = { Text("确定要清空所有测试数据吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllRecords()
                        showClearDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}