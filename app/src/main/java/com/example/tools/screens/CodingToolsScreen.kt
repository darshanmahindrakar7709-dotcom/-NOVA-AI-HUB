package com.example.tools.screens

import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun CodingToolsScreen(
    toolId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.toolId == toolId }

    var selectedTab by remember(toolId) {
        mutableIntStateOf(
            when (toolId) {
                "json_tool" -> 0
                "code_beautifier" -> 1
                "base64_url" -> 2
                "color_converter" -> 3
                "regex_tester" -> 4
                else -> 0
            }
        )
    }

    val tabTitles = listOf("JSON Tool", "Beautifier", "Base64 & URL", "Color Studio", "Regex Tester")

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Coding Studio",
                categoryName = "Coding Tools",
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
                    0 -> JsonToolsView(viewModel)
                    1 -> CodeBeautifierView(viewModel)
                    2 -> Base64UrlView(viewModel)
                    3 -> ColorStudioView(viewModel)
                    4 -> RegexTesterView(viewModel)
                }
            }
        }
    }
}

// 1. JSON TOOL
@Composable
fun JsonToolsView(viewModel: AppViewModel) {
    var jsonInput by remember {
        mutableStateOf("{\"app\":\"NOVA AI HUB\",\"version\":1.0,\"features\":[\"100% Free\",\"No Login\",\"Offline Ready\"],\"settings\":{\"theme\":\"dark\",\"cached\":true}}")
    }
    var jsonOutput by remember { mutableStateOf("") }
    var validationStatus by remember { mutableStateOf<String?>(null) }
    var isValid by remember { mutableStateOf(true) }

    fun prettify(indent: Int = 2) {
        try {
            val trimmed = jsonInput.trim()
            val formatted = if (trimmed.startsWith("{")) {
                JSONObject(trimmed).toString(indent)
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed).toString(indent)
            } else {
                throw IllegalArgumentException("JSON must start with { or [")
            }
            jsonOutput = formatted
            validationStatus = "✅ Valid JSON syntax"
            isValid = true
        } catch (e: Exception) {
            validationStatus = "❌ Syntax Error: ${e.localizedMessage}"
            isValid = false
            jsonOutput = ""
        }
    }

    fun minify() {
        try {
            val trimmed = jsonInput.trim()
            val minified = if (trimmed.startsWith("{")) {
                JSONObject(trimmed).toString()
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed).toString()
            } else {
                throw IllegalArgumentException("Invalid format")
            }
            jsonOutput = minified
            validationStatus = "✅ Minified successfully"
            isValid = true
        } catch (e: Exception) {
            validationStatus = "❌ Syntax Error: ${e.localizedMessage}"
            isValid = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("JSON Formatter & Validator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = jsonInput,
                onValueChange = {
                    jsonInput = it
                    validationStatus = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                placeholder = { Text("Paste raw JSON here...") },
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { prettify(2) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaViolet)
                ) {
                    Text("Prettify (2 Space)")
                }
                Button(
                    onClick = { minify() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaCyanDark)
                ) {
                    Text("Minify JSON")
                }
            }

            if (validationStatus != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = if (isValid) NovaEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationStatus!!,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isValid) NovaEmerald else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        CopyableOutputBox(
            title = "Formatted JSON Result",
            content = jsonOutput,
            onCopy = { viewModel.copyToClipboard(jsonOutput) }
        )
    }
}

// 2. CODE BEAUTIFIER
@Composable
fun CodeBeautifierView(viewModel: AppViewModel) {
    var language by remember { mutableStateOf("HTML") }
    var codeInput by remember { mutableStateOf("<div class=\"container\"><h1>NOVA AI HUB</h1><p>Free AI Tools</p></div>") }
    var formattedCode by remember { mutableStateOf("") }

    val languages = listOf("HTML", "CSS", "JavaScript", "XML")

    fun beautify() {
        val raw = codeInput.trim()
        formattedCode = when (language) {
            "HTML", "XML" -> {
                raw.replace("><", ">\n<")
                    .lines()
                    .joinToString("\n") { line ->
                        if (line.startsWith("</")) "  $line" else if (line.startsWith("<")) "    $line" else "      $line"
                    }
            }
            "CSS" -> {
                raw.replace("{", " {\n  ")
                    .replace(";", ";\n  ")
                    .replace("}", "\n}\n")
                    .trim()
            }
            "JavaScript" -> {
                raw.replace("{", " {\n  ")
                    .replace(";", ";\n")
                    .replace("}", "\n}\n")
                    .trim()
            }
            else -> raw
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Code Beautifier & Indenter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                languages.forEach { lang ->
                    FilterChip(
                        selected = language == lang,
                        onClick = { language = lang },
                        label = { Text(lang) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "Beautify $language Code",
                icon = Icons.Default.Code,
                onClick = { beautify() }
            )
        }

        CopyableOutputBox(
            title = "Beautified Code",
            content = formattedCode,
            onCopy = { viewModel.copyToClipboard(formattedCode) }
        )
    }
}

// 3. BASE64 & URL ENCODER/DECODER
@Composable
fun Base64UrlView(viewModel: AppViewModel) {
    var mode by remember { mutableStateOf("Base64 Encode") }
    var inputText by remember { mutableStateOf("Hello NOVA AI HUB 2026!") }
    var outputText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val modes = listOf("Base64 Encode", "Base64 Decode", "URL Encode", "URL Decode")

    fun process() {
        errorMsg = null
        try {
            outputText = when (mode) {
                "Base64 Encode" -> Base64.encodeToString(inputText.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
                "Base64 Decode" -> String(Base64.decode(inputText.trim(), Base64.DEFAULT), StandardCharsets.UTF_8)
                "URL Encode" -> URLEncoder.encode(inputText, "UTF-8")
                "URL Decode" -> URLDecoder.decode(inputText, "UTF-8")
                else -> inputText
            }
        } catch (e: Exception) {
            errorMsg = "Error: ${e.localizedMessage}"
            outputText = ""
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Encoding & Decoding Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(modes) { m ->
                    FilterChip(
                        selected = mode == m,
                        onClick = { mode = m },
                        label = { Text(m, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "Run $mode",
                icon = Icons.Default.Key,
                onClick = { process() }
            )

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        CopyableOutputBox(
            title = "Encoded / Decoded Result",
            content = outputText,
            onCopy = { viewModel.copyToClipboard(outputText) }
        )
    }
}

// 4. COLOR STUDIO & CONVERTER
@Composable
fun ColorStudioView(viewModel: AppViewModel) {
    var red by remember { mutableFloatStateOf(139f) }
    var green by remember { mutableFloatStateOf(92f) }
    var blue by remember { mutableFloatStateOf(246f) }
    var alpha by remember { mutableFloatStateOf(255f) }

    val currentColor = Color(red.toInt(), green.toInt(), blue.toInt(), alpha.toInt())
    val hexCode = String.format("#%02X%02X%02X", red.toInt(), green.toInt(), blue.toInt())
    val hexAlphaCode = String.format("#%02X%02X%02X%02X", alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
    val rgbCode = "rgb(${red.toInt()}, ${green.toInt()}, ${blue.toInt()})"
    val rgbaCode = "rgba(${red.toInt()}, ${green.toInt()}, ${blue.toInt()}, ${(alpha / 255f)})"
    val composeColorCode = "Color(0x${String.format("%02X%02X%02X%02X", alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())})"

    // HSL
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(red.toInt(), green.toInt(), blue.toInt(), hsv)
    val hslCode = "hsl(${hsv[0].toInt()}deg, ${(hsv[1] * 100).toInt()}%, ${(hsv[2] * 100).toInt()}%)"

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Color Preview Swatch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(currentColor)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = hexCode,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Sliders
        GlassCard {
            Text("Adjust RGBA Channels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            ColorSliderRow("Red (R)", red, { red = it }, NovaPink)
            ColorSliderRow("Green (G)", green, { green = it }, NovaEmerald)
            ColorSliderRow("Blue (B)", blue, { blue = it }, NovaBlue)
            ColorSliderRow("Alpha (A)", alpha, { alpha = it }, NovaViolet)
        }

        // Color Format Cards
        GlassCard {
            Text("Color Formats & Exports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            ColorCodeRow("HEX", hexCode) { viewModel.copyToClipboard(hexCode) }
            ColorCodeRow("RGB", rgbCode) { viewModel.copyToClipboard(rgbCode) }
            ColorCodeRow("RGBA", rgbaCode) { viewModel.copyToClipboard(rgbaCode) }
            ColorCodeRow("HSL", hslCode) { viewModel.copyToClipboard(hslCode) }
            ColorCodeRow("Jetpack Compose", composeColorCode) { viewModel.copyToClipboard(composeColorCode) }
        }
    }
}

@Composable
fun ColorSliderRow(label: String, value: Float, onValueChange: (Float) -> Unit, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ${value.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(95.dp),
            color = color
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}

@Composable
fun ColorCodeRow(title: String, code: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Text(code, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", modifier = Modifier.size(16.dp))
        }
    }
}

// 5. REGEX TESTER
@Composable
fun RegexTesterView(viewModel: AppViewModel) {
    var regexPattern by remember { mutableStateOf("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}") }
    var testText by remember { mutableStateOf("Contact support@novahub.ai or user.test_2026@gmail.com for inquiries.") }
    var caseInsensitive by remember { mutableStateOf(true) }

    val cheatsheets = listOf(
        "Email" to "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        "URL" to "https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/[^\\s]*)?",
        "Digits Only" to "\\d+",
        "Phone (US)" to "\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}",
        "Hex Color" to "#?([a-fA-F0-9]{6}|[a-fA-F0-9]{3})"
    )

    val matches = remember(regexPattern, testText, caseInsensitive) {
        try {
            val options = if (caseInsensitive) setOf(RegexOption.IGNORE_CASE) else emptySet()
            val regex = Regex(regexPattern, options)
            regex.findAll(testText).map { it.value }.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard {
            Text("Regex Evaluator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Quick Presets:", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(cheatsheets) { (label, pat) ->
                    SuggestionChip(
                        onClick = { regexPattern = pat },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = regexPattern,
                onValueChange = { regexPattern = it },
                label = { Text("Regular Expression Pattern") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = caseInsensitive, onCheckedChange = { caseInsensitive = it })
                Text("Case-Insensitive (i)", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = testText,
                onValueChange = { testText = it },
                label = { Text("Test String") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Matches Found: ${matches.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (matches.isNotEmpty()) {
                    FilledTonalButton(onClick = { viewModel.copyToClipboard(matches.joinToString("\n")) }) {
                        Text("Copy All")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (matches.isEmpty()) {
                Text("No pattern matches found in test string.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    matches.forEachIndexed { idx, m ->
                        Surface(
                            color = NovaVioletDark.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Match ${idx + 1}: $m",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NovaCyan,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
