package com.milktrackerapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milktrackerapp.ui.history.HistoryScreen
import com.milktrackerapp.ui.record.RecordScreen
import com.milktrackerapp.ui.theme.Navy
import com.milktrackerapp.ui.theme.Slate
import com.milktrackerapp.ui.theme.Tile
import com.milktrackerapp.ui.theme.Rule

enum class Tab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    RECORD("记录", Icons.Filled.EditNote, Icons.Outlined.EditNote),
    HISTORY("历史", Icons.Filled.History, Icons.Outlined.History)
}

@Composable
fun MilkTrackerNavigation(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = Tile,
                tonalElevation = 0.dp
            ) {
                Tab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy,
                            selectedTextColor = Navy,
                            unselectedIconColor = Slate,
                            unselectedTextColor = Slate,
                            indicatorColor = Navy.copy(alpha = 0.08f)
                        )
                    )
                }
            }
        },
        content = { padding ->
            when (currentTab) {
                Tab.RECORD -> RecordScreen(modifier = Modifier.padding(padding))
                Tab.HISTORY -> HistoryScreen(modifier = Modifier.padding(padding))
            }
        }
    )
}
