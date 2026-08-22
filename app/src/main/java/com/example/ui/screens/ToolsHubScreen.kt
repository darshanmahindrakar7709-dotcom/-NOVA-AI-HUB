package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tools.registry.ToolCategory
import com.example.tools.registry.ToolRegistry
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@Composable
fun ToolsHubScreen(
    viewModel: AppViewModel,
    onOpenTool: (String) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }

    val categories = ToolCategory.values().toList()

    val filteredTools = remember(searchQuery, selectedCategory) {
        ToolRegistry.searchTools(searchQuery, selectedCategory)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "AI Tools Directory",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${filteredTools.size} free tools available offline",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tools by name, tag, keyword...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaCyan) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth().testTag("tools_search_input"),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All Categories") }
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                    label = { Text(cat.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredTools.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.SearchOff,
                title = "No tools found",
                subtitle = "Try changing your search query or removing category filters."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredTools) { tool ->
                    val isFav = favorites.any { it.toolId == tool.id }
                    FeaturedToolCard(
                        tool = tool,
                        isFavorite = isFav,
                        onToggleFavorite = { viewModel.toggleFavorite(tool.id) },
                        onClick = { onOpenTool(tool.id) }
                    )
                }
            }
        }
    }
}
