package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tools.registry.ToolCategory
import com.example.tools.registry.ToolDefinition
import com.example.tools.registry.ToolRegistry
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToTools: () -> Unit,
    onOpenTool: (String) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val history by viewModel.recentHistory.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All") + ToolCategory.values().map { it.displayName }
    val featuredTools = ToolRegistry.allTools.filter { it.isFeatured }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // 1. BRAND HERO BANNER
        item {
            HeroBanner(
                onExploreClick = onNavigateToTools,
                onStartCreatingClick = { onOpenTool("ai_chat") }
            )
        }

        // 2. PRIVACY PILL
        item {
            Surface(
                color = SpaceSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("privacy_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(NovaEmerald.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = NovaEmerald, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("100% Free & Guest-Only", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NovaEmerald)
                        Text("No login, no subscriptions, zero data leaves your phone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 3. CATEGORY SELECTOR CHIPS
        item {
            Text("Tool Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            if (cat != "All") onNavigateToTools()
                        },
                        label = { Text(cat) },
                        leadingIcon = if (selectedCategory == cat) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        // 4. RECENT HISTORY (IF ANY)
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Jump Back In", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${history.size} recent", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(history.take(6)) { hist ->
                        val tool = ToolRegistry.findToolById(hist.toolId)
                        if (tool != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SpaceSurfaceElevated,
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable { onOpenTool(tool.id) }
                                    .border(1.dp, NovaViolet.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(tool.icon, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(tool.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(tool.category.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. FEATURED AI TOOLS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Featured AI Tools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onNavigateToTools) {
                    Text("See All (35+)", color = NovaCyan)
                }
            }
        }

        items(featuredTools) { tool ->
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

@Composable
fun HeroBanner(
    onExploreClick: () -> Unit,
    onStartCreatingClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(NovaVioletDark.copy(alpha = 0.85f), SpaceSurfaceElevated)
                )
            )
            .border(1.dp, NovaViolet.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Brush.linearGradient(listOf(NovaViolet, NovaCyan)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "NOVA AI HUB",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "All Your AI Tools.\nOne Free Workspace.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Write, study, create, code and get things done — without creating an account.",
                style = MaterialTheme.typography.bodyMedium,
                color = NovaVioletLight
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GradientButton(
                    text = "Start Creating",
                    icon = Icons.Default.Chat,
                    onClick = onStartCreatingClick,
                    modifier = Modifier.weight(1f).testTag("hero_start_creating_button")
                )

                OutlinedButton(
                    onClick = onExploreClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Explore Tools", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun FeaturedToolCard(
    tool: ToolDefinition,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.testTag("tool_card_${tool.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(listOf(NovaViolet.copy(alpha = 0.3f), NovaCyan.copy(alpha = 0.3f))),
                        RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, NovaViolet.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(tool.icon, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tool.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text(tool.category.displayName, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) NovaPink else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
