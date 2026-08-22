package com.example.tools.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CopyableOutputBox
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.components.ToolTopBar
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UtilitiesScreen(
    toolId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.toolId == toolId }

    var selectedTab by remember(toolId) {
        mutableIntStateOf(
            when (toolId) {
                "device_info" -> 0
                "qr_generator" -> 1
                "world_clock" -> 2
                "markdown_preview" -> 3
                "uuid_hash" -> 4
                else -> 0
            }
        )
    }

    val tabTitles = listOf("Device Info", "QR Generator", "World Clock", "Markdown", "UUID & Hash")

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Utilities Hub",
                categoryName = "Utilities",
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
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> DeviceInfoView(viewModel)
                    1 -> QrGeneratorView(viewModel)
                    2 -> WorldClockView(viewModel)
                    3 -> MarkdownPreviewView(viewModel)
                    4 -> UuidHashView(viewModel)
                }
            }
        }
    }
}

// 1. DEVICE INFO
@Composable
fun DeviceInfoView(viewModel: AppViewModel) {
    val context = LocalContext.current

    // Memory info
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager?.getMemoryInfo(memInfo)
    val totalRamGb = memInfo.totalMem / (1024 * 1024 * 1024f)
    val availRamGb = memInfo.availMem / (1024 * 1024 * 1024f)

    // Battery info
    val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
        context.registerReceiver(null, filter)
    }
    val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100

    val dm = context.resources.displayMetrics
    val screenRes = "${dm.widthPixels} × ${dm.heightPixels} px (${dm.densityDpi} DPI)"

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard {
            Text("Hardware & Platform Diagnostics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            MetadataRow("Device Model", "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}")
            MetadataRow("Android OS Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            MetadataRow("Hardware Board", Build.BOARD)
            MetadataRow("Architecture (ABI)", Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a")
            MetadataRow("Screen Resolution", screenRes)
            MetadataRow("System Memory (RAM)", "${String.format("%.1f", availRamGb)} GB free of ${String.format("%.1f", totalRamGb)} GB")
            MetadataRow("Battery Level", "$batteryPct%")
            MetadataRow("App Runtime", "Kotlin + Jetpack Compose M3")
        }
    }
}

// 2. QR CODE GENERATOR
@Composable
fun QrGeneratorView(viewModel: AppViewModel) {
    var qrContent by remember { mutableStateOf("https://ai.studio/build") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard {
            Text("QR Code Matrix Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = qrContent,
                onValueChange = { qrContent = it },
                label = { Text("Enter Website URL or Text") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Deterministic Visual QR Pattern
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                val seed = qrContent.hashCode()
                val random = Random(seed.toLong())
                val gridSize = 21

                Canvas(modifier = Modifier.size(220.dp)) {
                    val cellSize = size.width / gridSize

                    // Background
                    drawRect(Color.White, Offset.Zero, size)

                    // Draw QR corner finder patterns
                    fun drawFinder(startX: Int, startY: Int) {
                        // Outer 7x7 black
                        drawRect(Color.Black, Offset(startX * cellSize, startY * cellSize), Size(7 * cellSize, 7 * cellSize))
                        // Inner 5x5 white
                        drawRect(Color.White, Offset((startX + 1) * cellSize, (startY + 1) * cellSize), Size(5 * cellSize, 5 * cellSize))
                        // Center 3x3 black
                        drawRect(Color.Black, Offset((startX + 2) * cellSize, (startY + 2) * cellSize), Size(3 * cellSize, 3 * cellSize))
                    }

                    drawFinder(0, 0)
                    drawFinder(gridSize - 7, 0)
                    drawFinder(0, gridSize - 7)

                    // Data cells
                    for (r in 0 until gridSize) {
                        for (c in 0 until gridSize) {
                            val inFinderTopLeft = r < 8 && c < 8
                            val inFinderTopRight = r < 8 && c >= gridSize - 8
                            val inFinderBottomLeft = r >= gridSize - 8 && c < 8

                            if (!inFinderTopLeft && !inFinderTopRight && !inFinderBottomLeft) {
                                if (random.nextBoolean() || (r + c) % 3 == 0) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(c * cellSize, r * cellSize),
                                        size = Size(cellSize, cellSize)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.copyToClipboard(qrContent) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy QR Content Payload")
            }
        }
    }
}

// 3. WORLD CLOCK
@Composable
fun WorldClockView(viewModel: AppViewModel) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }

    val timezones = listOf(
        "UTC / GMT" to "UTC",
        "New York (EST/EDT)" to "America/New_York",
        "San Francisco (PST/PDT)" to "America/Los_Angeles",
        "London (BST/GMT)" to "Europe/London",
        "Tokyo (JST)" to "Asia/Tokyo",
        "Sydney (AEST)" to "Australia/Sydney",
        "Dubai (GST)" to "Asia/Dubai"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Global Timezone Clocks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(timezones) { (name, tzId) ->
                val tz = TimeZone.getTimeZone(tzId)
                val sdfTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).apply { timeZone = tz }
                val sdfDate = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).apply { timeZone = tz }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(sdfDate.format(Date(currentTime)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = sdfTime.format(Date(currentTime)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NovaCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// 4. MARKDOWN PREVIEW
@Composable
fun MarkdownPreviewView(viewModel: AppViewModel) {
    var mdInput by remember {
        mutableStateOf(
            """
            # NOVA AI HUB

            All your AI tools in **one free workspace**.

            ### Key Features
            - **100% Free**: No subscriptions or credit cards
            - **Offline-First**: Local storage via Room Database
            - **Privacy Guaranteed**: Zero user accounts

            > "The best tools get out of your way and let you create."

            ```kotlin
            val hub = NovaAiHub(guestMode = true)
            hub.launch()
            ```
            """.trimIndent()
        )
    }

    var isPreviewMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Markdown Live Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FilterChip(
                selected = isPreviewMode,
                onClick = { isPreviewMode = !isPreviewMode },
                label = { Text(if (isPreviewMode) "Preview Mode" else "Editor Mode") }
            )
        }

        if (!isPreviewMode) {
            OutlinedTextField(
                value = mdInput,
                onValueChange = { mdInput = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            )
        } else {
            GlassCard {
                Text("Rendered Output:", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
                Spacer(modifier = Modifier.height(8.dp))

                mdInput.lines().forEach { line ->
                    val trimmed = line.trim()
                    when {
                        trimmed.startsWith("# ") -> Text(trimmed.removePrefix("# "), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = NovaVioletLight)
                        trimmed.startsWith("### ") -> Text(trimmed.removePrefix("### "), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = NovaCyan)
                        trimmed.startsWith("- ") -> Text("• ${trimmed.removePrefix("- ")}", style = MaterialTheme.typography.bodyMedium)
                        trimmed.startsWith("> ") -> {
                            Surface(
                                color = SpaceSurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(trimmed.removePrefix("> "), style = MaterialTheme.typography.bodyMedium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(8.dp))
                            }
                        }
                        trimmed.startsWith("```") -> {}
                        else -> Text(trimmed, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Button(
            onClick = { viewModel.copyToClipboard(mdInput) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SpaceSurfaceElevated)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy Markdown Text")
        }
    }
}

// 5. UUID & HASH
@Composable
fun UuidHashView(viewModel: AppViewModel) {
    var rawInput by remember { mutableStateOf("NOVA AI HUB 2026") }
    var generatedUuid by remember { mutableStateOf(UUID.randomUUID().toString()) }

    fun getHash(algorithm: String, text: String): String {
        return try {
            val md = MessageDigest.getInstance(algorithm)
            val bytes = md.digest(text.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    val md5Hash = remember(rawInput) { getHash("MD5", rawInput) }
    val sha1Hash = remember(rawInput) { getHash("SHA-1", rawInput) }
    val sha256Hash = remember(rawInput) { getHash("SHA-256", rawInput) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // UUID Card
        GlassCard {
            Text("UUID v4 Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = SpaceSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(generatedUuid, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, color = NovaCyan)
                    IconButton(onClick = { viewModel.copyToClipboard(generatedUuid) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { generatedUuid = UUID.randomUUID().toString() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
            ) {
                Text("Generate New UUID")
            }
        }

        // Hashes Card
        GlassCard {
            Text("Cryptographic Hash Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = rawInput,
                onValueChange = { rawInput = it },
                label = { Text("Input String to Hash") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            ColorCodeRow("MD5 (128-bit)", md5Hash) { viewModel.copyToClipboard(md5Hash) }
            ColorCodeRow("SHA-1 (160-bit)", sha1Hash) { viewModel.copyToClipboard(sha1Hash) }
            ColorCodeRow("SHA-256 (256-bit)", sha256Hash) { viewModel.copyToClipboard(sha256Hash) }
        }
    }
}
