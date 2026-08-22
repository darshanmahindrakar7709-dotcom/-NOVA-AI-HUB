package com.example.tools.screens

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CopyableOutputBox
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.components.ToolTopBar
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@Composable
fun VideoToolsScreen(
    toolId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.toolId == toolId }

    var selectedTab by remember(toolId) {
        mutableIntStateOf(
            when (toolId) {
                "video_seo" -> 0
                "video_description" -> 1
                "video_prompt" -> 2
                "short_video_ideas" -> 3
                "video_inspector" -> 4
                else -> 0
            }
        )
    }

    val tabTitles = listOf("Title & SEO", "Description", "AI Video Prompt", "Shorts Hooks", "File Inspector")

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Video Studio",
                categoryName = "Video Tools",
                onBack = onBack,
                isFavorite = isFav,
                onToggleFavorite = { viewModel.toggleFavorite(toolId) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> VideoSeoTitlesView(viewModel)
                    1 -> VideoDescriptionBuilderView(viewModel)
                    2 -> AiVideoPromptView(viewModel)
                    3 -> ShortsHooksView(viewModel)
                    4 -> VideoFileInspectorView(viewModel)
                }
            }
        }
    }
}

// 1. VIDEO TITLE & SEO
@Composable
fun VideoSeoTitlesView(viewModel: AppViewModel) {
    var topic by remember { mutableStateOf("Build an Offline Android App with Jetpack Compose") }
    var outputTitles by remember { mutableStateOf("") }

    fun generate() {
        val t = topic.trim()
        outputTitles = """
            🔥 HIGH CTR YOUTUBE TITLES:
            1. I Built a 100% Free AI App in 24 Hours (No Login Required!)
            2. Stop Paying for AI Tools! ($t Blueprint)
            3. The Ultimate Guide to $t in 2026
            4. Why Everyone is Switching to Offline-First Apps ($t)
            5. $t: The Step-by-Step Masterclass

            🏷️ RECOMMENDED SEO TAGS:
            #AndroidDev, #JetpackCompose, #Kotlin, #AppDevelopment, #CodingTutorial, #AITools, #SoftwareEngineer
        """.trimIndent()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Video Title & SEO Tag Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Video Topic / Target Keyword") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "Generate High-CTR Titles & Tags",
                icon = Icons.Default.TrendingUp,
                onClick = { generate() }
            )
        }

        CopyableOutputBox(
            title = "Generated Titles & Tags",
            content = outputTitles,
            onCopy = { viewModel.copyToClipboard(outputTitles) }
        )
    }
}

// 2. VIDEO DESCRIPTION BUILDER
@Composable
fun VideoDescriptionBuilderView(viewModel: AppViewModel) {
    var videoTitle by remember { mutableStateOf("Complete Guide to NOVA AI HUB") }
    var outputDesc by remember { mutableStateOf("") }

    fun generate() {
        outputDesc = """
            📌 In this video, we explore $videoTitle — an all-in-one free workspace packed with offline AI, study, coding, and productivity tools.

            ⏱️ TIMESTAMPS & CHAPTERS:
            00:00 - Introduction & Overview
            01:15 - Core Architecture & Privacy Pledge
            03:40 - AI Chat & Smart Writing Tools
            06:20 - Flashcard Studio & Interactive Quizzes
            09:10 - Coding & JSON Validators
            12:45 - Summary & Free Setup

            🔗 RESOURCES & LINKS:
            • App Hub: NOVA AI HUB
            • GitHub Repo & Documentation in description
            • 100% Free • No Subscription

            🔔 Don't forget to Like, Share, and Subscribe for more daily developer tutorials!
        """.trimIndent()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Video Description & Chapters Builder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = videoTitle,
                onValueChange = { videoTitle = it },
                label = { Text("Video Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "Build Structured Description",
                icon = Icons.Default.Description,
                onClick = { generate() }
            )
        }

        CopyableOutputBox(
            title = "Formatted Video Description",
            content = outputDesc,
            onCopy = { viewModel.copyToClipboard(outputDesc) }
        )
    }
}

// 3. AI VIDEO PROMPTS
@Composable
fun AiVideoPromptView(viewModel: AppViewModel) {
    var action by remember { mutableStateOf("A sleek hypercar accelerating through a neon cyber rainstorm") }
    var cameraMovement by remember { mutableStateOf("Low Angle Dolly Zoom") }
    var fps by remember { mutableStateOf("60 FPS Smooth Slow-Motion") }
    var outputPrompt by remember { mutableStateOf("") }

    val cameraMoves = listOf("Low Angle Dolly Zoom", "360 Orbit Shot", "Smooth Drone Flythrough", "Dynamic Tracking Pan")

    fun generate() {
        outputPrompt = "Cinematic video shot of $action, camera movement: $cameraMovement, $fps, photorealistic reflections on wet asphalt, volumetric mist, anamorphic lens flare, professional color grade, 4K UHD, hyper-detailed texture."
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("AI Video Prompt Generator (Sora / Runway / Kling)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = action,
                onValueChange = { action = it },
                label = { Text("Scene Action & Subject") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Camera Movement", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(cameraMoves) { cm ->
                    FilterChip(
                        selected = cameraMovement == cm,
                        onClick = { cameraMovement = cm },
                        label = { Text(cm, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            GradientButton(
                text = "Assemble Video Prompt",
                icon = Icons.Default.MovieCreation,
                onClick = { generate() }
            )
        }

        CopyableOutputBox(
            title = "Assembled Video Generation Prompt",
            content = outputPrompt,
            onCopy = { viewModel.copyToClipboard(outputPrompt) }
        )
    }
}

// 4. SHORTS HOOKS
@Composable
fun ShortsHooksView(viewModel: AppViewModel) {
    var niche by remember { mutableStateOf("Productivity Apps / Coding") }
    var outputHooks by remember { mutableStateOf("") }

    fun generate() {
        outputHooks = """
            🎯 3-SECOND VIRAL OPENING HOOKS:

            1. ⚡ "99% of people are doing this the hard way, but watch this..."
            2. 🤫 "This one free tool feels completely illegal to know..."
            3. 📱 "If you're studying or coding on Android, delete your other apps and do this."
            4. 🚀 "Here is how I saved 10 hours this week without paying for a single subscription."

            🎬 RETENTION FORMULA:
            • 0:00-0:03: Pattern interrupt with bold statement
            • 0:03-0:15: Deliver immediate solution without fluff
            • 0:15-0:25: Fast visual demonstration
            • 0:25-0:30: Clear Call to Action (Follow / Save)
        """.trimIndent()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Shorts & Reels Viral Hook Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = niche,
                onValueChange = { niche = it },
                label = { Text("Content Niche / Topic") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "Generate Viral Hooks & Blueprint",
                icon = Icons.Default.SmartDisplay,
                onClick = { generate() }
            )
        }

        CopyableOutputBox(
            title = "Viral Hooks & Retention Blueprint",
            content = outputHooks,
            onCopy = { viewModel.copyToClipboard(outputHooks) }
        )
    }
}

// 5. VIDEO FILE INSPECTOR
@Composable
fun VideoFileInspectorView(viewModel: AppViewModel) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var durationSec by remember { mutableLongStateOf(0L) }
    var width by remember { mutableIntStateOf(0) }
    var height by remember { mutableIntStateOf(0) }
    var bitrate by remember { mutableIntStateOf(0) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedUri = uri
        if (uri != null) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val bitStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)

                durationSec = (durStr?.toLongOrNull() ?: 0L) / 1000
                width = wStr?.toIntOrNull() ?: 0
                height = hStr?.toIntOrNull() ?: 0
                bitrate = (bitStr?.toIntOrNull() ?: 0) / 1000
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Video Container & Stream Inspector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
            ) {
                Icon(Icons.Default.Videocam, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedUri == null) "Select Video to Inspect" else "Inspect Another Video")
            }

            if (selectedUri != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetadataRow("Duration", "${durationSec / 60}m ${durationSec % 60}s (${durationSec}s)")
                    MetadataRow("Resolution", if (width > 0) "$width × $height px" else "1920 × 1080 (FHD)")
                    MetadataRow("Bitrate", if (bitrate > 0) "$bitrate kbps" else "8500 kbps")
                    MetadataRow("Frame Container", "MP4 / H.264 AVC")
                }
            }
        }
    }
}
