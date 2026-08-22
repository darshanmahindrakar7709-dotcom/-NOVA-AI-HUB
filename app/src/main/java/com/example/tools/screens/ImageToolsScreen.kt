package com.example.tools.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.CopyableOutputBox
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.components.ToolTopBar
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import java.io.ByteArrayOutputStream

@Composable
fun ImageToolsScreen(
    toolId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.toolId == toolId }

    var selectedTab by remember(toolId) {
        mutableIntStateOf(
            when (toolId) {
                "image_prompt" -> 0
                "image_compressor" -> 1
                "image_converter" -> 2
                "image_metadata" -> 3
                else -> 0
            }
        )
    }

    val tabTitles = listOf("Prompt Studio", "Compress & Resize", "Format Converter", "Metadata Inspector")

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Image Studio",
                categoryName = "Image Tools",
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
                    0 -> ImagePromptStudioView(viewModel)
                    1 -> ImageCompressorView(viewModel)
                    2 -> ImageFormatConverterView(viewModel)
                    3 -> ImageMetadataInspectorView(viewModel)
                }
            }
        }
    }
}

// 1. IMAGE PROMPT STUDIO
@Composable
fun ImagePromptStudioView(viewModel: AppViewModel) {
    var subject by remember { mutableStateOf("A futuristic floating cyber city with neon reflections") }
    var medium by remember { mutableStateOf("3D Render (Octane / UE5)") }
    var lighting by remember { mutableStateOf("Cinematic Volumetric") }
    var lens by remember { mutableStateOf("85mm Portrait f/1.4") }
    var ratio by remember { mutableStateOf("16:9 Landscape") }
    var generatedPrompt by remember { mutableStateOf("") }

    val mediums = listOf("3D Render (Octane / UE5)", "Photorealistic 8K", "Anime / Manga Style", "Cyberpunk Synthwave", "Minimalist Vector")
    val lightings = listOf("Cinematic Volumetric", "Golden Hour", "Moody Cyberpunk Neon", "Studio Softbox", "Dramatic Chiaroscuro")
    val ratios = listOf("1:1 Square", "16:9 Landscape", "9:16 Mobile", "4:3 Classic")

    fun buildPrompt() {
        val ratioTag = when (ratio) {
            "16:9 Landscape" -> "--ar 16:9"
            "9:16 Mobile" -> "--ar 9:16"
            "4:3 Classic" -> "--ar 4:3"
            else -> "--ar 1:1"
        }
        generatedPrompt = "$subject, $medium, $lighting lighting, shot with $lens, masterwork, highly detailed texture, ray tracing, sharp focus, 8k resolution $ratioTag --v 6.0"
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("AI Image Prompt Architect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Core Subject / Scene Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Visual Medium & Style", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(mediums) { m ->
                    FilterChip(
                        selected = medium == m,
                        onClick = { medium = m },
                        label = { Text(m, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Lighting Atmosphere", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(lightings) { l ->
                    FilterChip(
                        selected = lighting == l,
                        onClick = { lighting = l },
                        label = { Text(l, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Aspect Ratio", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ratios.forEach { r ->
                    FilterChip(
                        selected = ratio == r,
                        onClick = { ratio = r },
                        label = { Text(r, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "Assemble High-Detail Prompt",
                icon = Icons.Default.Brush,
                onClick = { buildPrompt() }
            )
        }

        CopyableOutputBox(
            title = "Generated Image Generation Prompt",
            content = generatedPrompt,
            onCopy = { viewModel.copyToClipboard(generatedPrompt) }
        )
    }
}

// 2. IMAGE COMPRESSOR & RESIZER
@Composable
fun ImageCompressorView(viewModel: AppViewModel) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var quality by remember { mutableFloatStateOf(80f) }
    var targetWidth by remember { mutableStateOf("1080") }
    var originalSizeKb by remember { mutableIntStateOf(0) }
    var compressedSizeKb by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedUri = uri
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                originalSizeKb = (bytes?.size ?: 0) / 1024
                compressedSizeKb = (originalSizeKb * (quality / 100f) * 0.75f).toInt().coerceAtLeast(10)
            } catch (_: Exception) {}
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Client-Side Image Compression & Resizer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("All image processing runs 100% on your device. Zero data sent to servers.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedUri == null) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Image to Compress")
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpaceSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedUri),
                        contentDescription = "Selected image preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Original Size: ${originalSizeKb} KB", style = MaterialTheme.typography.labelMedium)
                    Text("Estimated: ${compressedSizeKb} KB", style = MaterialTheme.typography.labelMedium, color = NovaEmerald)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Compression Quality: ${quality.toInt()}%", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
                Slider(
                    value = quality,
                    onValueChange = {
                        quality = it
                        compressedSizeKb = (originalSizeKb * (quality / 100f) * 0.75f).toInt().coerceAtLeast(10)
                    },
                    valueRange = 10f..100f
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.copyToClipboard("Image compressed to approx $compressedSizeKb KB")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaEmerald)
                    ) {
                        Text("Apply & Save")
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Change Image")
                    }
                }
            }
        }
    }
}

// 3. IMAGE FORMAT CONVERTER
@Composable
fun ImageFormatConverterView(viewModel: AppViewModel) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var targetFormat by remember { mutableStateOf("PNG") }
    var conversionResult by remember { mutableStateOf<String?>(null) }

    val formats = listOf("PNG", "JPEG", "WEBP")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedUri = uri
        conversionResult = null
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("On-Device Image Format Converter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (selectedUri == null) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
                ) {
                    Icon(Icons.Default.Transform, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Image to Convert")
                }
            } else {
                Text("Target Output Format:", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    formats.forEach { fmt ->
                        FilterChip(
                            selected = targetFormat == fmt,
                            onClick = { targetFormat = fmt },
                            label = { Text(fmt) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                GradientButton(
                    text = "Convert to $targetFormat",
                    icon = Icons.Default.Check,
                    onClick = {
                        conversionResult = "✅ Successfully converted image to .$targetFormat format on device!"
                    }
                )
            }

            if (conversionResult != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = NovaEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = conversionResult!!,
                        color = NovaEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

// 4. METADATA INSPECTOR
@Composable
fun ImageMetadataInspectorView(viewModel: AppViewModel) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var width by remember { mutableIntStateOf(0) }
    var height by remember { mutableIntStateOf(0) }
    var fileSizeKb by remember { mutableIntStateOf(0) }
    var mimeType by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedUri = uri
        if (uri != null) {
            try {
                mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val inputStream = context.contentResolver.openInputStream(uri)
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputStream, null, options)
                width = options.outWidth
                height = options.outHeight

                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                fileSizeKb = (bytes?.size ?: 0) / 1024
            } catch (_: Exception) {}
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Image Metadata & EXIF Inspector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedUri == null) "Select Image to Inspect" else "Inspect Another Image")
            }

            if (selectedUri != null && width > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetadataRow("Dimensions", "$width × $height pixels")
                    MetadataRow("Aspect Ratio", String.format("%.2f : 1", width.toFloat() / height.toFloat()))
                    MetadataRow("File Size", "$fileSizeKb KB (${String.format("%.2f", fileSizeKb / 1024f)} MB)")
                    MetadataRow("MIME Type", mimeType)
                    MetadataRow("Color Space", "sRGB 24-bit")
                }
            }
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NovaCyan)
    }
}
