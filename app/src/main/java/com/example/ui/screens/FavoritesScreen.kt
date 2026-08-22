package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tools.registry.ToolRegistry
import com.example.ui.components.EmptyStateView
import com.example.viewmodel.AppViewModel

@Composable
fun FavoritesScreen(
    viewModel: AppViewModel,
    onNavigateToTools: () -> Unit,
    onOpenTool: (String) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()

    val favoritedTools = remember(favorites) {
        favorites.mapNotNull { fav ->
            ToolRegistry.findToolById(fav.toolId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Favorite Tools",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${favoritedTools.size} tools pinned for quick access",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (favoritedTools.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Favorite,
                title = "No favorite tools yet",
                subtitle = "Tap the heart icon on any tool card or top bar to pin it here."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(favoritedTools) { tool ->
                    FeaturedToolCard(
                        tool = tool,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.toggleFavorite(tool.id) },
                        onClick = { onOpenTool(tool.id) }
                    )
                }
            }
        }
    }
}
