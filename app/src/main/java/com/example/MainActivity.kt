package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tools.registry.ToolCategory
import com.example.tools.registry.ToolRegistry
import com.example.tools.screens.*
import com.example.ui.screens.*
import com.example.ui.theme.NovaTheme
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceSurfaceElevated
import com.example.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovaTheme {
                val context = LocalContext.current
                val appViewModel: AppViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return AppViewModel(context.applicationContext as android.app.Application) as T
                        }
                    }
                )

                NovaAiHubApp(viewModel = appViewModel)
            }
        }
    }
}

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val tabIndex: Int
)

@Composable
fun NovaAiHubApp(viewModel: AppViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeToolId by viewModel.activeToolId.collectAsState()

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home, 0),
        NavItem("Tools", Icons.Default.Widgets, 1),
        NavItem("Favorites", Icons.Default.Favorite, 2),
        NavItem("History", Icons.Default.History, 3),
        NavItem("Settings", Icons.Default.Settings, 4)
    )

    // Handle back button behavior
    BackHandler(enabled = activeToolId != null || selectedTab != 0) {
        if (activeToolId != null) {
            viewModel.closeTool()
        } else if (selectedTab != 0) {
            viewModel.selectTab(0)
        }
    }

    if (activeToolId != null) {
        // Render Active Tool Fullscreen
        val toolDef = ToolRegistry.findToolById(activeToolId!!)
        when {
            activeToolId == "ai_chat" -> {
                AiChatScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
            toolDef?.category == ToolCategory.WRITING -> {
                WritingToolsScreen(
                    toolId = activeToolId!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
            toolDef?.category == ToolCategory.STUDY -> {
                StudyToolsScreen(
                    toolId = activeToolId!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
            toolDef?.category == ToolCategory.CODING -> {
                CodingToolsScreen(
                    toolId = activeToolId!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
            toolDef?.category == ToolCategory.IMAGE -> {
                ImageToolsScreen(
                    toolId = activeToolId!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
            toolDef?.category == ToolCategory.VIDEO -> {
                VideoToolsScreen(
                    toolId = activeToolId!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
            toolDef?.category == ToolCategory.AUDIO -> {
                AudioToolsScreen(
                    toolId = activeToolId!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
            toolDef?.category == ToolCategory.PRODUCTIVITY -> {
                ProductivityToolsScreen(
                    toolId = activeToolId!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
            toolDef?.category == ToolCategory.UTILITIES -> {
                UtilitiesScreen(
                    toolId = activeToolId!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
            else -> {
                // Fallback for any generic tool
                WritingToolsScreen(
                    toolId = activeToolId!!,
                    viewModel = viewModel,
                    onBack = { viewModel.closeTool() }
                )
            }
        }
    } else {
        // Main Navigation Scaffold with Bottom Navigation
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = SpaceBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = SpaceSurfaceElevated,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = selectedTab == item.tabIndex,
                            onClick = { viewModel.selectTab(item.tabIndex) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (selectedTab == item.tabIndex) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = NovaViolet,
                                indicatorColor = NovaViolet.copy(alpha = 0.35f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${item.title.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTools = { viewModel.selectTab(1) },
                        onOpenTool = { toolId -> viewModel.openTool(toolId) }
                    )
                    1 -> ToolsHubScreen(
                        viewModel = viewModel,
                        onOpenTool = { toolId -> viewModel.openTool(toolId) }
                    )
                    2 -> FavoritesScreen(
                        viewModel = viewModel,
                        onNavigateToTools = { viewModel.selectTab(1) },
                        onOpenTool = { toolId -> viewModel.openTool(toolId) }
                    )
                    3 -> HistoryScreen(
                        viewModel = viewModel,
                        onOpenTool = { toolId -> viewModel.openTool(toolId) }
                    )
                    4 -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
